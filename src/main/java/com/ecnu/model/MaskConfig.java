package com.ecnu.model;

import com.ecnu.utils.enums.MaskMethodEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zou yuanyuan
 */
@Data
public class MaskConfig implements Serializable {
    private String selectField;
    private MaskMethodEnum selectMethod;
    private double parameter;

    public MaskConfig() {

    }

    public MaskConfig(String selectField, MaskMethodEnum selectMethod, double parameter) {
        this.selectField = selectField;
        this.selectMethod = selectMethod;
        this.parameter = parameter;
    }

}
