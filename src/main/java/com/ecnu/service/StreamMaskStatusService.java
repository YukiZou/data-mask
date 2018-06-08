package com.ecnu.service;

import com.ecnu.model.StreamMaskStatus;

import java.util.List;

/**
 * @author zou yuanyuan
 */
public interface StreamMaskStatusService {
    /**
     * 新增 stream_mask_status 记录 并返回新增的记录 id
     * @param userStreamId
     * @param partition
     * @return 记录 id
     */
    int addStreamMaskStatus(int userStreamId, int partition);

    /**
     * 更改 id 指定记录的 start_offset 和 end_offset
     * @param id
     * @param startOffset
     * @param endOffset
     * @return
     */
    int updateStreamMaskStatus(int id, long startOffset, long endOffset);

    /**
     * 根据 userStreamId 查找对应的记录
     * @param userStreamId
     * @return
     */
    List<StreamMaskStatus> findStreamMaskStatusByUserStreamId(int userStreamId);
}
