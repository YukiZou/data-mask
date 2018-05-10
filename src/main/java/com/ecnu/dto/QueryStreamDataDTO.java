package com.ecnu.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询最新脱敏结果数据传递过来的参数。
 * @author zou yuanyuan
 */
@Data
public class QueryStreamDataDTO implements Serializable {
    private String collectionName;
}
