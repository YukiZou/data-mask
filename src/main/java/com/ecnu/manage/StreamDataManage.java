package com.ecnu.manage;

import com.alibaba.fastjson.JSON;
import com.ecnu.model.UserStream;
import com.ecnu.service.UserStreamService;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 新增 user_stream 表记录
 * 查找所有已脱敏集合中的数据
 * @author zou yuanyuan
 */
@Service
public class StreamDataManage {
    private static Logger log = LoggerFactory.getLogger(StreamDataManage.class);
    private static final int LIMIT = 10;

    @Autowired
    private UserStreamService userStreamService;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 新增 user_stream 表记录的方法。(user_id + collection_name + topic)
     * @param userId user_id
     * @param topic topic
     * @return 返回新增的记录 id userStreamId
     */
    public int addUserStreamRecord(int userId, String topic) {
        // 构建集合名，要保证唯一性。
        String collectionName;
        UserStream queryUserStream;
        do {
            collectionName = createCollectionName(topic);
            queryUserStream = userStreamService.queryUserStream(collectionName);
        } while (queryUserStream != null);
        log.info("插入 userStream 记录 集合名 {}", collectionName);
        return userStreamService.addUserStream(userId, collectionName, topic);
    }

    /**
     * 根据 id 找 对应的 UserStream 对象
     * @param userStreamId
     * @return
     */
    public UserStream queryUserStream(int userStreamId) {
        return userStreamService.queryUserStreamById(userStreamId);
    }

    /**
     * 根据 collectionName查找记录
     * @param collectionName
     * @return
     */
    public UserStream queryUserStream(String collectionName) {
        return userStreamService.queryUserStream(collectionName);
    }

    /**
     * 根据 userId 查找记录
     * @param userId
     * @return
     */
    public List<UserStream> queryAllUserStreams(int userId) {
        return userStreamService.queryUserStreamsByUserId(userId);
    }

    /**
     * 去 collectionName 指定的集合中找到最新的脱敏记录，并返回给前端展示。
     * @param collectionName
     * @param fields
     * @return
     */
    public List<String[]> newestMaskStreamData(String collectionName, List<String> fields) {
        List<String[]> newestMaskedData = new ArrayList<>();
        Query query = new Query();
        // 按 _id 降序排列mongodb中数据
        query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "_id")));
        query.limit(LIMIT);
        List<String> queryData = mongoTemplate.find(query, String.class, collectionName);
        if (queryData != null) {
            // 将json字符串转化成map再转成 String[]存入数据结构中返回
            newestMaskedData = getStringArrayList(queryData, fields);
        }
        return newestMaskedData;
    }

    /**
     * 去 collectionName 指定的集合中找到所有的脱敏记录，并返回给前端展示。
     * @param collectionName
     * @param fields
     * @return
     */
    public List<String[]> allStreamMaskedData(String collectionName, List<String> fields) {
        List<String[]> allMaskedData = new ArrayList<>();
        List<String> queryData = mongoTemplate.findAll(String.class, collectionName);
        if (queryData != null) {
            // 将json字符串转化成map再转成 String[]存入数据结构中返回
            allMaskedData = getStringArrayList(queryData, fields);
        }
        return allMaskedData;
    }

    /**
     * 将 List<String> 转化成 List<String[]> 数据
     * @param rowJsons
     * @param fields
     * @return
     */
    private List<String[]> getStringArrayList(List<String> rowJsons, List<String> fields) {
        List<String[]> resData = new ArrayList<>();
        int size = fields.size();
        for (String rowJson : rowJsons) {
            String[] maskedRow = new String[size];
            Map map = JSON.parseObject(rowJson);
            for (int i = 0; i < size; i++) {
                String field = fields.get(i);
                if (map.containsKey(field)) {
                    Object value = map.get(field);
                    maskedRow[i] = ObjectUtils.toString(value);
                }
            }
            resData.add(maskedRow);
        }
        return resData;
    }

    /**
     * 根据 topic 构建集合名
     * @param topic
     * @return
     */
    private String createCollectionName(String topic) {
        //构建表,表名唯一
        Random random = new Random();
        String collectionName ="z" + topic + System.currentTimeMillis() + Math.abs(random.nextInt());
        log.info("集合名 collectionName {}", collectionName);
        return collectionName;
    }
}
