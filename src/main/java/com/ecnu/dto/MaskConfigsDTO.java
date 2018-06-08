package com.ecnu.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zou yuanyuan
 */
@Data
public class MaskConfigsDTO implements Serializable{
    private List<MaskConfigDTO> maskConfigs;
}
