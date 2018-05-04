package com.ecnu.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zou yuanyuan
 */
@Data
public class MaskConfigDTO implements Serializable{
    private String selectField;
    private String selectMethod;
    private String parameter;
}
