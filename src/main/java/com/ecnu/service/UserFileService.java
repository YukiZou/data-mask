package com.ecnu.service;

import com.ecnu.model.UserFile;

import java.util.List;

/**
 * @author zou yuanyuan
 */
public interface UserFileService {
    /**
     * 新增 user_file 表记录
     * 前置条件： userId是当前登录用户ID，tableName不为空且唯一，tableFields不为空且是,分隔的字符串。
     * @param userId 当前会话用户的ID
     * @param tableName 生成的唯一的将要存储脱敏后数据的表名
     * @param fileName 原始文件名（不包含后缀）
     * @param tableFields 表的字段，用,分隔
     * @param fields 原始文件中的标题行标题，导出的时候用到
     * @return <= 0 代表新增出错，返回新增的记录的id
     */
    int addUserFile(int userId, String tableName, String fileName, String tableFields, String fields);

    /**
     * 根据tableName查询符合条件的user_file记录。
     * tableName在user_file表中唯一
     * @param tableName
     * @return
     */
    UserFile queryUserFiles(String tableName);

    /**
     * 根据userId查询该用户的所有数据表记录。
     * @param userId
     * @return
     */
    List<UserFile> queryUserFiles(int userId);

    /**
     * 根据id查找UserFile记录
     * @param id userFile的 id
     * @return userFile null 没有找到
     */
    UserFile queryUserFileById(int id);

}
