package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.manage.mapper.*;
import static com.atguigu.gmall.manage.constant.ManageConst.*;
import com.atguigu.gmall.service.ManageService;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

//    @Resource
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(BaseCatalog2 baseCatalog2) {
        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(BaseCatalog3 baseCatalog3) {
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(BaseAttrInfo baseAttrInfo) {
        return baseAttrInfoMapper.select(baseAttrInfo);
    }

    @Transactional
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        //如果有主键就进行删除或更新，如果没有就插入
        if (baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0){
            //如果有属性名和属性值就进行更新，如果没有就删除
            if(StringUtils.isEmpty(baseAttrInfo.getAttrName()) && (attrValueList == null || attrValueList.size() == 0)){
                baseAttrInfoMapper.deleteByPrimaryKey(baseAttrInfo);
            } else {
                baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
            }
        } else {
                baseAttrInfo.setId(null);
                baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        //把原属性值全部清空
        BaseAttrValue baseAttrValueDel = new BaseAttrValue();
        baseAttrValueDel.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValueDel);
        //重新插入属性值
        if (attrValueList != null && attrValueList.size() > 0){
            for (BaseAttrValue attrValue : baseAttrInfo.getAttrValueList()){
                attrValue.setId(null);
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);
            }
        }

    }

    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        BaseAttrInfo attrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        attrInfo.setAttrValueList(this.getAttrValueList(attrId));
        return attrInfo;
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        return baseAttrValueMapper.select(baseAttrValue);
    }

    @Override
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo) {
        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        spuInfoMapper.insertSelective(spuInfo);
        String spuId = spuInfo.getId();
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList != null && spuImageList.size() > 0){
            for (SpuImage spuImage : spuImageList){
                spuImage.setSpuId(spuId);
                spuImageMapper.insertSelective(spuImage);
            }
        }
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList != null && spuSaleAttrList.size() > 0){
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList){
                spuSaleAttr.setSpuId(spuId);
                spuSaleAttrMapper.insertSelective(spuSaleAttr);
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (spuSaleAttrValueList != null && spuSaleAttrValueList.size() > 0){
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList){
                        spuSaleAttrValue.setSpuId(spuId);
                        spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                    }
                }
            }
        }
    }

    @Override
    public List<SpuImage> getSpuImageList(SpuImage spuImage) {
        return spuImageMapper.select(spuImage);
    }

    @Override
    public List<BaseAttrInfo> getAttrInfoList(String catalog3Id) {
        return baseAttrInfoMapper.selectBaseAttrInfoListByCatalog3Id(catalog3Id);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    @Transactional
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        skuInfoMapper.insertSelective(skuInfo);
        String skuId = skuInfo.getId();
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (skuAttrValueList != null && skuAttrValueList.size() > 0){
            for (SkuAttrValue skuAttrValue : skuAttrValueList){
                skuAttrValue.setSkuId(skuId);
                skuAttrValueMapper.insertSelective(skuAttrValue);
            }
        }
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList != null && skuSaleAttrValueList.size() > 0){
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList){
                skuSaleAttrValue.setSkuId(skuId);
                skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }
        }
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList != null && skuImageList.size() > 0){
            for (SkuImage skuImage : skuImageList){
                skuImage.setSkuId(skuId);
                skuImageMapper.insertSelective(skuImage);
            }
        }


    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {
        SkuInfo skuInfo = null;
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String skuKey = SKUKEY_PREFIX + skuId + SKUKEY_SUFFIX;
            String skuJson = jedis.get(skuKey);
            if (skuJson == null){
                Config config = new Config();
                config.useSingleServer().setAddress("redis://192.168.194.132:6379");
                RedissonClient redisson = Redisson.create(config);
                RLock lock = redisson.getLock("myLock");
                System.out.println("redisson 分布式锁！");
                boolean res = lock.tryLock(100, 10, TimeUnit.SECONDS);
                if(res){
                    try {
                        skuInfo = getSkuInfoDB(skuId);
                        if(skuInfo == null){
                            jedis.setex(skuKey, SKUKEY_TIMEOUT, "");
                        }
                        jedis.setex(skuKey, SKUKEY_TIMEOUT, JSON.toJSONString(skuInfo));
                        return skuInfo;
                    } finally {
                        lock.unlock();
                    }
                }
            }else {
                skuInfo = JSON.parseObject(skuJson, SkuInfo.class);
                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null){
                jedis.close();
            }
        }
//        return getSkuInfoByRedisSet(skuId);
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoByRedisSet(String skuId) {
        SkuInfo skuInfo = null;
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String skuKey = SKUKEY_PREFIX + skuId + SKUKEY_SUFFIX;
            String skuJson = jedis.get(skuKey);
            if (skuJson == null){
                System.out.println("缓存中没有数据：");
                String skuLockKey = SKUKEY_PREFIX + skuId + SKULOCK_SUFFIX;
                String token = UUID.randomUUID().toString().replace("-", "");
                String lockKey = jedis.set(skuLockKey, token, "NX", "PX", SKULOCK_EXPIRE_PX);
                if("OK".equals(lockKey)){
                    System.out.println("上锁成功！");
                    skuInfo = getSkuInfoDB(skuId);
                    jedis.setex(skuKey, SKUKEY_TIMEOUT, JSON.toJSONString(skuInfo));
                    String script ="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    jedis.eval(script, Collections.singletonList(skuLockKey), Collections.singletonList(token));
                    return skuInfo;
                }else {
                    Thread.sleep(1000);
                    return getSkuInfo(skuId);
                }
            }else {
                skuInfo = JSON.parseObject(skuJson, SkuInfo.class);
                return skuInfo;
            }
//            if (jedis.exists(skuKey)){
//                String skuJson = jedis.get(skuKey);
//                if (!StringUtils.isEmpty(skuJson)){
//                    skuInfo = JSON.parseObject(skuJson, SkuInfo.class);
//                    return skuInfo;
//                }
//            }else {
//                skuInfo = getSkuInfoDB(skuId);
//                jedis.setex(skuKey, SKUKEY_TIMEOUT, JSON.toJSONString(skuInfo));
//                return skuInfo;
//            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null){
                jedis.close();
            }
        }
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoDB(String skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        skuInfo.setSkuImageList(skuImageMapper.select(skuImage));
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        skuInfo.setSkuAttrValueList(skuAttrValueMapper.select(skuAttrValue));
        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        return skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
    }

    @Override
    public Map getSkuValueIdsMap(String spuId) {
        // 根据spuId 组成map
        List<Map> mapList =  skuSaleAttrValueMapper.getSaleAttrValuesBySpu(spuId);
        HashMap<Object, Object> hashMap = new HashMap<>();
        for (Map map : mapList) {
            hashMap.put(map.get("value_ids"),map.get("sku_id"));
        }
        return hashMap;

    }

    @Override
    public List<BaseAttrInfo> getAttrInfoList(List<String> attrValueIdList) {
        String attrValueIds = org.apache.commons.lang3.StringUtils.join(attrValueIdList.toArray(), ",");
        return baseAttrInfoMapper.selectAttrInfoListByIds(attrValueIds);
    }

}
