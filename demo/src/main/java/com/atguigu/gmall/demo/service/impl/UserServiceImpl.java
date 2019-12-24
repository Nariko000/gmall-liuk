package com.atguigu.gmall.demo.service.impl;

import com.atguigu.gmall.demo.bean.UserInfo;
import com.atguigu.gmall.demo.mapper.UserInfoMapper;
import com.atguigu.gmall.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired(required = false)
    private UserInfoMapper userInfoMapper;

    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }
}
