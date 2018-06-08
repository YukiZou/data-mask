package com.ecnu.vo;

import com.ecnu.utils.enums.StatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zou yuanyuan
 */
@Data
public class StreamMaskStatusVO implements Serializable{
    private String status;
    private List<PartitionOffsetsVO> partitionStatus;

    public StreamMaskStatusVO(StatusEnum statusEnum) {
        this.status =statusEnum.getStatus();
    }

    public StreamMaskStatusVO(StatusEnum statusEnum, List<PartitionOffsetsVO> partitionStatus) {
        this.status =statusEnum.getStatus();
        this.partitionStatus = partitionStatus;
    }

}
