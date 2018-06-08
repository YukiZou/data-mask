package com.ecnu.dao;

import com.ecnu.model.StreamMaskStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zou yuanyuan
 */
@Repository
public interface StreamMaskStatusDao {
    /**
     * 插入一条记录
     * @param streamMaskStatus userStreamId partition 不为空
     * @return
     */
    int insertRecord(StreamMaskStatus streamMaskStatus);

    /**
     * 更改参数的id指定的记录，实际上只会更改 startOffset endOffset 字段值
     * @param streamMaskStatus id startOffset endOffset 有值
     * @return
     */
    int updateRecord(StreamMaskStatus streamMaskStatus);

    /**
     * 根据参数字段值模糊查找记录
     * 会用到根据 userStreamId （+ partition）查找
     * @param streamMaskStatus
     * @return
     */
    List<StreamMaskStatus> findStreamMaskStatus(StreamMaskStatus streamMaskStatus);
}
