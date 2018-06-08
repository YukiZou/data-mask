package com.ecnu.vo;

import com.ecnu.utils.enums.StatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zou yuanyuan
 */
@Data
public class FileDataRecordsVO implements Serializable{
    private String status;
    private List<FileDataRecordVO> fileDataRecords;

    public FileDataRecordsVO(StatusEnum statusEnum) {
        this.status = statusEnum.getStatus();
    }

    public FileDataRecordsVO(StatusEnum statusEnum, List<FileDataRecordVO> fileDataRecords) {
        this.status = statusEnum.getStatus();
        this.fileDataRecords = fileDataRecords;
    }
}
