package com.ecnu.vo;

import com.ecnu.model.User;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zou yuanyuan
 */
@Data
public class UserVO implements Serializable{
    private String status;
    private String name;

    public UserVO(String status) {
        this.status = status;
    }

    public UserVO(String status, User user) {
        this.status = status;
        this.name = user.getName();
    }
}
