package com.ecnu.utils.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 读取Txt文件工具类
 * @author zou yuanyuan
 */
public class TxtUtil {
    private static Logger logger = LoggerFactory.getLogger(TxtUtil.class);
    private final static String TXT = "txt";

    public static List<String[]> readTxt(MultipartFile file) throws IOException {
        if (checkTxtFile(file)) {
            InputStream inputStream = file.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(reader);
            //存储txt中的原始数据。
            List<String[]> rowList = new ArrayList<>();

            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                //把每一行的数据放在cells中
                String[] cells = line.split("[;,| \t]");
                rowList.add(cells);
            }
            bufferedReader.close();
            reader.close();
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
    private static boolean checkTxtFile(MultipartFile  file) throws IOException {
        if (file == null) {
            logger.error("文件不存在");
            throw new FileNotFoundException("文件不存在！");
        }

        // 获取文件名
        String filename = file.getOriginalFilename();
        logger.info("file name : {}", filename);
        // 判断是否为txt文件
        if (!filename.endsWith(TXT)) {
            logger.error(filename + "不是txt文件");
            throw new IOException(filename + "不是txt文件");
        }
        return true;
    }


    /**
     * 将数据写入指定文件的工具方法
     * @param fileName 要写的文件全路径
     * @param allRecords 要写入的数据
     * @param field 要写入的字段名(本来是应该传入原始字段，但是为了简单，传入表字段)
     * @throws IOException
     */
    public static void writeTxt(String fileName, List<Map> allRecords, List<String> field) throws IOException {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
            for (Map map : allRecords) {

                String line = "";
                for (String fie : field) {
                    String value = (String) map.get(fie);
                    line = line.concat(value);
                    line = line.concat("\t");
                }
                bw.write(line);
                bw.newLine();
                bw.flush();
            }
            bw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

