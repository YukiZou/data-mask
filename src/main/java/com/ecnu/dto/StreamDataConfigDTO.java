package com.ecnu.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zou yuanyuan
 */
@Data
public class StreamDataConfigDTO implements Serializable {
    private String brokers;
    private String topic;
    private String group;

    private List<MaskConfigDTO> maskConfigs;
}
