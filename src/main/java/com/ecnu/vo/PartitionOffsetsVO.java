package com.ecnu.vo;

import com.ecnu.model.StreamMaskStatus;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zou yuanyuan
 */
@Data
public class PartitionOffsetsVO implements Serializable {
    private int partition;
    private String startOffset;
    private String endOffset;

    public PartitionOffsetsVO(StreamMaskStatus streamMaskStatus) {
        this.partition = streamMaskStatus.getPartition();
        this.startOffset = streamMaskStatus.getStartOffset();
        this.endOffset = streamMaskStatus.getEndOffset();
    }
}
