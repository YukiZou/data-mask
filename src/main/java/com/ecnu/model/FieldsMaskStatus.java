package com.ecnu.model;

import lombok.Data;

import java.io.Serializable;

/**
 * maskStatus 只会出现 complete 和 processing
 * @author zou yuanyuan
 */
@Data
public class FieldsMaskStatus implements Serializable {
    private int id;
    private int userFileId;
    private String selectFields;
    private String maskStatus;

    public FieldsMaskStatus() {

    }

    public FieldsMaskStatus(int id, String maskStatus) {
        this.id = id;
        this.maskStatus = maskStatus;
    }

    public FieldsMaskStatus(int userFileId, String selectFields, String maskStatus) {
        this.userFileId = userFileId;
        this.selectFields = selectFields;
        this.maskStatus = maskStatus;
    }
}
