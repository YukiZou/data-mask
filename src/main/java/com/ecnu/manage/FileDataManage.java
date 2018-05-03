package com.ecnu.manage;

import com.ecnu.utils.io.CsvUtil;
import com.ecnu.utils.io.POIExcelUtil;
import com.ecnu.utils.io.TxtUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author zou yuanyuan
 */
@Service
public class FileDataManage {
    private static Logger log = LoggerFactory.getLogger(FileDataManage.class);
    private final static String TXT = "txt";
    private final static String CSV = "csv";
    private final static String XLS = "xls";
    private final static String XLSX = "xlsx";

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
     * 拿到带后缀名的文件名
     * @param file
     * @return
     */
    public String getFileName(MultipartFile file) {
        String filename = file.getOriginalFilename();
        log.info("getFileName {}", filename);
        return filename;
    }

    /**
     * 拿到不带后缀名的文件名
     * @param filename
     * @return
     */
    public String getFileNameWithoutSuffix(String filename) {
        if (filename == null || "".equals(filename)) {
            log.error("文件名为空");
            return "";
        }
        return StringUtils.substringBeforeLast(filename, ".");
    }

    /**
     * 根据去除后缀的文件名构建存储脱敏后数据的mysql表名。
     * @param fileNameWithoutSuffix
     * @return
     */
    public String getTableName(String fileNameWithoutSuffix) {
        //构建表,表名唯一
        Random random = new Random();
        String tableName = fileNameWithoutSuffix + System.currentTimeMillis() + Math.abs(random.nextInt());
        log.info("数据库表名 tableName {}", tableName);
        return tableName;
    }

    /**
     * 根据标题行数据生成数据库表字段
     */
    public List<String> getTableFields(List<String> fields) {
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
}
