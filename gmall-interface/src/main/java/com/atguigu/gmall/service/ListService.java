package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;

public interface ListService {

    /**
     * 商品上架
     * @param skuLsInfo
     */
    public void saveSkuInfo(SkuLsInfo skuLsInfo);

    /**
     * 根据用户输入的检索条件查询数据
     * @param skuLsParams
     * @return
     */
    public SkuLsResult search(SkuLsParams skuLsParams);

    /**
     * 根据商品Id 更新热度
     * @param skuId
     */
    public void incrHotScore(String skuId);

}
