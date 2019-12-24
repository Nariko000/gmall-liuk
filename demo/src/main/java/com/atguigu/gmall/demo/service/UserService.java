package com.atguigu.gmall.demo.service;

import com.atguigu.gmall.demo.bean.UserInfo;

import java.util.List;

public interface UserService {

    /**
     * SELECT * FROM user_info
     * 查询所有用户数据
     * @return
     */
    public List<UserInfo> findAll();

    /**
     * SELECT * FROM user_info WHERE ? = ?
     * 根据条件查询用户数据
     * @param userInfo
     * @return
     */
    public List<UserInfo> findByUserInfo(UserInfo userInfo);

    /**
     * SELECT * FROM user_info WHERE login_name LIKE '%?%'
     * 根据loginName模糊查询用户数据
     * @param userInfo
     * @return
     */
    public List<UserInfo> findByUser(UserInfo userInfo);

    /**
     * SELECT * FROM user_info WHERE login_name = ? AND passwd = ?
     * 登录
     * @param userInfo
     * @return
     */
    public UserInfo login(UserInfo userInfo);

    /**
     * INSERT INTO user_info VALUES(?, ?, ?, ?, ?)
     * 注册
     * @param userInfo
     */
    public void addUser(UserInfo userInfo);

    /**
     * UPDATE user_info SET ? = ? WHERE id = ?
     * 根据ID更新用户数据
     * @param userInfo
     */
    public void editUser(UserInfo userInfo);

    /**
     * UPDATE user_info SET login_Name = ? WHERE name = ?
     * 根据name修改login_name
     * @param userInfo
     */
    public void updUser(UserInfo userInfo);

    /**
     * DELETE FROM user_info WHERE ? = ?
     * 根据条件删除用户数据
     * @param userInfo
     */
    public void delUser(UserInfo userInfo);
}
