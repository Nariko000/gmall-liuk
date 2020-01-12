package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller
public class OrderController {

    @Reference
    private UserService userService;

    @Reference
    private CartService cartService;

    @Reference
    private OrderService orderService;

    @Reference
    private ManageService manageService;

    @LoginRequire
    @GetMapping("trade")
    public String trade(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        String tradeNo = orderService.getTradeNo(userId);
        List<UserAddress> userAddressList = userService.findUserAddressByUserId(userId);
        List<CartInfo> cartInfoList = cartService.cartgetCartCheckedList(userId);
        List<OrderDetail> detailArrayList = new ArrayList<>();
        for (CartInfo cartInfo : cartInfoList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            detailArrayList.add(orderDetail);
        }
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(detailArrayList);
        orderInfo.sumTotalAmount();
        request.setAttribute("tradeNo", tradeNo);
        request.setAttribute("userAddressList", userAddressList);
        request.setAttribute("detailArrayList", detailArrayList);
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());
        return "trade";
    }

    @LoginRequire
    @RequestMapping("submitOrder")
    public String submitOrder(OrderInfo orderInfo, HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        String outTradeNo="ATGUIGU"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setUserId(userId);
        orderInfo.setOutTradeNo(outTradeNo);
        String tradeNo = request.getParameter("tradeNo");
        boolean result = orderService.checkTradeCode(tradeNo, userId);
        if (!result){
            request.setAttribute("errMsg", "请勿重复提交订单！");
            return "tradeFail";
        }
        orderService.deleteTradeNo(userId);
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            boolean flag = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
            if (!flag){
                request.setAttribute("errMsg",orderDetail.getSkuName()+"库存不足，请联系客服！");
                return "tradeFail";
            }
            SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
            int res = orderDetail.getOrderPrice().compareTo(skuInfo.getPrice());
            if (res!=0){
                request.setAttribute("errMsg",orderDetail.getSkuName()+"商品价格有变动，请重新下单！");
                cartService.loadCartCache(userId);
                return "tradeFail";
            }
        }
        String orderId = orderService.saveOrderInfo(orderInfo);
        return "redirect://payment.gmall.com/index?orderId="+orderId;
    }

}
