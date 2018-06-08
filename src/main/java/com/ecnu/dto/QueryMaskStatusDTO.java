package com.ecnu.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 前端轮询时传过来的参数解析对象
 * @author zou yuanyuan
 */
@Data
public class QueryMaskStatusDTO implements Serializable{
    private int userFileId;
}
