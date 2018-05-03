package com.ecnu.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户和脱敏后数据存储的表格关联表
 * @author asus
 */
@Data
public class UserFile implements Serializable{
    private int id;
    private int userId;

    /**
     * 存储脱敏后数据的表名，唯一
     */
    private String tableName;

    /**
     * 指定表的字段字符串，多个字段用,分隔，存储成一个字符串。
     */
    private String tableFields;

    /**
     *原始列表字段
     */
    private String fields;

    public UserFile() {

    }

    public UserFile (int userId, String tableName, String tableFields) {
        this.userId = userId;
        this.tableName = tableName;
        this.tableFields = tableFields;
    }

    public UserFile (int userId, String tableName, String tableFields, String fields) {
        this.userId = userId;
        this.tableName = tableName;
        this.tableFields = tableFields;
        this.fields = fields;
    }
}
