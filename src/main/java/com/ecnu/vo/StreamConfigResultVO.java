package com.ecnu.vo;

import com.ecnu.utils.enums.StatusEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 将 userStreamId 返回给前端
 * @author zou yuanyuan
 */
@Data
public class StreamConfigResultVO implements Serializable {
    private String status;
    private int userStreamId;

    public StreamConfigResultVO(StatusEnum statusEnum) {
        this.status = statusEnum.getStatus();
    }

    public StreamConfigResultVO(StatusEnum statusEnum, int userStreamId) {
        this.status = statusEnum.getStatus();
        this.userStreamId = userStreamId;
    }
}
