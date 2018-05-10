package com.ecnu.service.impl;

import com.ecnu.dao.UserStreamDao;
import com.ecnu.model.UserStream;
import com.ecnu.service.UserStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zou yuanyuan
 */
@Service
public class UserStreamServiceImpl implements UserStreamService {
    @Autowired
    private UserStreamDao userStreamDao;

    @Override
    public int addUserStream(int userId, String collectionName, String topic) {
        Boolean isNull = (collectionName == null || "".equals(collectionName) ||
                topic == null || "".equals(topic));
        if (isNull) {
            return 0;
        }
        UserStream userStream = new UserStream(userId, collectionName, topic);
        userStreamDao.insertUserStream(userStream);
        return userStream.getId();
    }

    @Override
    public int updateUserStream(int id, String fields) {
        UserStream userStream = new UserStream(id, fields);
        return userStreamDao.updateUserStream(userStream);
    }

    @Override
    public UserStream queryUserStream(String collectionName) {
        UserStream userStream = new UserStream();
        userStream.setCollectionName(collectionName);
        List<UserStream> userStreams = userStreamDao.findUserStreams(userStream);
        if (userStreams == null || userStreams.size() == 0) {
            return null;
        } else {
            return userStreams.get(0);
        }
    }

    @Override
    public UserStream queryUserStreamById(int id) {
        UserStream userStream = new UserStream();
        userStream.setId(id);
        List<UserStream> userStreams = userStreamDao.findUserStreams(userStream);
        if (userStreams == null || userStreams.size() == 0) {
            return null;
        } else {
            return userStreams.get(0);
        }
    }

    @Override
    public List<UserStream> queryUserStreamsByUserId(int userId) {
        UserStream userStream = new UserStream();
        userStream.setUserId(userId);
        return userStreamDao.findUserStreams(userStream);
    }
}
