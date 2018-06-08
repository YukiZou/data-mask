package com.ecnu.utils.cookies;

import javax.servlet.http.Cookie;

/**
 * @author zou yuanyuan
 */
public class CookiesUtil {
    public static Cookie usernameCookie(String username) {
        Cookie cookie = new Cookie("username", username);
        // 20 min 后过期
        cookie.setMaxAge(1200);
        cookie.setPath("/");
        return cookie;
    }

    public static Cookie loginStatusCookie(String loginStatus) {
        Cookie cookie = new Cookie("loginStatus", loginStatus);
        cookie.setMaxAge(1200);
        cookie.setPath("/");
        return cookie;
    }
}
