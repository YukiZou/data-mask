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
     * @param userId 当前会话用户的ID
     * @param tableName 生成的唯一的将要存储脱敏后数据的表名
     * @param fileName 原始文件名（不包含后缀）
     * @param tableFields 表的字段，用,分隔
     * @param fields 原始文件中的标题行标题，导出的时候用到
     * @return <= 0 代表新增出错，返回新增的记录的id
     */
    @Override
    public int addUserFile(int userId, String tableName, String fileName, String tableFields, String fields) {
        Boolean isNull = (tableName == null || "".equals(tableName) ||
                fileName == null || "".equals(fileName) ||
                tableFields == null || "".equals(tableFields) ||
                fields == null || "".equals(fields));
        if (isNull) {
            log.error("输入字段为空。");
            return 0;
        }
        UserFile userFile = new UserFile(userId, tableName, fileName, tableFields, fields);
        userFileDao.insertUserFile(userFile);
        return userFile.getId();
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
        if (userFiles == null || userFiles.size() == 0) {
            return null;
        } else {
            return userFiles.get(0);
        }
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

    /**
     * 根据id查找UserFile记录
     * @param id userFile的 id
     * @return userFile null 没有找到
     */
    @Override
    public UserFile queryUserFileById(int id) {
        UserFile userFile = new UserFile();
        userFile.setId(id);
        List<UserFile> userFiles = userFileDao.findUserFiles(userFile);
        if (userFiles == null || userFiles.size() == 0) {
            return null;
        } else {
            return userFiles.get(0);
        }
    }
}
