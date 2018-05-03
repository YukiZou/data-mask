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
     * @param userId
     * @param tableName
     * @param tableFields
     * @return
     */
    int addUserFile(int userId, String tableName, String tableFields);

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

}
