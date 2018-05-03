package com.ecnu.vo;

import com.ecnu.utils.enums.StatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zou yuanyuan
 */
@Data
public class FileUploadVO implements Serializable {
    private String status;
    /**
     * 原始表的字段行字段
     */
    private List<String> fields;
    /**
     * 返回平台提供的脱敏方法
     */
    private List<String> maskMethods;

    public FileUploadVO() {

    }

    public FileUploadVO(StatusEnum statusEnum) {
        this.status = statusEnum.getStatus();
    }

    public FileUploadVO(StatusEnum statusEnum, List<String> fields, List<String > maskMethods) {
        this.status = statusEnum.getStatus();
        this.fields = fields;
        this.maskMethods = maskMethods;
    }
}
