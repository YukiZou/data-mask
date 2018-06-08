package com.ecnu.controller;

import com.ecnu.manage.FileDataManage;
import com.ecnu.manage.StreamDataManage;
import com.ecnu.model.User;
import com.ecnu.model.UserFile;
import com.ecnu.model.UserStream;
import com.ecnu.utils.cookies.CookiesUtil;
import com.ecnu.utils.enums.StatusEnum;
import com.ecnu.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * 历史脱敏记录的管理
 * @author zou yuanyuan
 */
@Controller
@RequestMapping(value = "/history")
public class HistoryRecordController {
    private static Logger log = LoggerFactory.getLogger(HistoryRecordController.class);
    private final static String LOGOUT = "logout";

    @Autowired
    private FileDataManage fileDataManage;

    @Autowired
    private StreamDataManage streamDataManage;

    /**
     * 根据当前登录用户的 id 查找对应的所有文件数据脱敏记录
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/file_data", method = RequestMethod.POST)
    @ResponseBody
    public FileDataRecordsVO queryAllFileDataRecords(HttpServletRequest request,
                                                     HttpServletResponse response) {
        try{
            HttpSession session = request.getSession();
            String loginStatus = (String) session.getAttribute("loginStatus");
            if (loginStatus == null || "".equals(loginStatus) || LOGOUT.equals(loginStatus)) {
                Cookie cookie = CookiesUtil.loginStatusCookie("logout");
                response.addCookie(cookie);
                return new FileDataRecordsVO(StatusEnum.NO_USER_FAIL);
            }
            // 更新 cookie
            Cookie cookie = CookiesUtil.loginStatusCookie(loginStatus);
            response.addCookie(cookie);

            User loginUser = (User) session.getAttribute("loginUser");
            List<UserFile> userFiles =  fileDataManage.queryAllUserFiles(loginUser.getId());
            List<FileDataRecordVO> fileDataRecordVOS = new ArrayList<>();
            for (UserFile userFile : userFiles) {
                FileDataRecordVO fileDataRecordVO = new FileDataRecordVO(userFile);
                fileDataRecordVOS.add(fileDataRecordVO);
            }
            return new FileDataRecordsVO(StatusEnum.SUCCESS, fileDataRecordVOS);

        } catch (Exception e) {
            e.printStackTrace();
            return new FileDataRecordsVO(StatusEnum.FAIL);
        }
    }

    /**
     * 根据当前登录用户的 id 查找对应的所有实时流数据脱敏记录
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/stream_data", method = RequestMethod.POST)
    @ResponseBody
    public StreamDataRecordsVO queryAllStreamDataRecords(HttpServletRequest request,
                                                       HttpServletResponse response) {
        try{
            HttpSession session = request.getSession();
            String loginStatus = (String) session.getAttribute("loginStatus");
            if (loginStatus == null || "".equals(loginStatus) || LOGOUT.equals(loginStatus)) {
                Cookie cookie = CookiesUtil.loginStatusCookie("logout");
                response.addCookie(cookie);
                return new StreamDataRecordsVO(StatusEnum.NO_USER_FAIL);
            }
            // 更新 cookie
            Cookie cookie = CookiesUtil.loginStatusCookie(loginStatus);
            response.addCookie(cookie);

            User loginUser = (User) session.getAttribute("loginUser");
            List<UserStream> userStreams =  streamDataManage.queryAllUserStreams(loginUser.getId());
            List<StreamDataRecordVO> streamDataRecordVOS = new ArrayList<>();
            for (UserStream userStream : userStreams) {
                StreamDataRecordVO streamDataRecordVO = new StreamDataRecordVO(userStream);
                streamDataRecordVOS.add(streamDataRecordVO);
            }
            return new StreamDataRecordsVO(StatusEnum.SUCCESS, streamDataRecordVOS);

        } catch (Exception e) {
            e.printStackTrace();
            return new StreamDataRecordsVO(StatusEnum.FAIL);
        }
    }

}
