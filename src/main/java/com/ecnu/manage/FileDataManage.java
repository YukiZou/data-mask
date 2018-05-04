package com.ecnu.manage;

import com.ecnu.model.UserFile;
import com.ecnu.service.UserFileService;
import com.ecnu.utils.io.CsvUtil;
import com.ecnu.utils.io.POIExcelUtil;
import com.ecnu.utils.io.TxtUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 解析文件中的数据
 * 调用UserFileService 完成新增 user_file表记录。
 * @author zou yuanyuan
 */
@Service
public class FileDataManage {
    private static Logger log = LoggerFactory.getLogger(FileDataManage.class);
    private final static String TXT = "txt";
    private final static String CSV = "csv";
    private final static String XLS = "xls";
    private final static String XLSX = "xlsx";

    @Autowired
    private UserFileService userFileService;

    /**
     * 解析到文件中的待脱敏数据
     * @param file 用户上传的文件
     * @return 文件中的数据（包括标题行）
     * @throws IOException
     */
    public List<String[]> getFileData(MultipartFile file) throws IOException {
        List<String[]> rowList = new ArrayList<>();
        if (!file.isEmpty()) {
            log.info("start get file data");
            String filename = getFileName(file);
            // 判断文件类型
            if (filename.endsWith(TXT)) {
                rowList = TxtUtil.readTxt(file);
            } else if (filename.endsWith(CSV)) {
                rowList = CsvUtil.readCsv(file);
            } else if (filename.endsWith(XLS) || filename.endsWith(XLSX)) {
                rowList = POIExcelUtil.readExcel(file);
            }
        }
        return rowList;
    }

    /**
     * 拿到展示用的部分原始数据列表。
     * @param rowList 完整的文件数据
     * @return 部分文件数据
     */
    public List<String[]> getSubRowList(List<String[]> rowList) {
        List<String[]> subRowList = new ArrayList<>();
        int len = rowList.size();
        int maxSize = 11;
        for (int index = 1; index < len && index < maxSize; index++) {
            subRowList.add(rowList.get(index));
        }
        return subRowList;
    }

    /**
     * 将参数解析后调用UserFileService的方法往数据库表user_file中新增一条记录。
     * @param userId
     * @param file
     * @param fields
     * @return 新增的user_file记录的主键id
     */
    public int addUserFileRecord(int userId, MultipartFile file, List<String> fields) {
        //带后缀的文件名name
        String fileNameWithSuffix = getFileName(file);
        //不带后缀的文件名, 存入数据库
        String fileName = getFileName(fileNameWithSuffix);
        //数据库表字段: 因为原始的标题行数据可能会有中划线，中划线用mybatis的mapper插入会报错
        List<String> tableFields = getTableFields(fields);
        String tableFieldsStr = fieldsValue(tableFields);
        String fieldsStr = fieldsValue(fields);
        //构建数据库表名,要保证唯一性，如果不唯一，再次生成新的存储脱敏后数据的表名
        String tableName = getTableName(fileName);
        UserFile queryUserFile = userFileService.queryUserFiles(tableName);
        while (queryUserFile != null) {
            tableName = getTableName(fileName);
            queryUserFile = userFileService.queryUserFiles(tableName);
        }
        int userFileId = userFileService.addUserFile(userId, tableName, fileName, tableFieldsStr, fieldsStr);
        return userFileId;
    }

    /**
     * 根据userFileId 拿到整个的userFile记录
     * @param userFileId id
     * @return null if cannot find.
     */
    public UserFile findUserFileById (int userFileId) {
        UserFile userFile = userFileService.queryUserFileById(userFileId);
        return userFile;
    }

    /**
     * 拿到带后缀名的文件名
     * @param file
     * @return
     */
    private String getFileName(MultipartFile file) {
        String filename = file.getOriginalFilename();
        log.info("getFileName {}", filename);
        return filename;
    }

    /**
     * 拿到不带后缀名的文件名
     * @param filenameWithSuffix
     * @return
     */
    private String getFileName(String filenameWithSuffix) {
        if (filenameWithSuffix == null || "".equals(filenameWithSuffix)) {
            log.error("文件名为空");
            return "";
        }
        return StringUtils.substringBeforeLast(filenameWithSuffix, ".");
    }

    /**
     * 根据去除后缀的文件名构建存储脱敏后数据的mysql表名。
     * @param fileName
     * @return
     */
    private String getTableName(String fileName) {
        //构建表,表名唯一
        Random random = new Random();
        String tableName = fileName + System.currentTimeMillis() + Math.abs(random.nextInt());
        log.info("数据库表名 tableName {}", tableName);
        return tableName;
    }

    /**
     * 根据标题行数据生成数据库表字段
     */
    private List<String> getTableFields(List<String> fields) {
        if (fields == null || fields.size() <= 0) {
            log.error("文件标题行为空。");
            return null;
        }
        List<String> tableField = new ArrayList<>();
        //将字段中的中划线替换成下划线不然建表sql会出错。
        for (String field : fields) {
            if (field.contains("-")) {
                tableField.add(field.replace("-", "_"));
            } else {
                tableField.add(field);
            }
        }
        return tableField;
    }

    /**
     * 将List<String>的字段列表转化成以逗号分隔的String。
     * @param fields
     * @return
     */
    private String fieldsValue(List<String> fields) {
        if (fields == null) {
            return null;
        }
        String fieldsStr = String.join(",", fields);
        return fieldsStr;
    }
}
