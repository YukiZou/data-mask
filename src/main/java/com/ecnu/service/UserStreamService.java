package com.ecnu.service;

import com.ecnu.model.UserStream;

import java.util.List;

/**
 * @author zou yuanyuan
 */
public interface UserStreamService {
    /**
     * 新增记录
     * @param userId
     * @param collectionName
     * @param topic
     * @return 记录的 id
     */
    int addUserStream(int userId, String collectionName, String topic);

    /**
     * 将 fields 存入指定的 user_stream 记录中。
     * @param id user_stream 主键
     * @param fields fields
     * @return
     */
    int updateUserStream(int id, String fields);

    /**
     * 根据 collectionName 找 user_stream 记录
     * collectionName 具有唯一性
     * @param collectionName
     * @return
     */
    UserStream queryUserStream(String collectionName);

    /**
     * 根据 id 查找记录
     * @param id
     * @return
     */
    UserStream queryUserStreamById(int id);

    /**
     * 根据 userId 查找记录
     * @param userId
     * @return
     */
    List<UserStream> queryUserStreamsByUserId(int userId);
}
