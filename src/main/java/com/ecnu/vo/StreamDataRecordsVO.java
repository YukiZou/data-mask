package com.ecnu.vo;

import com.ecnu.utils.enums.StatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zou yuanyuan
 */
@Data
public class StreamDataRecordsVO implements Serializable {
    private String status;
    private List<StreamDataRecordVO> streamDataRecords;

    public StreamDataRecordsVO(StatusEnum statusEnum) {
        this.status = statusEnum.getStatus();
    }

    public StreamDataRecordsVO(StatusEnum statusEnum, List<StreamDataRecordVO> streamDataRecords) {
        this.status = statusEnum.getStatus();
        this.streamDataRecords = streamDataRecords;
    }
}
