package com.ecnu.manage;

import com.alibaba.fastjson.JSON;
import com.ecnu.dto.MaskConfigDTO;
import com.ecnu.model.*;
import com.ecnu.service.EntityService;
import com.ecnu.service.FieldsMaskStatusService;
import com.ecnu.service.StreamMaskStatusService;
import com.ecnu.service.UserStreamService;
import com.ecnu.service.impl.EncryptionServiceImpl;
import com.ecnu.utils.enums.MaskMethodEnum;
import com.ecnu.utils.enums.MaskStatusEnum;
import com.ecnu.utils.enums.StatusEnum;
import jdk.net.SocketFlow;
import org.apache.commons.lang.ObjectUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.metrics.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import scala.util.parsing.combinator.testing.Str;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析用户自定义的脱敏配置
 * 启动本文数据的脱敏执行线程
 * 启动监听 kafka topic的异步线程
 * @author zou yuanyuan
 */
@Service
public class MaskExecuteManage {
    private static Logger log = LoggerFactory.getLogger(MaskMethodManage.class);
    private final static ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    public static volatile boolean exit = false;
    //TODO:为了测试暂时改成10条，正式的要处理比10条多的数据
    private final static int minBatchSize = 10;
    /**
     * 匹配正整数和正浮点数的正则表达式。
     */
    private static Pattern NUMBER_PATTERN = Pattern.compile("^[0-9]+([.][0-9]+)?$");

    @Autowired
    private EntityService entityService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserStreamService userStreamService;

    @Autowired
    private FieldsMaskStatusService fieldsMaskStatusService;

    @Autowired
    private StreamMaskStatusService streamMaskStatusService;

    @Autowired
    private EncryptionServiceImpl encryptionService;

    /**
     * 用户自定义的脱敏配置有效性验证函数
     * 文件数据的脱敏配置验证：调用此方法完成验证
     * 流数据的脱敏配置验证：除了调用此方法验证外，还需再根据实际数据的字段去验证selectFields是否匹配。
     * @param maskConfigDTOs 前端传过来的脱敏配置
     * @return
     */
    public List<MaskConfig> getMaskConfigs(List<MaskConfigDTO> maskConfigDTOs) {
        List<MaskConfig> maskConfigs = new ArrayList<>();
        for (MaskConfigDTO maskConfigDTO : maskConfigDTOs) {
            String selectField = maskConfigDTO.getSelectField();
            String selectMethod = maskConfigDTO.getSelectMethod();
            String parameterStr = maskConfigDTO.getParameter();
            double parameter = 0;
            //判断选择的脱敏规则、脱敏字段的有效性
            Boolean isNull = selectMethod == null || "".equals(selectMethod) || selectField == null || "".equals(selectField);
            if (isNull) {
                continue;
            }
            //将selectMethod转化成Enum类型的对象
            MaskMethodEnum maskMethod = MaskMethodEnum.getMaskMethodEnum(selectMethod);
            //如果当前选择的脱敏方法是需要参数的脱敏方法
            if (MaskMethodEnum.needParameterMethod(maskMethod)) {
                Boolean illegalParameter = (parameterStr == null || "".equals(parameterStr) || !isNumeric(parameterStr));
                if (illegalParameter) {
                    continue;
                }
                parameter = Double.parseDouble(parameterStr);
            }

            MaskConfig maskConfig = new MaskConfig(selectField, maskMethod, parameter);
            maskConfigs.add(maskConfig);
        }
        return maskConfigs;
    }

    /**
     * 根据user_file中存储的 tableName 和 tableFields 创建指定的 mysql 表
     * @param tableName 表名
     * @param tableFieldsStr 字段名，用, 分隔，需要解析成 List<String>类型
     * @return
     */
    public Boolean createTable(String tableName, String tableFieldsStr) {
        log.info("开始创建存储脱敏后数据的表 {}", tableName);
        List<String> tableFields = Arrays.asList(tableFieldsStr.split(","));
        return entityService.createTable(tableName, tableFields);
    }

    /**
     * 将参数插入表 fields_mask_status 中, maskStatus 根据 selectFields 个数生成对应的 status
     * @param userFileId session 中拿到的 user_file 表的 主键
     * @param selectFields 拿到有效配置中的 selectField list
     * @return
     */
    public int addFieldsMaskStatus(int userFileId, List<String> selectFields) {
        List<String> maskStatus = new ArrayList<>();
        int size = selectFields.size();
        for (int index = 0; index < size; index++) {
            maskStatus.add(MaskStatusEnum.INITIALIZE.getMaskStatus());
        }
        return fieldsMaskStatusService.addFieldsMaskStatus(userFileId, selectFields, maskStatus);
    }

