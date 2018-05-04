package com.ecnu.utils.enums;

/**
 * 字段的脱敏状态
 * @author zou yuanyuan
 */
public enum MaskStatusEnum {
    INITIALIZE("initialize"),
    COMPLETE("complete"),
    PROCESSING("processing");


    private String maskStatus;

    MaskStatusEnum(String maskStatus) {
        this.maskStatus = maskStatus;
    }

    public String getMaskStatus() {
        return this.maskStatus;
    }
}
