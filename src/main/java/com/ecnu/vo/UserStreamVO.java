package com.ecnu.vo;

import com.ecnu.utils.enums.StatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 前端流数据脱敏过程展示页面 create时请求返回的数据，
 * @author zou yuanyuan
 */
@Data
public class UserStreamVO implements Serializable{
    private String status;
    private String collectionName;
    private String topic;

    public UserStreamVO(StatusEnum statusEnum) {
        this.status = statusEnum.getStatus();
    }

    public UserStreamVO(StatusEnum statusEnum, String collectionName, String topic) {
        this.status = statusEnum.getStatus();
        this.collectionName = collectionName;
        this.topic = topic;
    }
}
