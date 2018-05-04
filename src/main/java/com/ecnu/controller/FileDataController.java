package com.ecnu.controller;

import com.ecnu.dto.MaskConfigDTO;
import com.ecnu.dto.MaskConfigsDTO;
import com.ecnu.manage.FileDataManage;
import com.ecnu.manage.MaskExecuteManage;
import com.ecnu.manage.MaskMethodManage;
import com.ecnu.model.MaskConfig;
import com.ecnu.model.User;
import com.ecnu.model.UserFile;
import com.ecnu.utils.cookies.CookiesUtil;
import com.ecnu.utils.enums.StatusEnum;
import com.ecnu.vo.BaseResponse;
import com.ecnu.vo.FileUploadVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    private final static String LOGOUT = "logout";
    @Autowired
    private FileDataManage fileDataManage;

    @Autowired
    private MaskMethodManage maskMethodManage;

    @Autowired
    private MaskExecuteManage maskExecuteManage;


    /**
     * 解析到上传的文件数据并放入缓存中
     * 生成一条 user_file记录，将解析到的信息存入数据库表记录中
     * 返回字段List和脱敏方法List供前端显示进行脱敏配置
     * 返回部分原始数据供前端展示（展示<=10条的数据）
     * @param file
     * @param request
     * @return
     */
    @RequestMapping(value = "/file", method = RequestMethod.POST)
    @ResponseBody
    public FileUploadVO uploadFile(@RequestParam("file") CommonsMultipartFile file,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        try{
            HttpSession session = request.getSession(true);
            log.info("sessionID", session.getId());
            String loginStatus = (String) session.getAttribute("loginStatus");
            //当前session中的用户不存在或者已经登出，表示当前的session访问是无效的。
            if (loginStatus == null || LOGOUT.equals(loginStatus)) {
                Cookie cookie = CookiesUtil.loginStatusCookie("logout");
                response.addCookie(cookie);
                return new FileUploadVO(StatusEnum.NO_USER_FAIL);
            }
            User loginUser = (User) session.getAttribute("loginUser");
            //拿到文件中数据，包含标题行(rowList不会为空)
            List<String[]> rowList = fileDataManage.getFileData(file);
            if (rowList == null || rowList.size() <= 0) {
                log.error("数据文件格式不正确!");
                return new FileUploadVO(StatusEnum.FAIL);
            }
            //拿到部分原始数据并返回前端展示
            List<String[]> subRowList = fileDataManage.getSubRowList(rowList);

            //将数据存入session缓存中
            session.setAttribute("rowList", rowList);
            //原始标题行数据, 返回到前端
            List<String> fields = Arrays.asList(rowList.get(0));
            //拿到平台提供的脱敏方法。
            List<String> maskMethods = maskMethodManage.allMaskMethods();

            //插入userFile记录,并将记录的ID存入 session 中。
            int insertRecordId = fileDataManage.addUserFileRecord(loginUser.getId(), file, fields);
            if (insertRecordId <= 0) {
                log.error("插入usrFile记录出错");
                return new FileUploadVO(StatusEnum.FAIL);
            }
            session.setAttribute("userFileId", insertRecordId);

            log.info("数据文件解析成功。");
            return new FileUploadVO(StatusEnum.SUCCESS, fields, maskMethods, subRowList);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("数据文件上传失败!");
            return new FileUploadVO(StatusEnum.FAIL);
        }
    }

    /**
     * 脱敏执行方法入口
     * 通过参数拿到前端传回来的脱敏配置。
     * @param maskConfigsDTO
     * @param request
     * @return
     */
    @RequestMapping(value = "/masking", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse dataMaskExecute(@RequestBody MaskConfigsDTO maskConfigsDTO,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        try {
            List<MaskConfigDTO> maskConfigDTOs = maskConfigsDTO.getMaskConfigs();
            log.info("start masking with maskConfigs {}", maskConfigDTOs);
            //拿到原始数据
            HttpSession session = request.getSession();
            String loginStatus = (String) session.getAttribute("loginStatus");
            if (loginStatus == null || "".equals(loginStatus) || LOGOUT.equals(loginStatus)) {
                Cookie cookie = CookiesUtil.loginStatusCookie("logout");
                response.addCookie(cookie);
                return new BaseResponse(StatusEnum.NO_USER_FAIL);
            }
            //拿到之前生成的userFile record 记录。
            int userFileRecordId = (int) session.getAttribute("userFileId");
            UserFile queryUserFile = fileDataManage.findUserFileById(userFileRecordId);
            String fieldsStr = queryUserFile.getFields();
            // 拿到解析后的脱敏配置list.
            List<MaskConfig> maskConfigs = maskExecuteManage.getMaskConfigs(maskConfigDTOs, fieldsStr);
            if (maskConfigs == null || maskConfigs.size() == 0) {
                log.info("无合法的脱敏配置信息");
                return new BaseResponse(StatusEnum.NO_MASK_CONFIG);
            }
            //拿到有效的脱敏配置List中的 selectField list
            List<String> selectFields = maskExecuteManage.getSelectFields(maskConfigs);
            session.setAttribute("selectFields", selectFields);

            //根据 UserFile 对象中记录的字段值创建存储脱敏后数据的表。
            maskExecuteManage.createTable(queryUserFile.getTableName(), queryUserFile.getTableFields());

            //往表 fields_mask_status(id, user_file_id, selectFields, maskStatus)中插入数据
            maskExecuteManage.addFieldsMaskStatus(userFileRecordId, selectFields);

            //TODO 启动异步线程执行脱敏，在脱敏时，动态改变 fields_mask_status 中对应字段的脱敏状态
            //TODO 返回状态，让前端转到脱敏执行过程页面，并将 session中的 selectFields和 userFileId 返回到该过程页面
            // TODO 过程页面定时刷新获取数据库中表 fields_mask_status 记录的状态来查看整个脱敏过程。
            List<String[]> rowList = (List<String[]>) session.getAttribute("rowList");
            log.info("rowList {}", rowList);
            return new BaseResponse(StatusEnum.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse(StatusEnum.FAIL);
        }
    }

}
