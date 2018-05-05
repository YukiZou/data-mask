package com.ecnu.vo;

import com.ecnu.utils.enums.StatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 返回字段的脱敏状态列表
 * @author zou yuanyuan
 */
@Data
public class MaskStatusVO implements Serializable {
    private String status;
    private List<String> maskStatus;

    public MaskStatusVO(StatusEnum statusEnum) {
        this.status = statusEnum.getStatus();
    }

    public MaskStatusVO(StatusEnum statusEnum, List<String> maskStatus) {
        this.status = statusEnum.getStatus();
        this.maskStatus = maskStatus;
    }
}
