package com.ecnu.vo;

import com.ecnu.utils.enums.StatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zou yuanyuan
 */
@Data
public class StreamMaskedResultVO implements Serializable{
    private String status;
    private List<String> fields;
    private List<String[]> streamMaskedData;

    public StreamMaskedResultVO(StatusEnum statusEnum) {
        this.status = statusEnum.getStatus();
    }

    public StreamMaskedResultVO(StatusEnum statusEnum, List<String> fields, List<String[]> streamMaskedData) {
        this.status = statusEnum.getStatus();
        this.fields = fields;
        this.streamMaskedData = streamMaskedData;
    }
}
