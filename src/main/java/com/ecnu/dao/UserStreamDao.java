package com.ecnu.dao;

import com.ecnu.model.UserStream;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zou yuanyuan
 */
@Repository
public interface UserStreamDao {
    /**
     * 插入一条记录
     * 参数的 userId collectionName topic 字段有值
     * @param userStream
     * @return
     */
    int insertUserStream(UserStream userStream);

    /**
     * 参数的 id 字段存在且有意义。
     * 主要用来改 fields字段的值，因为一开始在insert对应记录时不知道fields
     * @param userStream
     * @return
     */
    int updateUserStream(UserStream userStream);

    /**
     * 根据参数userStream的字段信息匹配符合条件的记录返回。
     * @param userStream
     * @return
     */
    List<UserStream> findUserStreams(UserStream userStream);
}
