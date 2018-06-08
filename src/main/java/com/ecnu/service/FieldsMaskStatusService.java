package com.ecnu.service;

import com.ecnu.model.FieldsMaskStatus;

import java.util.List;

/**
 * @author zou yuanyuan
 */
public interface FieldsMaskStatusService {
    /**
     * 新增 FieldsMaskStatus 记录
     * @param userFileId userFileId
     * @param selectFields 用户选择的脱敏字段
     * @param maskStatus 脱敏字段对应的脱敏状态
     * @return
     */
    int addFieldsMaskStatus(int userFileId, List<String> selectFields, List<String> maskStatus);

    /**
     * 更新 FieldsMaskStatus 记录
     * @param id FieldsMaskStatus id
     * @param newMaskStatus 新 maskStatus
     * @return
     */
    int updateFieldsMaskStatus(int id, String newMaskStatus);

    /**
     * 根据 userFileId 字段查找 FieldsMaskStatus 记录
     * @param userFileId
     * @return
     */
    FieldsMaskStatus findFieldsMaskStatusByUserFileId(int userFileId);
}
