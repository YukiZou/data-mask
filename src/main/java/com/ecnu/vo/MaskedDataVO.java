package com.ecnu.vo;

import com.ecnu.utils.enums.StatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zou yuanyuan
 */
@Data
public class MaskedDataVO implements Serializable{
    private String status;
    private int userFileId;
    /**
     * 原始表的字段行字段
     */
    private List<String> fields;

    private List<String[]> maskedData;

    public MaskedDataVO(StatusEnum statusEnum) {
        this.status = statusEnum.getStatus();
    }

    public MaskedDataVO(StatusEnum statusEnum, int userFileId, List<String> fields, List<String[]> maskedData) {
        this.status = statusEnum.getStatus();
        this.userFileId = userFileId;
        this.fields = fields;
        this.maskedData = maskedData;
    }
}
