package com.ecnu.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zou yuanyuan
 */
@Data
public class StreamExportFileDTO implements Serializable {
    private int userStreamId;
    private String fileType;
}
