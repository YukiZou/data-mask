package com.ecnu.service.impl;

import com.ecnu.dao.UserDao;
import com.ecnu.model.User;
import com.ecnu.service.UserService;
import com.ecnu.utils.enums.StatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zou yuanyuan
 */
@Service
public class UserServiceImpl implements UserService {
    private static Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserDao userDao;

    /**
     * 用户注册接口
     * 参数检查： name password 字段不为空，且确保 name字段唯一，
     * @param name
     * @param password
     * @return
     */
    @Override
    public StatusEnum register(String name, String password) {
        Boolean isNull = (name == null || "".equals(name) || password == null || "".equals(password));
        if (isNull) {
            log.error("注册的用户名或密码为空，不合法。");
            return StatusEnum.INPUT_FAIL;
        }
        //保证用户名的唯一性
        User queryUser = userDao.findUserByName(name);
        if (queryUser != null) {
            log.error("用户名已存在，请重新注册新的用户名");
            return StatusEnum.DUPLICATE__FAIL;
        }
        User user = new User(name, password);
        int res = userDao.insertUser(user);
        if (res == 1) {
            log.info("新增用户成功");
            return StatusEnum.SUCCESS;
        } else {
            log.error("新增用户失败");
            return StatusEnum.FAIL;
        }
    }

    /**
     * 用户登录接口
     * 参数检查： name password 字段不为空。
     * @param name
     * @param password
     * @return
     */
    @Override
    public StatusEnum login(String name, String password) {
        Boolean isNull = (name == null || "".equals(name) || password == null || "".equals(password));
        if (isNull) {
            log.error("登录的用户名或密码为空，不合法。");
            return StatusEnum.INPUT_FAIL;
        }
        //用用户名查找该用户
        User queryUser = userDao.findUserByName(name);
        if (queryUser == null) {
            log.error("{} 用户不存在。", name);
            return StatusEnum.NO_USER_FAIL;
        }
        //密码匹配
        if (queryUser.getPassword().equals(password)) {
            log.info("{} 用户登录成功", name);
            return StatusEnum.SUCCESS;
        } else {
            log.error("{} 用户登录失败", name);
            return StatusEnum.PASSWORD_ERROR;
        }
    }

    /**
     * 根据用户名查询用户
     * 调用此服务前确保此用户存在且唯一。
     * @param name
     * @return
     */
    @Override
    public User queryUserByName(String name) {
        return userDao.findUserByName(name);
    }
}