    /**
     * 拿到 userFileId指定的记录中的 maskStatus
     * @param userFileId
     * @return
     */
    public List<String> queryMaskStatus(int userFileId) {
        FieldsMaskStatus fieldsMaskStatus = fieldsMaskStatusService.findFieldsMaskStatusByUserFileId(userFileId);
        String maskStatusStr = fieldsMaskStatus.getMaskStatus();
        return Arrays.asList(maskStatusStr.split(","));
    }

    /**
     * 拿到有效脱敏配置中的 selectField list
     * @param maskConfigs
     * @return
     */
    public List<String> getSelectFields(List<MaskConfig> maskConfigs) {
        List<String> selectFields = new ArrayList<>();
        for (MaskConfig maskConfig : maskConfigs) {
            selectFields.add(maskConfig.getSelectField());
        }
        return selectFields;
    }

    /**
     * 离线数据的脱敏执行方法。
     * @param maskConfigs 脱敏配置 list
     * @param rowList 文件中的数据， 包括标题行
     */
    public void fileDataMask(List<MaskConfig> maskConfigs, List<String[]> rowList, int userFileId, String tableName, String tableFieldsStr) {

        Callable<Boolean> callable = () -> {
            log.info("start mask data with userFiledId {}", userFileId);
            //拿到要存储的表的字段 list
            List<String> tableFields = Arrays.asList(tableFieldsStr.split(","));
            List<String> fields = Arrays.asList(rowList.get(0));
            int listSize = rowList.size();
            int rowSize = fields.size();
            int maskConfigSize = maskConfigs.size();

            encryptionService.setup();
            //遍历maskConfigs拿到用户的脱敏配置然后针对字段进行不同的脱敏操作
            for (int index = 0; index < maskConfigSize; index++) {
                MaskConfig maskConfig = maskConfigs.get(index);
                String selectField = maskConfig.getSelectField();
                //拿到当前选择的脱敏字段的列号col
                int col = -1;
                for(int i = 0; i < rowSize; i++) {
                    if (selectField.equals(fields.get(i))) {
                        col = i;
                        break;
                    }
                }
                if (col >= 0) {
                    //开始脱敏 selectField字段，将 fields_mask_status表中的对应状态更改成 processing
                    changeFieldMaskStatus(index, MaskStatusEnum.PROCESSING, userFileId);

                    // 拿到指定列的数据。
                    List<String> originData = new ArrayList<>();
                    for (int i = 1; i < listSize; i++) {
                        String[] row = rowList.get(i);
                        originData.add(row[col]);
                    }
                    //执行脱敏方法
                    List<String> maskedData = mask(originData, maskConfig.getSelectMethod(), maskConfig.getParameter());
                    //将脱敏后的字段信息再存进对象中去
                    for (int i = 1; i < listSize; i++) {
                        rowList.get(i)[col] = maskedData.get(i - 1);
                    }
                    //自此，针对某一列的数据脱敏处理完成。更改表中记录的脱敏状态
                    changeFieldMaskStatus(index, MaskStatusEnum.COMPLETE, userFileId);
                }
            }
            //完成所有的脱敏，将脱敏后数据存入数据库表中。
            for (int i = 1; i < listSize; i++) {
                List<String> record = Arrays.asList(rowList.get(i));
                //往数据库中插入记录
                int res = entityService.insertRecord(tableName, tableFields, record);
                if (res > 0) {
                    log.info("insert record {} success", record.toString());
                } else {
                    log.error("insert record {} fail", record.toString());
                }
            }
            return true;
        };
        //将 callable 接口放入异步线程中，并让线程池执行该线程。
        FutureTask<Boolean> fileDataMasking = new FutureTask<>(callable);
        EXECUTOR_SERVICE.submit(fileDataMasking);
    }

