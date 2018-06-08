package com.ecnu.vo;

import com.ecnu.model.UserStream;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zou yuanyuan
 */
@Data
public class StreamDataRecordVO implements Serializable {
    private int id;
    private String collectionName;
    private String topic;

    public StreamDataRecordVO() {

    }

    public StreamDataRecordVO(UserStream userStream) {
        this.id = userStream.getId();
        this.collectionName = userStream.getCollectionName();
        this.topic = userStream.getTopic();
    }
}
