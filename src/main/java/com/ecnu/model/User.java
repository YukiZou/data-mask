package com.ecnu.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 脱敏平台的用户类
 * @author zou yuanyuan
 */
@Data
public class User implements Serializable{
    private int id;
    private String name;
    private String password;

    public User() {

    }
    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }
}
