package com.ecnu.model;

import lombok.Data;

import java.io.Serializable;

/**
 * userStreamId + partition 具有唯一性，表示指定topic下的指定partition.
 * @author zou yuanyuan
 */
@Data
public class StreamMaskStatus implements Serializable {
    private int id;
    private int userStreamId;
    private int partition;
    private String startOffset;
    private String endOffset;

    public StreamMaskStatus() {

    }

    public StreamMaskStatus(int userStreamId){
        this.userStreamId = userStreamId;
    }

    public StreamMaskStatus(int id, String startOffset, String endOffset) {
        this.id = id;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public StreamMaskStatus(int userStreamId, int partition) {
        this.userStreamId = userStreamId;
        this.partition = partition;
    }
}
