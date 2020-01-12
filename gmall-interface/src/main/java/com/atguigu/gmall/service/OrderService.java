package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;

public interface OrderService {

    /**
     *  保存订单
     * @param orderInfo
     * @return
     */
    public String saveOrderInfo(OrderInfo orderInfo);

    /**
     * 生成流水号
     * @param userId
     * @return
     */
    public  String getTradeNo(String userId);

    /**
     * 验证流水号
     * @param tradeCodeNo
     * @param userId
     * @return
     */
    public  boolean checkTradeCode(String tradeCodeNo, String userId);

    /**
     * 删除流水号
     * @param userId
     */
    public void deleteTradeNo(String userId);

    /**
     * 验证库存
     * @param skuId
     * @param skuNum
     * @return
     */
    public boolean checkStock(String skuId, Integer skuNum);

    /**
     * 通过orderId 查询 OrderInfo
     * @param orderId
     * @return
     */
    public OrderInfo getOrderInfo(String orderId);
}
