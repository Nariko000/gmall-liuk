package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@CrossOrigin
@Controller
public class ListController {

    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;

    @RequestMapping("list.html")
    public String list(SkuLsParams skuLsParams, HttpServletRequest request){
        skuLsParams.setPageSize(3);
        SkuLsResult skuLsResult = listService.search(skuLsParams);
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> baseAttrInfoList = manageService.getAttrInfoList(attrValueIdList);
        String urlParam = makeUrlParam(skuLsParams);
        List<BaseAttrValue> baseAttrValueArrayList = new ArrayList<>();
        if (baseAttrInfoList != null && baseAttrInfoList.size() > 0){
            for (Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator(); iterator.hasNext(); ) {
                BaseAttrInfo baseAttrInfo = iterator.next();
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0){
                        for (String valueId : skuLsParams.getValueId()) {
                            if (baseAttrValue.getId().equals(valueId)){
                                iterator.remove();
                                BaseAttrValue baseAttrValueed = new BaseAttrValue();
                                baseAttrValueed.setValueName(baseAttrInfo.getAttrName() + ":" + baseAttrValue.getValueName());
                                String newUrlParam = makeUrlParam(skuLsParams, valueId);
                                baseAttrValueed.setUrlParam(newUrlParam);
                                baseAttrValueArrayList.add(baseAttrValueed);

                            }
                        }
                    }
                }
            }
        }
        System.out.println("查询的参数列表:" + urlParam);
        request.setAttribute("skuLsInfoList", skuLsInfoList);
        request.setAttribute("baseAttrInfoList", baseAttrInfoList);
        request.setAttribute("urlParam", urlParam);
        request.setAttribute("baseAttrValueArrayList", baseAttrValueArrayList);
        request.setAttribute("keyword", skuLsParams.getKeyword());
        request.setAttribute("pageNo", skuLsParams.getPageNo());
        request.setAttribute("totalPages", skuLsResult.getTotalPages());
        return "list";
    }

    private String makeUrlParam(SkuLsParams skuLsParams, String... excludeValueIds) {
        String urlParam = "";
        if (skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0){
            urlParam += "keyword=" + skuLsParams.getKeyword();
        }
        if (skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length() > 0){
            urlParam += "catalog3Id=" + skuLsParams.getCatalog3Id();
        }
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0){
            for (String valueId : skuLsParams.getValueId()) {
                if (excludeValueIds != null && excludeValueIds.length > 0){
                    String excludeValueId = excludeValueIds[0];
                    if (excludeValueId.equals(valueId)){
                        continue;
                    }
                }
                urlParam += "&valueId=" + valueId;
            }
        }
        return urlParam;
    }

}
