package com.ecnu.vo;

import com.ecnu.utils.enums.StatusEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 文件导出，返回文件的url(相对路径，如/files/testMaskedData.csv)
 * @author zou yuanyuan
 */
@Data
public class FileUrlVO implements Serializable{
    private String status;
    private String fileUrl;
    private String fileName;

    public FileUrlVO(StatusEnum statusEnum) {
        this.status = statusEnum.getStatus();
    }

    public FileUrlVO(StatusEnum statusEnum, String fileUrl, String fileName) {
        this.status = statusEnum.getStatus();
        this.fileUrl = fileUrl;
        this.fileName = fileName;
    }
}
