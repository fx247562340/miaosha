package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.UserDOMapper;
import com.miaoshaproject.dao.UserPassowrdDOMapper;
import com.miaoshaproject.dataobject.UserDO;
import com.miaoshaproject.dataobject.UserPassowrdDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPassowrdDOMapper userPassowrdDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Override
    public UserModel getUserById(Integer id) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if(userDO == null){
            return  null;
        }
        UserPassowrdDO userPassowrdDO = userPassowrdDOMapper.selectByUserId(userDO.getId());
        return convertFromDataObject(userDO,userPassowrdDO);
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if(userModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        ValidationResult validationResult = validator.validate(userModel);
        if(validationResult.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,validationResult.getErrMsg());
        }

        UserDO userDO = convertFromModel(userModel);
        try {
            userDOMapper.insertSelective(userDO);
        }catch (DuplicateKeyException ex){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"手机号已注册，请勿重复注册");
        }

        userModel.setId(userDO.getId());
        UserPassowrdDO userPassowrdDO = convertPasswordFromModel(userModel);
        userPassowrdDOMapper.insertSelective(userPassowrdDO);
        return;
    }

    @Override
    public UserModel validateLogin(String telephone, String entryPassword) throws BusinessException {
        //通过用户手机号获取用户信息
        UserDO userDO = userDOMapper.selectByTelephone(telephone);
        if(userDO == null){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        UserPassowrdDO userPassowrdDO = userPassowrdDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = convertFromDataObject(userDO, userPassowrdDO);

        //比对用户信息内加密的密码是否和传输进来的密码相匹配
        if(!StringUtils.equals(entryPassword,userModel.getEncrptPassword())){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }

        return userModel;
    }


    //实现model转dataObject方法
    public UserPassowrdDO convertPasswordFromModel(UserModel userModel){
        if(userModel == null){
            return  null;
        }
        UserPassowrdDO userPassowrdDO = new UserPassowrdDO();
        userPassowrdDO.setEncrptPassword(userModel.getEncrptPassword());
        userPassowrdDO.setUserId(userModel.getId());
        return userPassowrdDO;
    }

    public UserDO convertFromModel(UserModel userModel){
        if(userModel == null){
            return  null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel,userDO);
        return  userDO;
    }

    private UserModel convertFromDataObject(UserDO userDO, UserPassowrdDO userPassowrdDO){
        if(userDO == null){
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO,userModel);
        if(userPassowrdDO != null){
            userModel.setEncrptPassword(userPassowrdDO.getEncrptPassword());
        }
        return userModel;
    }
}
