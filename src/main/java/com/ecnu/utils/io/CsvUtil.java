package com.ecnu.utils.io;

import com.csvreader.CsvWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 读取/生成csv文件工具类
 * @author zou yuanyuan
 */
public class CsvUtil {
    private static Logger logger = LoggerFactory.getLogger(CsvUtil.class);
    private final static String CSV = "csv";

    /**
     * 将数据写入指定文件的工具方法
     * @param filePath 要写的文件全路径
     * @param allRecords 要写入的数据
     * @param fields 要写入的字段名
     * @throws IOException
     */
    public static void writeCsv(String filePath, List<String[]> allRecords, List<String> fields) throws IOException {
        if (checkFilePath(filePath)) {
            // 创建CSV写对象 例如:CsvWriter(文件路径如"D://StemQ.csv"，分隔符，编码格式)
            CsvWriter csvWriter = new CsvWriter(filePath, ',', Charset.forName("UTF-8"));
            //写入表头，参数为String[]类型
            csvWriter.writeRecord(fields.toArray(new String[0]));
            //写内容
            int size = allRecords.size();
            for (int index = 0; index < size; index++) {
                csvWriter.writeRecord(allRecords.get(index));
            }
            csvWriter.close();
        }
    }

    public static List<String[]> readCsv(MultipartFile file) throws IOException {
        if (checkCsvFile(file)) {
            InputStream inputStream = file.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            //存储csv中的原始数据。
            List<String[]> rowList = new ArrayList<>();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                //把每一行的数据放在cells中
                String[] cells = line.split("[;,| \t]");
                rowList.add(cells);
            }
            bufferedReader.close();
            inputStream.close();
            return rowList;
        } else {
            logger.error("文件读取错误");
            throw new IOException(file.getName() + "文件读取错误");
        }
    }

    /**
     * 检查文件
     */
    private static boolean checkCsvFile(MultipartFile  file) throws IOException {
        if (file == null) {
            logger.error("文件不存在");
            throw new FileNotFoundException("文件不存在！");
        }
        // 获取文件名
        String filename = file.getOriginalFilename();
        logger.info("file name : {}", filename);
        // 判断是否为csv文件
        if (!filename.endsWith(CSV)) {
            logger.error(filename + "不是csv文件");
            throw new IOException(filename + "不是csv文件");
        }
        return true;
    }

    /**
     * 检查文件路径是否正确
     * @param filePath
     * @return
     */
    private static boolean checkFilePath(String filePath) throws IOException{
        if(filePath == null || filePath.equals("")) {
            logger.error("文件路径不存在");
            throw new IOException("文件路径不存在!");
        }
        if (!filePath.endsWith(CSV)) {
            logger.error(filePath + "不是csv文件");
            throw new IOException(filePath + "不是csv文件");
        }
        return true;
    }

}

