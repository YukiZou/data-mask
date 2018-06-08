package com.ecnu.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zou yuanyuan
 */
@Data
public class ExportFileDTO implements Serializable{
    private int userFileId;
    private String fileType;
}
