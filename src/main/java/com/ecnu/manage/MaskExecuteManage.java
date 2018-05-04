package com.ecnu.manage;

import com.ecnu.dto.MaskConfigDTO;
import com.ecnu.model.MaskConfig;
import com.ecnu.service.EntityService;
import com.ecnu.service.FieldsMaskStatusService;
import com.ecnu.utils.enums.MaskMethodEnum;
import com.ecnu.utils.enums.MaskStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析用户自定义的脱敏配置
 * @author zou yuanyuan
 */
@Service
public class MaskExecuteManage {
    private static Logger log = LoggerFactory.getLogger(MaskMethodManage.class);
    /**
     * 匹配正整数和正浮点数的正则表达式。
     */
    private static Pattern NUMBER_PATTERN = Pattern.compile("^[0-9]+([.][0-9]+)?$");

    @Autowired
    private EntityService entityService;

    @Autowired
    private FieldsMaskStatusService fieldsMaskStatusService;

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

}