    /**
     * 根据参数配置 kafka consumer 属性
     * @param brokers
     * @param group
     * @return
     */
    public Properties consumerConfig(String brokers, String group) {
        Properties properties = new Properties();
        //指向Kafka集群的IP地址，以逗号分隔。
        properties.put("bootstrap.servers", brokers);
        //Consumer分组ID
        properties.put("group.id", group);
        //自动提交偏移量
        properties.put("enable.auto.commit", "false");
        properties.put("auto.commit.interval.ms", "1000");
        properties.put("session.timeout.ms", "30000");
        // 反序列化。Consumer把来自Kafka集群的二进制消息反序列化为指定的类型。
        // 因本例中的Producer使用的是String类型，所以调用StringDeserializer来反序列化
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return properties;
    }

    /**
     * 流数据消费、脱敏处理、存储方法
     * @param properties
     * @param topic
     * @param userStreamId
     * @param maskConfigs
     * @return
     */
    public StatusEnum streamDataMask(Properties properties, String topic, int userStreamId, List<MaskConfig> maskConfigs){
        try {
            //这个语句如果配置不对，会报错,不会进行后续操作。将报错传给前端。
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
            Map<String, List<PartitionInfo>> allTopics = consumer.listTopics();
            // 拿到当前 broker下的所有已存在的 topic,并判断用户输入的topic是否在其中。
            Set topicsSet = allTopics.keySet();
            if (!topicsSet.contains(topic)) {
                return StatusEnum.NO_TOPIC;
            }
            // 拿到当前 topic 的 partition 分区信息
            List<PartitionInfo> selectTopic = allTopics.get(topic);
            int largePartition = selectTopic.size();
            log.info("partition个数： {}", largePartition);
            // 根据分区信息新建 stream_mask_status 记录并将记录的 id 存到数组中。
            int[] streamMaskStatusIds = new int[largePartition];
            for (int index = 0; index < largePartition; index++) {
                streamMaskStatusIds[index] = streamMaskStatusService.addStreamMaskStatus(userStreamId, index);
            }
            // 根据partition数声明数据和offset存储的数据结构。
            List[] partitionData = new List[largePartition];
            OffsetRecord[] partitionOffset = new OffsetRecord[largePartition];
            for (int i = 0; i < largePartition; i++) {
                List<String> data = new ArrayList<>();
                partitionData[i] = data;
                OffsetRecord offsetRecord = new OffsetRecord();
                partitionOffset[i] = offsetRecord;
            }
            //拿到当前userStreamId 对应UserStream下的 collectionName
            UserStream userStream = userStreamService.queryUserStreamById(userStreamId);
            String collectionName = userStream.getCollectionName();
            //执行到这步，表明完成对用户的consumer配置的验证。对脱敏方法setup
            encryptionService.setup();

            Callable<Boolean> callable = () -> {
                String[] topics = {topic};
                consumer.subscribe(Arrays.asList(topics));
                List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
                Boolean firstMsg = true;
                String fieldsStr;
                while (!exit) {
                    // Consumer调用poll方法来轮循Kafka集群的消息, 如果kafka集群里没有消息，则最多等待100ms，没有消息这轮循环就结
                    ConsumerRecords<String, String> records = consumer.poll(100);
                    for (ConsumerRecord<String, String> record : records) {
                        // print the offset,key and value for the consumer records.
                        buffer.add(record);
                    }
                    log.info("buffer 大小 {}", buffer.size());

                    //一次消费到的消息达到批量处理的最小要求，则执行脱敏操作和存储操作，同步确认 offset
                    if (buffer.size() >= minBatchSize) {
                        //对buffer中的数据执行脱敏操作和存储操作。（这边要不要再起一个异步线程执行脱敏操作和存储操作）
                        //TODO: 如果同步执行，确保了数据不会丢失；如果异步执行，执行速率会很快，脱敏操作不影响消费操作。
                        // 拿到 topic中的各字段名并存储，同时二次验证脱敏配置的有效性。
                        if (firstMsg) {
                            String firstRow = buffer.get(0).value();
                            Map firstMap = JSON.parseObject(firstRow);
                            Set fieldsSet = firstMap.keySet();
                            fieldsStr = String.join(",", fieldsSet);
                            // 将 fieldsStr 存入 userStreamId 指定的 user_stream 记录中。
                            userStreamService.updateUserStream(userStreamId, fieldsStr);
                            // 用 fields验证脱敏配置中的字段是否正确。
                            for (MaskConfig maskConfig : maskConfigs) {
                                String selectField =maskConfig.getSelectField();
                                if (!fieldsSet.contains(selectField)) {
                                    maskConfigs.remove(maskConfig);
                                }
                            }
                            // 正确
                            log.info("配置筛选正确性验证 {}", maskConfigs);
                            firstMsg = false;
                        }
                        //ConsumerRecord(topic = testInfo, partition = 0, offset = 2, CreateTime = 1525771177900,
                        // serialized key size = -1, serialized value size = 46, headers = RecordHeaders(headers = [], isReadOnly = false),
                        // key = null, value = {"address":"Shanghai1","name":"Tom1","age":23})
                        // 按 partition 分区对数据进行脱敏处理和存储脱敏状态

                        // 1. 按分区整理 records中的value和 offset.
                        int bufferSize = buffer.size();
                        for (int bs = 0; bs < bufferSize; bs++) {
                            ConsumerRecord<String, String> consumerRecord = buffer.get(bs);
                            int partition = consumerRecord.partition();
                            long offset = consumerRecord.offset();
                            partitionData[partition].add(consumerRecord.value());
                            OffsetRecord offsetRecord = partitionOffset[partition];
                            if (bs == 0) {
                                offsetRecord.setStartOffset(offset);
                                offsetRecord.setEndOffset(offset);
                            }
                            if (offset > offsetRecord.getEndOffset()) {
                                offsetRecord.setEndOffset(offset);
                            }
                            if (offset < offsetRecord.getStartOffset()) {
                                offsetRecord.setStartOffset(offset);
                            }
                        }
                        // 2.更新 stream_mask_status表对应记录的 startOffset 和 endOffset。
                        for (int index = 0; index < largePartition; index++) {
                            streamMaskStatusService.updateStreamMaskStatus(streamMaskStatusIds[index], partitionOffset[index].getStartOffset(), partitionOffset[index].getEndOffset());
                        }

                        // 3. 对流数据分区执行脱敏操作。index 表示当前分区号
                        for (int index = 0; index < largePartition; index++) {
                            // 3.1 将指定分区的数据变成List<Map>结构存储
                            List<Map> originData = new ArrayList<>();
                            List<String> partitionOriginData = partitionData[index];
                            for (String rowJson : partitionOriginData) {
                                Map map = JSON.parseObject(rowJson);
                                originData.add(map);
                            }
                            // 3.2 遍历maskConfigs 对每个分区内的数据所选字段执行脱敏操作
                            for (MaskConfig maskConfig : maskConfigs) {
                                String selectField = maskConfig.getSelectField();
                                //拿到需要脱敏的列数据 originCol
                                List<String> originCol = new ArrayList<>();
                                for (Map row : originData) {
                                    if (row.containsKey(selectField)) {
                                        Object object = row.get(selectField);
                                        originCol.add(ObjectUtils.toString(object));
                                    }
                                }
                                // 执行脱敏, maskedCol正确
                                List<String> maskedCol = mask(originCol, maskConfig.getSelectMethod(), maskConfig.getParameter());
                                log.info("脱敏后列数据 {}", maskedCol);
                                //将脱敏后字段再存入原数据结构map中,替换该字段脱敏前的原始值
                                int size = originData.size();
                                for (int j = 0; j < size; j++) {
                                    Map map = originData.get(j);
                                    if (map.containsKey(selectField)) {
                                        String maskedCell = maskedCol.get(j);
                                        map.put(selectField, maskedCell);
                                    }
                                }
                            }
                            //该分区的脱敏操作结束，目前originData里面存储的是脱敏后的分区数据
                            // 4 将脱敏后分区数据加上分区号存入 mongoDB指定集合中
                            for (Map map : originData) {
                                map.put("partition", index);
                                mongoTemplate.insert(map, collectionName);
                            }
                        }
                        //标识当前消费到的所有数据脱敏完成。手动提交 offset。
                        consumer.commitSync();
                        buffer.clear();
                    }
                }
                log.info("exist consumer listener");
                return null;
            };
            //将 callable 接口放入异步线程中，并让线程池执行该线程。
            FutureTask<Boolean> streamDataMasking = new FutureTask<>(callable);
            EXECUTOR_SERVICE.submit(streamDataMasking);
            return StatusEnum.SUCCESS;
        } catch (KafkaException ke) {
            log.info(ke.getMessage());
            return StatusEnum.PROPERTIES_ERROR;
        } catch (NoSuchAlgorithmException e) {
            log.info(e.getMessage());
            return StatusEnum.ENCRYPTION_ERROR;
        }
    }

