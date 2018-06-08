package com.ecnu.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zou yuanyuan
 */
@Data
public class UserDTO implements Serializable {
    private String name;
    private String password;
}
