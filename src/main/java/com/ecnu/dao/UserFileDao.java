package com.ecnu.dao;

import com.ecnu.model.UserFile;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zou yuanyuan
 */
@Repository
public interface UserFileDao {
    /**
     * 插入一条userFile记录，表示该用户与指定的存储数据的表关联。
     * 前置条件：参数的 user_id 是当前登录用户的id, table_name唯一（之前不存在），tableFields不为空，用,分隔的字符串。
     * @param userFile
     * @return
     */
    int insertUserFile(UserFile userFile);

    /**
     * 根据参数userFile的字段信息匹配符合条件的记录返回。
     * @param userFile
     * @return
     */
    List<UserFile> findUserFiles(UserFile userFile);
}
