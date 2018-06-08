package com.ecnu.service.impl;

import com.ecnu.dao.StreamMaskStatusDao;
import com.ecnu.model.StreamMaskStatus;
import com.ecnu.service.StreamMaskStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zou yuanyuan
 */
@Service
public class StreamMaskStatusServiceImpl implements StreamMaskStatusService{
    @Autowired
    private StreamMaskStatusDao streamMaskStatusDao;

    @Override
    public int addStreamMaskStatus(int userStreamId, int partition) {
        StreamMaskStatus streamMaskStatus = new StreamMaskStatus(userStreamId, partition);
        streamMaskStatusDao.insertRecord(streamMaskStatus);
        return streamMaskStatus.getId();
    }

    @Override
    public int updateStreamMaskStatus(int id, long startOffset, long endOffset) {
        String startOffsetStr = Long.toString(startOffset);
        String endOffsetStr = Long.toString(endOffset);
        StreamMaskStatus streamMaskStatus = new StreamMaskStatus(id, startOffsetStr, endOffsetStr);
        return streamMaskStatusDao.updateRecord(streamMaskStatus);
    }

    @Override
    public List<StreamMaskStatus> findStreamMaskStatusByUserStreamId(int userStreamId) {
        StreamMaskStatus streamMaskStatus = new StreamMaskStatus(userStreamId);
       return streamMaskStatusDao.findStreamMaskStatus(streamMaskStatus);
    }
}
