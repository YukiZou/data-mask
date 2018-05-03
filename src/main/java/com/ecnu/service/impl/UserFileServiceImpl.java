package com.ecnu.service.impl;

import com.ecnu.dao.UserFileDao;
import com.ecnu.model.UserFile;
import com.ecnu.service.UserFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zou yuanyuan
 */
@Service
public class UserFileServiceImpl implements UserFileService {
    private static Logger log = LoggerFactory.getLogger(UserFileServiceImpl.class);
    @Autowired
    private UserFileDao userFileDao;

    /**
     * 新增 user_file 表记录
     * 前置条件： userId是当前登录用户ID，tableName不为空且唯一，tableFields不为空且是,分隔的字符串。
     * tableName 唯一性在调用这个服务之前确保。
     * @param userId
     * @param tableName
     * @param tableFields
     * @return <= 0 代表新增出错。
     */
    @Override
    public int addUserFile(int userId, String tableName, String tableFields) {
        Boolean isNull = (tableName == null || "".equals(tableName)
                            || tableFields == null || "".equals(tableFields));
        if (isNull) {
            log.error("tableName 或 tableFields 字段为空。");
            return 0;
        }

        UserFile userFile = new UserFile(userId, tableName, tableFields);
        return userFileDao.insertUserFile(userFile);
    }

    /**
     * 根据tableName查询符合条件的user_file记录。
     * tableName在user_file表中唯一
     * @param tableName
     * @return
     */
    @Override
    public UserFile queryUserFiles(String tableName) {
        UserFile userFile = new UserFile();
        userFile.setTableName(tableName);
        List<UserFile> userFiles = userFileDao.findUserFiles(userFile);
        return userFiles.get(0);
    }

    /**
     * 根据userId查询该用户的所有数据表记录。
     * @param userId
     * @return
     */
    @Override
    public List<UserFile> queryUserFiles(int userId) {
        UserFile userFile = new UserFile();
        userFile.setUserId(userId);
        List<UserFile> userFiles = userFileDao.findUserFiles(userFile);
        return userFiles;
    }
}
