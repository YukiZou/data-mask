package com.ecnu.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 根据String tableName List<String> tableFields 创建 mysql 表
 * 根据String tableName List<String> tableFields List<String> record insert 数据到指定表中
 * 根据String tableName List<String> tableFields 拿到指定表中的所有数据。
 * @author zou yuanyuan
 */
@Repository
public interface EntityDao {
    /**
     * 创建 名为 tableName 的 mysql 表
     * @param tableName 要创建的表名
     * @param tableFields 要创建的表的字段
     * @return
     */
    Boolean createTable(@Param("tableName") String tableName, @Param("tableFields") List<String> tableFields);

    /**
     * insert List<String> record 到 tableName 指定的表中
     * @param tableName  要插入数据的表名
     * @param tableFields 要插入数据的表的字段
     * @param record 要插入的数据， List<String>
     * @return
     */
    int insertRecord(@Param("tableName") String tableName, @Param("tableFields") List<String> tableFields, @Param("record") List<String> record);

    /**
     * 拿到 tableName 指定的表中的所有数据
     * @param tableName 表名
     * @return
     */
    //List<Map> getAllRecords(@Param("tableName") String tableName, @Param("tableFields") List<String> tableFields);

    List<Map> getAllRecords(@Param("tableName") String tableName);
}
