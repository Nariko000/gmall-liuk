package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.service.PaymentService;
import com.atguigu.gmall.util.IdWorker;
import com.atguigu.gmall.util.StreamUtil;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WxPayController {

    @Value("${partnerkey}")
    private String partnerkey;

    @Reference
    private PaymentService paymentService;

    @RequestMapping("wx/submit")
    @ResponseBody
    public Map wxSubmit(){ // param:  String orderId
        IdWorker idWorker = new IdWorker();
        long id = idWorker.nextId();
        String orderId = ""+id;
        System.out.println("orderId:"+orderId);
        Map map = paymentService.createNative(orderId,"1");
        String code_url = (String) map.get("code_url");
        System.out.println(code_url);
        return map;
    }

    @RequestMapping("wx/callback/notify")
    @ResponseBody
    public String callBack(HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println("你回来啦！");
        ServletInputStream inputStream = request.getInputStream();
        String xmlString  = StreamUtil.inputStream2String(inputStream, "utf-8");
        if (WXPayUtil.isSignatureValid(xmlString,partnerkey)){
            Map<String, String> paramMap  = WXPayUtil.xmlToMap(xmlString);
            String result_code = paramMap.get("result_code");
            if (result_code!=null && "SUCCESS".equals(result_code)){
                HashMap<String, String> map = new HashMap<>();
                map.put("return_code","SUCCESS");
                map.put("return_msg","OK");
                String returnXml  = WXPayUtil.mapToXml(map);
                response.setContentType("text/xml");
                System.out.println("交易编号："+paramMap.get("out_trade_no")+"支付成功！");
                return  returnXml;
            }else {
                System.out.println("交易编号："+paramMap.get("out_trade_no")+"支付失败！");
                System.out.println("验签失败！");
                HashMap<String, String> map = new HashMap<>();
                map.put("return_code","FAIL");
                return WXPayUtil.mapToXml(map);
            }
        }else {
            System.out.println("验签失败！");
            HashMap<String, String> map = new HashMap<>();
            map.put("return_code","FAIL");
            return WXPayUtil.mapToXml(map);
        }
    }

}
