package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.config.CookieUtil;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class CartController {

    @Reference
    private CartService cartService;

    @Reference
    private ManageService manageService;

    @LoginRequire(autoRedirect = false)
    @RequestMapping("addToCart")
    public String addToCart(HttpServletRequest request, HttpServletResponse response){
        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");
        String userId = (String) request.getAttribute("userId");
        if (userId == null){
            userId = CookieUtil.getCookieValue(request, "user-key", false);
            if (userId == null){
                userId = UUID.randomUUID().toString().replace("-", "");
                CookieUtil.setCookie(request, response, "user-key", userId,7*24*3600, false );
            }
        }
        cartService.addToCart(skuId, userId, Integer.parseInt(skuNum));
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo", skuInfo);
        request.setAttribute("skuNum", skuNum);
        return "success";
    }

    @LoginRequire(autoRedirect = false)
    @RequestMapping("cartList")
    public String cartList(HttpServletRequest request){
        List<CartInfo> cartInfoList = new ArrayList<>();
        String userId = (String) request.getAttribute("userId");
        if (userId != null){
            String userTempId = CookieUtil.getCookieValue(request, "user-key", false);
            List<CartInfo> cartInfoNoLoginList = new ArrayList<>();
            if (!StringUtils.isEmpty(userTempId)){
                cartInfoNoLoginList = cartService.getCartList(userTempId);
                if (cartInfoNoLoginList != null && cartInfoNoLoginList.size() > 0){
                    cartInfoList = cartService.mergeToCartList(cartInfoNoLoginList, userId);
                    cartService.deleteCartList(userTempId);
                }
            }
            if (StringUtils.isEmpty(userTempId) || (cartInfoNoLoginList == null || cartInfoNoLoginList.size() == 0)){
                cartInfoList = cartService.getCartList(userId);
            }
        }else {
            String userTempId = CookieUtil.getCookieValue(request, "user-key", false);
            if (!StringUtils.isEmpty(userTempId)){
                cartInfoList = cartService.getCartList(userTempId);
            }
        }
        request.setAttribute("cartInfoList", cartInfoList);
        return "cartList";
    }

    @LoginRequire(autoRedirect = false)
    @RequestMapping("checkCart")
    @ResponseBody
    public void checkCart(HttpServletRequest request){
        String isChecked = request.getParameter("isChecked");
        String skuId = request.getParameter("skuId");
        String userId = (String) request.getAttribute("userId");
        if (userId == null){
            userId = CookieUtil.getCookieValue(request, "user-key", false);
        }
        cartService.checkCart(skuId, userId, isChecked);
    }

    @LoginRequire
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        String userTempId = CookieUtil.getCookieValue(request, "user-key", false);
        if (!StringUtils.isEmpty(userTempId)){
            List<CartInfo> cartInfoNoLoginList = cartService.getCartList(userTempId);
            if (cartInfoNoLoginList != null && cartInfoNoLoginList.size() > 0){
                cartService.mergeToCartList(cartInfoNoLoginList, userId);
                cartService.deleteCartList(userTempId);
            }
        }
        return "redirect://trade.gmall.com/trade";
    }

}