    /**
     * 根据 userStreamId 查询对应的partition 的 offsets
     * @param userStreamId
     * @return
     */
    public List<StreamMaskStatus> queryStreamMaskStatus(int userStreamId) {
        return streamMaskStatusService.findStreamMaskStatusByUserStreamId(userStreamId);
    }

    /**
     * 停止 or 可以重新启动消费者线程。
     * @param stop true 退出 false 可以重新启动新的消费者线程
     */
    public void stopListener(Boolean stop) {
        exit = stop;
        log.info("启动线程前 exit 值 {}", exit);
    }

    /**
     * 判断String型的参数是否是整形或者浮点型
     * @param str
     * @return
     */
    private Boolean isNumeric(String str) {
        Matcher isNum = NUMBER_PATTERN.matcher(str);
        if (isNum.matches()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 将指定列号的字段脱敏状态改成 maskStatusEnum 指定的状态
     * @param index 下标
     * @param maskStatusEnum 状态， 如processing complete
     * @param userFileId 表的记录 id
     */
    private void changeFieldMaskStatus(int index, MaskStatusEnum maskStatusEnum, int userFileId) {
        //找到对应的记录
        FieldsMaskStatus queryFieldsMaskStatus = fieldsMaskStatusService.findFieldsMaskStatusByUserFileId(userFileId);
       //拿到 maskStatus
        String maskStatusStr = queryFieldsMaskStatus.getMaskStatus();
        List<String> maskStatus = Arrays.asList(maskStatusStr.split(","));
        //更新 maskStatus
        maskStatus.set(index, maskStatusEnum.getMaskStatus());
        maskStatusStr = String.join(",", maskStatus);
        //更新表记录
        fieldsMaskStatusService.updateFieldsMaskStatus(queryFieldsMaskStatus.getId(), maskStatusStr);
    }

    /**
     * 调用对应的脱敏方法对指定列数据进行脱敏
     * @param originData 待脱敏数据列
     * @param method 指定的脱敏方法
     * @param parameter 脱敏方法的参数
     * @return
     * @throws Exception
     */
    private List<String> mask (List<String> originData, MaskMethodEnum method, double parameter) throws Exception {
        List<String> maskedData = new ArrayList<>();
        switch (method) {
            case CAESAR_ENCRYPTION:
                //凯撒置换，不能针对单个数据操作。有偏移量
                long currentTime = System.nanoTime();
                maskedData = encryptionService.executeCaesar(originData, (int)parameter);
                long after = System.nanoTime();
                log.info("凯撒置换耗时：{}", after - currentTime);
                break;
            case RAIL_FENCE_ENCRYPTION:
                //栅栏置换，不能针对单个数据操作。有偏移量
                currentTime = System.nanoTime();
                maskedData = encryptionService.executeRailFence(originData, (int)parameter);
                after = System.nanoTime();
                log.info("栅栏置换耗时：{}", after - currentTime);
                break;
            case MD5_ENCRYPTION:
                //MD5加密脱敏，会把所有类型的输入都变成定长的String输出。
                currentTime = System.nanoTime();
                maskedData = encryptionService.executeMD5(originData);
                after = System.nanoTime();
                log.info("MD5耗时：{}", after - currentTime);
                break;
            case EPSILON_DIFFERENTIAL_PRIVACY:
                //差分隐私，只能处理数字类型的输入input: double; output: double，有偏移量
                maskedData = encryptionService.executeEpsilonDifferentialPrivacy(originData, parameter);
                break;
            case AES_ENCRYPTION:
                //可处理各种类型输入，输出为定长32位字符串。
                maskedData = encryptionService.executeAES(originData);
                break;
            case RSA_ENCRYPTION:
                //RSA加密算法，输出256位的String
                maskedData = encryptionService.executeRSA(originData);
                break;
            case FORMAT_PRESERVING_ENCRYPTION:
                //保形加密, 只能处理由小写字母组成的字符串
                maskedData = encryptionService.executeFormatPreserving(originData);
                break;
            case HOMOMORPHISM_ENCRYPTION:
                //用Paillier算法实现的同态加密，只能处理int/long/只有数字组成的大整数 这些输入
                currentTime = System.nanoTime();
                maskedData = encryptionService.executePaillier(originData);
                after = System.nanoTime();
                log.info("同态加密耗时：{}", after - currentTime);
                break;
            case ANONYMITY_ENCRYPTION:
                //k-匿名算法
                maskedData = encryptionService.executeAnonymity(originData, (int)parameter);
                break;
            default:
                break;
        }
        return maskedData;
    }

}
