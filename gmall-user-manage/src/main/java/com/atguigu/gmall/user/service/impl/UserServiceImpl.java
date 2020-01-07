package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    public String userKey_prefix="user:";

    public String userinfoKey_suffix=":info";

    public int userKey_timeOut=60*60*24*7;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> findUserAddressByUserId(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        return userAddressMapper.select(userAddress);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        String passwd = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        userInfo.setPasswd(passwd);
        UserInfo info = userInfoMapper.selectOne(userInfo);
        if (info != null){
            Jedis jedis = null;
            try {
                jedis = redisUtil.getJedis();
                String userKey = userKey_prefix + info.getId() + userinfoKey_suffix;
                jedis.setex(userKey, userKey_timeOut, JSON.toJSONString(info));
                return info;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (jedis != null){
                    jedis.close();
                }
            }
        }
        return null;
    }

    @Override
    public UserInfo varify(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String userKey = userKey_prefix + userId + userinfoKey_suffix;
        String userJson = jedis.get(userKey);
        if (!StringUtils.isEmpty(userJson)){
            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
            return userInfo;
        }
        return null;
    }
}
