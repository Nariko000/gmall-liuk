package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.cart.constant.CartConst;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Reference
    private ManageService manageService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        if (!jedis.exists(cartKey)){
            loadCartCache(userId);
        }
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId", userId).andEqualTo("skuId", skuId);
        CartInfo cartInfoExist = cartInfoMapper.selectOneByExample(example);
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        if (cartInfoExist != null){
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum() + skuNum);
            cartInfoExist.setSkuPrice(cartInfoExist.getCartPrice());
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
        }else {
            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setSkuId(skuId);
            cartInfo.setUserId(userId);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfoMapper.insertSelective(cartInfo);
            cartInfoExist = cartInfo;
        }
        jedis.hset(cartKey, skuId, JSON.toJSONString(cartInfoExist));
        setCartKeyExpire(userId, jedis, cartKey);
        jedis.close();
    }

    @Override
    public List<CartInfo> getCartList(String userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        List<String> stringList = jedis.hvals(cartKey);
        if(stringList != null && stringList.size() > 0){
            for (String cartInfoJson : stringList) {
                cartInfoList.add(JSON.parseObject(cartInfoJson, CartInfo.class));
            }
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o1.getId().compareTo(o2.getId());
                }
            });
            return cartInfoList;
        }else {
            cartInfoList = loadCartCache(userId);
            return cartInfoList;
        }
    }

    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartInfoArrayList, String userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        List<CartInfo> cartInfoListLogin = cartInfoMapper.selectCartListWithCurPrice(userId);
        if (cartInfoListLogin != null && cartInfoListLogin.size() > 0){
            for (CartInfo cartInfoNoLogin : cartInfoArrayList) {
                boolean isMatch = false;
                for (CartInfo cartInfoLogin : cartInfoListLogin) {
                    if (cartInfoNoLogin.getSkuId().equals(cartInfoLogin.getSkuId())){
                        cartInfoLogin.setSkuNum(cartInfoLogin.getSkuNum() + cartInfoNoLogin.getSkuNum());
                        cartInfoMapper.updateByPrimaryKeySelective(cartInfoLogin);
                        isMatch = true;
                    }
                }
                if (!isMatch){
                    cartInfoNoLogin.setSkuId(null);
                    cartInfoNoLogin.setUserId(userId);
                    cartInfoMapper.insertSelective(cartInfoNoLogin);
                }
            }
        }else {
            for (CartInfo cartInfo : cartInfoArrayList) {
                cartInfo.setId(null);
                cartInfo.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfo);
            }
        }
        cartInfoList = loadCartCache(userId);
        for (CartInfo cartInfoDB : cartInfoList){
            for (CartInfo cartInfo : cartInfoArrayList){
                if (cartInfoDB.getSkuId().equals(cartInfo.getSkuId())){
                    if ("1".equals(cartInfo.getIsChecked())){
                        if(!"1".equals(cartInfoDB.getIsChecked())){
                            cartInfo.setIsChecked("1");
                            checkCart(cartInfo.getIsChecked(), cartInfo.getSkuId(), userId);
                        }
                    }
                }
            }
        }
        return cartInfoList;
    }

    @Override
    public void deleteCartList(String userId) {
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId", userId);
        cartInfoMapper.deleteByExample(example);
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        jedis.del(cartKey);
        jedis.close();
    }

    @Override
    public void checkCart(String isChecked, String skuId, String userId) {
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId", userId).andEqualTo("skuId", skuId);
        CartInfo cartInfo = new CartInfo();
        cartInfo.setIsChecked(isChecked);
        System.out.println("修改数据------");
        cartInfoMapper.updateByExampleSelective(cartInfo, example);
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        jedis.hdel(cartKey, skuId);
        List<CartInfo> cartInfoList = cartInfoMapper.selectByExample(example);
        if (cartInfoList != null && cartInfoList.size() > 0){
            CartInfo cartInfoQuery = cartInfoList.get(0);
            cartInfoQuery.setSkuPrice(cartInfoQuery.getCartPrice());
            jedis.hset(cartKey, skuId, JSON.toJSONString(cartInfoQuery));
        }
        jedis.close();
    }

    private List<CartInfo> loadCartCache(String userId){
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
        if (cartInfoList == null || cartInfoList.size() == 0){
            return null;
        }
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        Map<String, String> map = new HashMap<>();
        for (CartInfo cartInfo : cartInfoList) {
            map.put(cartInfo.getSkuId(), JSON.toJSONString(cartInfo));
        }
        jedis.hmset(cartKey, map);
        jedis.close();
        return cartInfoList;
    }

    private void setCartKeyExpire(String userId, Jedis jedis, String cartKey){
        String userKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USERINFOKEY_SUFFIX;
        if (jedis.exists(userKey)){
            Long ttl = jedis.ttl(userKey);
            jedis.expire(cartKey, ttl.intValue());
        }else {
            jedis.expire(cartKey, 7*24*3600);
        }
    }

}
