package com.atguigu.gmall.demo.service.impl;

import com.atguigu.gmall.demo.bean.UserInfo;
import com.atguigu.gmall.demo.mapper.UserInfoMapper;
import com.atguigu.gmall.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired(required = false)
    private UserInfoMapper userInfoMapper;

    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserInfo> findByUserInfo(UserInfo userInfo) {
        return userInfoMapper.select(userInfo);
    }

    @Override
    public List<UserInfo> findByUser(UserInfo userInfo) {
        Example example = new Example(UserInfo.class);
        example.createCriteria().andLike("loginName", "%" + userInfo.getLoginName() + "%");
        return userInfoMapper.selectByExample(example);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        return userInfoMapper.selectOne(userInfo);
    }

    @Override
    public void addUser(UserInfo userInfo) {
        System.out.println("===>" + userInfo.getId());
        userInfoMapper.insertSelective(userInfo);
        System.out.println("=====>" + userInfo.getId());
    }

    @Override
    public void editUser(UserInfo userInfo) {
        userInfoMapper.updateByPrimaryKeySelective(userInfo);
    }

    @Override
    public void updUser(UserInfo userInfo) {
        Example example = new Example(UserInfo.class);
        example.createCriteria().andEqualTo("name", userInfo.getName());
        userInfoMapper.updateByExampleSelective(userInfo, example);
    }

    @Override
    public void delUser(UserInfo userInfo) {
        userInfoMapper.delete(userInfo);
    }
}
