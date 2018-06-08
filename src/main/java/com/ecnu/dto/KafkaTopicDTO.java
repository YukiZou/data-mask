package com.ecnu.dto;

import lombok.Data;


/**
 * @author zou yuanyuan
 * 测试用：写入指定kafka topic 中的 主题名
 */
@Data
public class KafkaTopicDTO {
    private String topic;
}
