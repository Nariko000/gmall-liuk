package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PaymentInfo;

public interface PaymentService {

    /**
     * 保存记录 并生成二维码
     * @param paymentInfo
     * @return
     */
    public void savyPaymentInfo(PaymentInfo paymentInfo);

    /**
     * 更新交易记录中的状态
     * @param out_trade_no
     * @param paymentInfoUpd
     */
    public void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd);

    /**
     * 根据outTradeNo 查询数据
     * @param paymentInfo
     * @return
     */
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfo);

    /**
     * 退款
     * @param orderId
     * @return
     */
    public boolean refund(String orderId);
}
