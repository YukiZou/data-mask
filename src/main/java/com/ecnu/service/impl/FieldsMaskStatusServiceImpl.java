package com.ecnu.service.impl;

import com.ecnu.dao.FieldsMaskStatusDao;
import com.ecnu.model.FieldsMaskStatus;
import com.ecnu.service.FieldsMaskStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zou yuanyuan
 */
@Service
public class FieldsMaskStatusServiceImpl implements FieldsMaskStatusService{
    @Autowired
    private FieldsMaskStatusDao fieldsMaskStatusDao;

    @Override
    public int addFieldsMaskStatus(int userFileId, List<String> selectFields, List<String> maskStatus) {
        String selectFieldsStr = String.join(",", selectFields);
        String maskStatusStr = String.join(",", maskStatus);
        FieldsMaskStatus fieldsMaskStatus = new FieldsMaskStatus(userFileId, selectFieldsStr, maskStatusStr);
        return fieldsMaskStatusDao.insertRecord(fieldsMaskStatus);
    }

    @Override
    public int updateFieldsMaskStatus(int id, String newMaskStatus) {
        FieldsMaskStatus fieldsMaskStatus = new FieldsMaskStatus(id, newMaskStatus);
        return fieldsMaskStatusDao.updateRecord(fieldsMaskStatus);
    }

    @Override
    public FieldsMaskStatus findFieldsMaskStatusByUserFileId(int userFileId) {
        FieldsMaskStatus fieldsMaskStatus = new FieldsMaskStatus();
        fieldsMaskStatus.setUserFileId(userFileId);
        List<FieldsMaskStatus> queryRecordList = fieldsMaskStatusDao.findFieldsMaskStatus(fieldsMaskStatus);
        if (queryRecordList == null || queryRecordList.size() == 0) {
            return null;
        } else {
            return queryRecordList.get(0);
        }
    }
}
