package com.ecnu.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 用户对流数据的脱敏记录
 */
@Data
public class UserStream implements Serializable {
    private int id;
    private int userId;
    private String collectionName;
    private String fields;
    private String topic;

    public UserStream() {

    }

    public UserStream(int id, String fields) {
        this.id = id;
        this.fields = fields;
    }

    public UserStream(int userId, String collectionName, String topic) {
        this.userId = userId;
        this.collectionName = collectionName;
        this.topic = topic;
    }
}
