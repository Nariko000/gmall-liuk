package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    private JestClient jestClient;

    @Autowired
    private RedisUtil redisUtil;

    private static final String ES_INDEX="gmall";

    private static final String ES_TYPE="SkuInfo";

    @Override
    public void saveSkuInfo(SkuLsInfo skuLsInfo) {
        Index index = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();
        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {
        String query = makeQueryStringForSearch(skuLsParams);
        Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();
        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return makeResultForSearch(searchResult, skuLsParams);
    }

    @Override
    public void incrHotScore(String skuId) {
        Jedis jedis = redisUtil.getJedis();
        Double hotScore = jedis.zincrby("hotScore", 1, "skuId:" + skuId);
        if (hotScore % 10 == 0){
            updateHotScore(skuId, Math.round(hotScore));
        }
    }

    private void updateHotScore(String skuId, long hotScore) {
        String upd="{\n" +
                "  \"doc\": {\n" +
                "      \"hotScore\": "+hotScore+"\n" +
                "  }\n" +
                "}";

        // update
        Update update = new Update.Builder(upd).index(ES_INDEX).type(ES_TYPE).id(skuId).build();

        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 返回结果集
    private SkuLsResult makeResultForSearch(SearchResult searchResult, SkuLsParams skuLsParams) {
        SkuLsResult skuLsResult = new SkuLsResult();
        List<SkuLsInfo> infoArrayList = new ArrayList<>();
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        if (hits != null && hits.size() > 0){
            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits){
                SkuLsInfo skuLsInfo = hit.source;
                if (hit.highlight != null && hit.highlight.size() > 0){
                    List<String> list = hit.highlight.get("skuName");
                    String skuNameHI = list.get(0);
                    skuLsInfo.setSkuName(skuNameHI);
                }
                infoArrayList.add(skuLsInfo);
            }
        }
        skuLsResult.setSkuLsInfoList(infoArrayList);
        skuLsResult.setTotal(searchResult.getTotal());
        long totalPages = (searchResult.getTotal() + skuLsParams.getPageSize() - 1) / skuLsParams.getPageSize();
        skuLsResult.setTotalPages(totalPages);
        List<String> stringArrayList = new ArrayList<>();
        TermsAggregation groupby_attr = searchResult.getAggregations().getTermsAggregation("groupby_attr");
        List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
        if (buckets != null && buckets.size() > 0){
            for (TermsAggregation.Entry bucket : buckets) {
                String valueId = bucket.getKey();
                stringArrayList.add(valueId);
            }
        }
        skuLsResult.setAttrValueIdList(stringArrayList);
        return skuLsResult;
    }

    // 根据用户输入的检索条件dsl 语句
    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length() > 0){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0){
            for (String valueId : skuLsParams.getValueId()) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }
        if (skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.preTags("<span style=color:red>");
            highlightBuilder.postTags("</span>");
            highlightBuilder.field("skuName");
            searchSourceBuilder.highlight(highlightBuilder);
        }
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);
        int from = skuLsParams.getPageSize() * (skuLsParams.getPageNo() - 1);
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParams.getPageSize());
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);
        searchSourceBuilder.query(boolQueryBuilder);
        String query = searchSourceBuilder.toString();
        System.out.println("query" + query);
        return query;
    }

}
