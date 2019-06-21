package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.UserModel;

public interface UserService {

    //通过id获取用户对象的方法
    UserModel getUserById(Integer id);

    void register(UserModel userModel) throws BusinessException;

    /**
     *
     * @param telephone 用户注册手机
     * @param entryPassword  用户加密过后的密码
     * @return
     * @throws BusinessException
     */
    UserModel validateLogin(String telephone,String entryPassword) throws BusinessException;
}
