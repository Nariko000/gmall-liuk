package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;

import java.util.List;

public interface UserService {

    /**
     * 查询所有数据
     * @return
     */
    public List<UserInfo> findAll();

    /**
     * 根据userId获取UserAddress数据
     * @param userId
     * @return
     */
    public List<UserAddress> findUserAddressByUserId(String userId);

    /**
     * 登录
     * @param userInfo
     * @return
     */
    public UserInfo login(UserInfo userInfo);

    /**
     * 解密token 获取userId
     * @param userId
     * @return
     */
    public UserInfo varify(String userId);
}
