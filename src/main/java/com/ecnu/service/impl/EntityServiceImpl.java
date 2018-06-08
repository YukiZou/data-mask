package com.ecnu.service.impl;

import com.ecnu.dao.EntityDao;
import com.ecnu.service.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author zou yuanyuan
 */
@Service
public class EntityServiceImpl implements EntityService {

    @Autowired
    EntityDao entityDao;

    @Override
    public Boolean createTable(String tableName, List<String> tableFields) {
        return entityDao.createTable(tableName, tableFields);
    }

    @Override
    public int insertRecord(String tableName, List<String> tableFields, List<String> record) {
        return entityDao.insertRecord(tableName, tableFields, record);
    }

    @Override
    public List<Map> getAllRecords(String tableName) {
        return entityDao.getAllRecords(tableName);
    }

}

