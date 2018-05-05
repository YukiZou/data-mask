package com.ecnu.service;

import java.util.List;
import java.util.Map;

/**
 * 创建表，插入数据，读取出所有数据
 * @author zou yuanyuan
 */
public interface EntityService {
    /**
     * 创建表
     * @param tableName
     * @param tableFields
     * @return
     */
    Boolean createTable(String tableName, List<String> tableFields);

    /**
     * 向表中插入数据
     * @param tableName
     * @param tableFields
     * @param record
     * @return
     */
    int insertRecord(String tableName, List<String> tableFields, List<String> record);

    /**
     * 拿到指定表的所有数据。
     * @param tableName
     * @return
     */
    //List<Map> getAllRecords(String tableName, List<String> tableFields);
    List<Map> getAllRecords(String tableName);
}
