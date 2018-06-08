package com.ecnu.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zou yuanyuan
 */
@Data
public class OffsetRecord implements Serializable {
    private long startOffset;
    private long endOffset;
}
