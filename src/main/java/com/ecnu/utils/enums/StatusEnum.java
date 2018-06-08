package com.ecnu.utils.enums;

/**
 * 返回状态
 * @author zou yuanyuan
 */

public enum StatusEnum {
    SUCCESS("success"),
    FAIL("fail"),
    NO_USER_FAIL("noUserFail"),
    PASSWORD_ERROR("passwordError"),
    DUPLICATE__FAIL("duplicateFail"),
    INPUT_FAIL("inputFail"),
    NO_MASK_CONFIG("noMaskConfig"),
    ENCRYPTION_ERROR("encryptionError"),
    PROPERTIES_ERROR("propertiesError"),
    NO_TOPIC("noTopic");


    private String status;

    StatusEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}
