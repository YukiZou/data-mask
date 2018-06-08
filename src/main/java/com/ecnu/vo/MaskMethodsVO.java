package com.ecnu.vo;

import com.ecnu.utils.enums.StatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zou yuanyuan
 */
@Data
public class MaskMethodsVO implements Serializable{
    private String status;
    private List<String> maskMethods;

    public MaskMethodsVO(StatusEnum statusEnum) {
        this.status = statusEnum.getStatus();
    }

    public MaskMethodsVO(StatusEnum statusEnum, List<String> maskMethods) {
        this.status = statusEnum.getStatus();
        this.maskMethods = maskMethods;
    }
}
