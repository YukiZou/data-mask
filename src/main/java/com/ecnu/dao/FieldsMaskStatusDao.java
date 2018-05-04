package com.ecnu.dao;

import com.ecnu.model.FieldsMaskStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zou yuanyuan
 */
@Repository
public interface FieldsMaskStatusDao {
    /**
     * 插入一条记录
     * @param fieldsMaskStatus 字段 userFileId selectFields maskStatus 不为空
     * @return
     */
    int insertRecord(FieldsMaskStatus fieldsMaskStatus);

    /**
     * 更改参数的id指定的记录，实际上只会更改 maskStatus 字段值
     * 有值的字段表示新值（id字段除外）
     * @param fieldsMaskStatus id 和 maskStatus 一定要有值。
     * @return
     */
    int updateRecord(FieldsMaskStatus fieldsMaskStatus);

    /**
     * 根据参数的有值字段模糊匹配查找
     * 会用到根据 userFileId 字段查找
     * @param fieldsMaskStatus
     * @return
     */
    List<FieldsMaskStatus> findFieldsMaskStatus(FieldsMaskStatus fieldsMaskStatus);
}
