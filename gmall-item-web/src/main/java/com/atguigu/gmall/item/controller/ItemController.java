package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Reference
    private ManageService manageService;

    @Reference
    private ListService listService;

//    @LoginRequire
    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, HttpServletRequest request){
        System.out.println(skuId);
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrListCheckBySku(skuInfo);
//        List<SkuSaleAttrValue> skuSaleAttrValueListBySpu = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());
//        String key = "";
//        Map<String, String> map = new HashMap<>();
//        if (skuSaleAttrValueListBySpu != null && skuSaleAttrValueListBySpu.size()>0){
//            for (int i = 0; i < skuSaleAttrValueListBySpu.size(); i++) {
//                SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);
//                if (key.length() > 0){
//                    key += "|";
//                }
//                key += skuSaleAttrValue.getSaleAttrValueId();
//                if((i+1) == skuSaleAttrValueListBySpu.size() || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueListBySpu.get(i+1).getSkuId())){
//                    map.put(key, skuSaleAttrValue.getSkuId());
//                    key = "";
//                }
//            }
//        }
//        String valuesSkuJson = JSON.toJSONString(map);
        Map skuValueIdsMap = manageService.getSkuValueIdsMap(skuInfo.getSpuId());
        String valuesSkuJson = JSON.toJSONString(skuValueIdsMap);
        request.setAttribute("skuInfo", skuInfo);
        request.setAttribute("spuSaleAttrList", spuSaleAttrList);
        request.setAttribute("valuesSkuJson", valuesSkuJson);
        listService.incrHotScore(skuId);
        return "item";
    }

}
