package com.ecnu.vo;

import com.ecnu.utils.enums.StatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 前端脱敏过程页面create时请求的接口返回的数据。
 * 包括session中的 userFileId 和 selectFields
 * @author zou yuanyuan
 */
@Data
public class MaskProcessVO implements Serializable{
    private String status;
    private int userFileId;
    private List<String> selectFields;

    public MaskProcessVO(StatusEnum statusEnum) {
        this.status = statusEnum.getStatus();
    }

    public MaskProcessVO(StatusEnum statusEnum, int userFileId, List<String> selectFields) {
        this.status = statusEnum.getStatus();
        this.userFileId = userFileId;
        this.selectFields = selectFields;
    }
}
