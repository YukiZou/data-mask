package com.ecnu.dao;

import com.ecnu.model.User;
import org.springframework.stereotype.Repository;

/**
 * @author zou yuanyuan
 */
@Repository
public interface UserDao {

    /**
     * 用户注册
     * 前置条件：user参数中name 和 password 字段不为空，且name字段要求在用户表中唯一。
     * @param user
     * @return
     */
    int insertUser(User user);

    /**
     * 根据用户名精确查找用户对象。用户登录的时候调用，可以比较返回对象的password和输入的password是否相同。
     * @param name
     * @return null or one user
     */
    User findUserByName(String name);
}

