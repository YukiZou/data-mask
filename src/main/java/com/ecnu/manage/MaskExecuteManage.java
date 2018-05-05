package com.ecnu.manage;

import com.ecnu.dto.MaskConfigDTO;
import com.ecnu.model.FieldsMaskStatus;
import com.ecnu.model.MaskConfig;
import com.ecnu.service.EntityService;
import com.ecnu.service.FieldsMaskStatusService;
import com.ecnu.service.impl.EncryptionServiceImpl;
import com.ecnu.utils.enums.MaskMethodEnum;
import com.ecnu.utils.enums.MaskStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析用户自定义的脱敏配置
 * @author zou yuanyuan
 */
@Service
public class MaskExecuteManage {
    private static Logger log = LoggerFactory.getLogger(MaskMethodManage.class);
    private final static ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    /**
     * 匹配正整数和正浮点数的正则表达式。
     */
    private static Pattern NUMBER_PATTERN = Pattern.compile("^[0-9]+([.][0-9]+)?$");

    @Autowired
    private EntityService entityService;

    @Autowired
    private FieldsMaskStatusService fieldsMaskStatusService;

    @Autowired
    private EncryptionServiceImpl encryptionService;

    /**
     * 将前端传过来的脱敏配置剔除不合法配置得到最终的可用的List<MaskConfig>
     * @param maskConfigDTOs 前端传过来的脱敏配置
     * @return
     */
    public List<MaskConfig> getMaskConfigs(List<MaskConfigDTO> maskConfigDTOs, String fieldsStr) {
        List<MaskConfig> maskConfigs = new ArrayList<>();
        List<String> fields = Arrays.asList(fieldsStr.split(","));
        for (MaskConfigDTO maskConfigDTO : maskConfigDTOs) {
            String selectField = maskConfigDTO.getSelectField();
            String selectMethod = maskConfigDTO.getSelectMethod();
            String parameterStr = maskConfigDTO.getParameter();
            double parameter = 0;
            //判断选择的脱敏规则、脱敏字段的有效性
            Boolean isNull = selectMethod == null || "".equals(selectMethod) || selectField == null || "".equals(selectField);
            if (isNull || !fields.contains(selectField)) {
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
                maskedData = encryptionService.executeCaesar(originData, (int)parameter);
                break;
            case RAIL_FENCE_ENCRYPTION:
                //栅栏置换，不能针对单个数据操作。有偏移量
                maskedData = encryptionService.executeRailFence(originData, (int)parameter);
                break;
            case MD5_ENCRYPTION:
                //MD5加密脱敏，会把所有类型的输入都变成定长的String输出。
                maskedData = encryptionService.executeMD5(originData);
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
                maskedData = encryptionService.executePaillier(originData);
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
