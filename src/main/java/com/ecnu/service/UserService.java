package com.ecnu.service;

import com.ecnu.model.User;
import com.ecnu.utils.enums.StatusEnum;

/**
 * @author zou yuanyuan
 */
public interface UserService {

    /**
     * 用户注册接口
     * 参数检查： name password 字段不为空，且确保 name字段唯一， password字段可在前端加密完再传过来.
     * //TODO: 非法输入的限制，避免用户注入脚本。
     * @param name
     * @param password
     * @return
     */
    StatusEnum register(String name, String password);

    /**
     * 用户登录接口
     * 参数检查： name password 字段不为空。
     * @param name
     * @param password
     * @return
     */
    StatusEnum login(String name, String password);

    /**
     * 根据用户名查询用户对象
     * @param name
     * @return
     */
    User queryUserByName(String name);
}
