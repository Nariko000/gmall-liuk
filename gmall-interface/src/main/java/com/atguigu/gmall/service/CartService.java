package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {

    /**
     * 添加购物车 用户Id，商品Id，商品数量。
     * @param skuId
     * @param userId
     * @param skuNum
     */
    public void  addToCart(String skuId,String userId,Integer skuNum);

    /**
     * 根据用户Id查询购物车列表
     * @param userId
     * @return
     */
    public List<CartInfo> getCartList(String userId);

    /**
     * 合并购物车: cartTempList 未登录的购物车, 根据userId查询购物车
     * @param cartTempList
     * @param userId
     * @return
     */
    public List<CartInfo> mergeToCartList(List<CartInfo> cartTempList, String userId);

    /**
     * 根据userId删除购物车
     * @param userId
     */
    public void deleteCartList(String userId);

    /**
     * 选中状态变更
     * @param isChecked
     * @param skuId
     * @param userId
     */
    public void checkCart(String isChecked, String skuId, String userId);

}
