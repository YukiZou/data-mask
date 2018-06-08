package com.ecnu.controller;

import com.ecnu.dto.UserDTO;
import com.ecnu.model.User;
import com.ecnu.service.UserService;
import com.ecnu.utils.cookies.CookiesUtil;
import com.ecnu.utils.enums.StatusEnum;
import com.ecnu.vo.BaseResponse;
import com.ecnu.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 用户登录、注册管理
 * @author zou yuanyuan
 */
@Controller
@RequestMapping("/user")
public class UserController {
    private static Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * 用户登录，登录成功，返回用户json字符串；登录不成功，返回null（这边需要改进）
     * 将成功登录的用户User对象放入session中
     * @param userDTO
     * @return
     */
    @RequestMapping(value = "/login", method= RequestMethod.POST)
    @ResponseBody
    public UserVO userLogin(@RequestBody UserDTO userDTO, HttpServletRequest request, HttpServletResponse response) {
        try {
            log.info("user {} login", userDTO.toString());
            StatusEnum statusEnum = userService.login(userDTO.getName(), userDTO.getPassword());
            log.info("user {} register status {}", userDTO.getName(), statusEnum.getStatus());
            switch (statusEnum) {
                case SUCCESS:
                    User loginUser = userService.queryUserByName(userDTO.getName());
                    //获取session, true表示如果没有，则新建一个session
                    HttpSession session = request.getSession(true);
                    //将loginUser存入session中，让其他方法可以访问到。
                    session.setAttribute("loginUser", loginUser);
                    session.setAttribute("loginStatus", "login");
                    log.info("session id {} isLogin {}", session.getId(), session.getAttribute("loginStatus"));
                    Cookie cookie = CookiesUtil.loginStatusCookie("login");
                    response.addCookie(cookie);
                    Cookie cookieUserName = CookiesUtil.usernameCookie(loginUser.getName());
                    response.addCookie(cookieUserName);
                    return new UserVO(statusEnum.getStatus(), loginUser);
                default:
                    return new UserVO(statusEnum.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("用户登录失败!");
            return new UserVO(StatusEnum.FAIL.getStatus());
        }
    }

    /**
     * 用户注销
     * 没有销毁session,而是把 session 和 cookie 中的 loginStatus 设置成 logout
     * @param request
     * @return
     */
    @RequestMapping(value = "/logout", method= RequestMethod.POST)
    @ResponseBody
    public BaseResponse userLogout(HttpServletRequest request, HttpServletResponse response) {
        try {
            log.info("start logout!");
            HttpSession session = request.getSession();
            // session.invalidate();
            session.setAttribute("loginStatus", "logout");
            Cookie cookie = CookiesUtil.loginStatusCookie("logout");
            response.addCookie(cookie);
            return new BaseResponse(StatusEnum.SUCCESS);
        } catch (Exception e) {
            log.error("logout error!");
            return new BaseResponse(StatusEnum.FAIL);
        }

    }

    /**
     * 用户注册
     * @param userDTO
     * @return
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse userRegister(@RequestBody UserDTO userDTO, HttpServletResponse response) {
        try {
            String name = userDTO.getName();
            String password = userDTO.getPassword();
            StatusEnum statusEnum = userService.register(name, password);
            log.info("user {} register status {}", name, statusEnum.getStatus());
            Cookie cookie = CookiesUtil.usernameCookie(name);
            response.addCookie(cookie);
            return new BaseResponse(statusEnum);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("注册失败");
            return new BaseResponse(StatusEnum.FAIL);
        }
    }

}
