package com.ecnu.vo;

import com.ecnu.utils.enums.StatusEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zou yuanyuan
 */
@Data
public class BaseResponse implements Serializable {
    private String status;

    public BaseResponse() {

    }

    public BaseResponse(StatusEnum statusEnum) {
        this.status = statusEnum.getStatus();
    }
}
