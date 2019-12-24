package com.atguigu.gmall.demo.controller;

import com.atguigu.gmall.demo.bean.UserInfo;
import com.atguigu.gmall.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    //localhost:8080/finAll
    @RequestMapping("findAll")
    public List<UserInfo> findAll(){
        return userService.findAll();
    }

    //localhost:8080/findByUserInfo?id=Xxx
    @RequestMapping("findByUserInfo")
    public List<UserInfo> findByUserInfo(UserInfo userInfo){
        return userService.findByUserInfo(userInfo);
    }

    //localhost:8080/findByUser?loginName=Xxx
    @RequestMapping("findByUser")
    public List<UserInfo> findByUser(UserInfo userInfo){
        return userService.findByUser(userInfo);
    }

    //localhost:8080/login?loginName=Xxx&passwd=Xxx
    @RequestMapping("login")
    public UserInfo login(UserInfo userInfo){
        return userService.login(userInfo);
    }

    //localhost:8080/addUser?loginName=Xxx&nickName=Xxx&name=Xxx&passwd=Xxx
    @RequestMapping("addUser")
    public void addUser(UserInfo userInfo){
        userService.addUser(userInfo);
    }

    //localhost:8080/editUser?id=Xxx&loginName=Xxx
    @RequestMapping("editUser")
    public void editUser(UserInfo userInfo){
        userService.editUser(userInfo);
    }

    //localhost:8080/updUser?name=Xxx&loginName=Xxx
    @RequestMapping("updUser")
    public void updUser(UserInfo userInfo){
        userService.updUser(userInfo);
    }

    //localhost:8080/delUser?id=Xxx
    @RequestMapping("delUser")
    public void delUser(UserInfo userInfo){
        userService.delUser(userInfo);
    }

}
