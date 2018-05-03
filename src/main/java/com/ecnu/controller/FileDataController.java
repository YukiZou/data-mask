package com.ecnu.controller;

import com.ecnu.manage.FileDataManage;
import com.ecnu.manage.MaskMethodManage;
import com.ecnu.utils.enums.StatusEnum;
import com.ecnu.vo.FileUploadVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

/**
 * @author zou yuanyuan
 */
@Controller
@RequestMapping(value = "/offline")
public class FileDataController {
    private static Logger log = LoggerFactory.getLogger(FileDataController.class);
    @Autowired
    private FileDataManage fileDataManage;

    @Autowired
    private MaskMethodManage maskMethodManage;

    @RequestMapping(value = "/file", method = RequestMethod.POST)
    @ResponseBody
    public FileUploadVO uploadFile(@RequestParam("file") CommonsMultipartFile file,
                                   HttpServletRequest request) {
        try{
            //拿到文件中数据，包含标题行
            List<String[]> rowList = fileDataManage.getFileData(file);
            if (rowList == null || rowList.size() <= 0) {
                log.error("数据文件格式不正确!");
                return new FileUploadVO(StatusEnum.FAIL);
            }
            HttpSession session = request.getSession();
            //将数据存入session缓存中
            session.setAttribute("rowList", rowList);
            //带后缀的文件名
            String fileName = fileDataManage.getFileName(file);
            //不带后缀的文件名
            String fileNameWithoutSuffix = fileDataManage.getFileNameWithoutSuffix(fileName);
            //构建数据库表名
            String tableName = fileDataManage.getTableName(fileNameWithoutSuffix);
            //原始标题行数据, 返回到前端
            List<String> fields = Arrays.asList(rowList.get(0));
            //数据库表字段: 因为原始的标题行数据可能会有中划线，中划线用mybatis的mapper插入会报错
            List<String> tableFields = fileDataManage.getTableFields(fields);
            //拿到平台提供的脱敏方法。
            List<String> maskMethods = maskMethodManage.allMaskMethods();
            log.info("数据文件解析成功。");
            return new FileUploadVO(StatusEnum.SUCCESS, fields, maskMethods);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("数据文件上传失败!");
            return new FileUploadVO(StatusEnum.FAIL);
        }
    }
}
