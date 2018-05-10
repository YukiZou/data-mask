package com.ecnu.controller;

import com.ecnu.dto.*;
import com.ecnu.manage.FileDataManage;
import com.ecnu.manage.MaskExecuteManage;
import com.ecnu.manage.MaskMethodManage;
import com.ecnu.manage.StreamDataManage;
import com.ecnu.model.MaskConfig;
import com.ecnu.model.StreamMaskStatus;
import com.ecnu.model.User;
import com.ecnu.model.UserStream;
import com.ecnu.utils.cookies.CookiesUtil;
import com.ecnu.utils.enums.StatusEnum;
import com.ecnu.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * 流数据脱敏处理
 * @author zou yuanyuan
 */
@Controller
@RequestMapping(value = "/stream")
public class StreamDataController {
    private static Logger log = LoggerFactory.getLogger(StreamDataController.class);
    private final static String LOGOUT = "logout";

    @Autowired
    private MaskMethodManage maskMethodManage;

    @Autowired
    private MaskExecuteManage maskExecuteManage;

    @Autowired
    private StreamDataManage streamDataManage;

    @Autowired
    private FileDataManage fileDataManage;

    /**
     * 查询系统提供的脱敏方法列表
     * @param request get session id
     * @param response set cookie
     * @return maskMethods
     */
    @RequestMapping(value = "/mask_methods", method = RequestMethod.POST)
    @ResponseBody
    public MaskMethodsVO queryMaskMethods(HttpServletRequest request,
                                          HttpServletResponse response) {
        try {
            HttpSession session = request.getSession();
            String loginStatus = (String) session.getAttribute("loginStatus");
            if (loginStatus == null || "".equals(loginStatus) || LOGOUT.equals(loginStatus)) {
                Cookie cookie = CookiesUtil.loginStatusCookie("logout");
                response.addCookie(cookie);
                return new MaskMethodsVO(StatusEnum.NO_USER_FAIL);
            }
            List<String> maskMethods = maskMethodManage.allMaskMethods();
            return new MaskMethodsVO(StatusEnum.SUCCESS, maskMethods);
        } catch (Exception e) {
            e.printStackTrace();
            return new MaskMethodsVO(StatusEnum.FAIL);
        }
    }

    /**
     * 触发针对流数据的消费线程接口
     * 针对传入的consumer config 和 maskConfigs 做有效性判断。
     * 触发线程监听kafka topic 消费消息并及时脱敏处理和存入mongodb的collection中。
     * @param streamDataConfig consumer config and maskConfigs
     * @param request sessionId
     * @param response set cookie
     * @return 消费的状态和userStreamId 返回。
     */
    @RequestMapping(value = "/config", method = RequestMethod.POST)
    @ResponseBody
    public StreamConfigResultVO executeConfig(@RequestBody StreamDataConfigDTO streamDataConfig,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        try {
            HttpSession session = request.getSession();
            String loginStatus = (String) session.getAttribute("loginStatus");
            if (loginStatus == null || "".equals(loginStatus) || LOGOUT.equals(loginStatus)) {
                Cookie cookie = CookiesUtil.loginStatusCookie("logout");
                response.addCookie(cookie);
                return new StreamConfigResultVO(StatusEnum.NO_USER_FAIL);
            }
            maskExecuteManage.stopListener(false);
            User loginUser = (User) session.getAttribute("loginUser");
            List<MaskConfigDTO> maskConfigDTOs = streamDataConfig.getMaskConfigs();
            String brokers = streamDataConfig.getBrokers();
            String topic = streamDataConfig.getTopic();
            String group = streamDataConfig.getGroup();

            // 检查自定义脱敏配置的有效性，之后还需验证selectField是否在数据中存在
            List<MaskConfig> maskConfigs = maskExecuteManage.getMaskConfigs(maskConfigDTOs);
            if (maskConfigs == null || maskConfigs.size() == 0) {
                log.info("无合法的脱敏配置信息");
                return new StreamConfigResultVO(StatusEnum.NO_MASK_CONFIG);
            }
            //新增user_stream 记录，方法中会根据 topicName 构造唯一的存储脱敏后数据的集合名 collectionName
            int userStreamId = streamDataManage.addUserStreamRecord(loginUser.getId(), topic);
            if (userStreamId <= 0) {
                log.error("插入 userStream 记录出错");
                return new StreamConfigResultVO(StatusEnum.FAIL);
            }

            // 将brokers和group配置到 properties中。
            Properties prop = maskExecuteManage.consumerConfig(brokers, group);
            // 调用启动异步线程的方法完成流数据消费、分区脱敏、分区脱敏offset改变和脱敏后分区数据存储。
            StatusEnum statusEnum = maskExecuteManage.streamDataMask(prop, topic, userStreamId, maskConfigs);
            return new StreamConfigResultVO(statusEnum, userStreamId);

        } catch (Exception e) {
            e.printStackTrace();
            return new StreamConfigResultVO(StatusEnum.FAIL);
        }
    }

    /**
     * 前端 create 流数据脱敏过程展示页面时 将 userStreamId传到后端请求 collectionName topic 信息
     * @param queryUserStreamDTO userStreamId
     * @return collectionName topic
     */
    @RequestMapping(value = "/stream_info", method = RequestMethod.POST)
    @ResponseBody
    public UserStreamVO queryUserStreamInfo(@RequestBody QueryUserStreamDTO queryUserStreamDTO) {
        try{
            int userStreamId = queryUserStreamDTO.getUserStreamId();
            //调用接口查询userStream中的数据
            UserStream userStream = streamDataManage.queryUserStream(userStreamId);
            if (userStream == null) {
                log.info("cannot find userStream of id {}", userStreamId);
                return new UserStreamVO(StatusEnum.FAIL);
            }

            return new UserStreamVO(StatusEnum.SUCCESS, userStream.getCollectionName(), userStream.getTopic());
        } catch (Exception e) {
            e.printStackTrace();
            return new UserStreamVO(StatusEnum.FAIL);
        }
    }

    /**
     * 前端通过 collectionName 轮询查询的最新脱敏结果的接口
     * @param queryStreamDataDTO collectionName
     * @param request session id
     * @param response update cookie
     * @return fields and newestResultData
     */
    @RequestMapping(value = "/newest_masked_data", method = RequestMethod.POST)
    @ResponseBody
    public StreamMaskedResultVO queryNewestMaskResult(@RequestBody QueryStreamDataDTO queryStreamDataDTO,
                                                    HttpServletRequest request,
                                                    HttpServletResponse response) {
        try{
            HttpSession session = request.getSession();
            String loginStatus = (String) session.getAttribute("loginStatus");
            if (loginStatus == null || "".equals(loginStatus) || LOGOUT.equals(loginStatus)) {
                Cookie cookie = CookiesUtil.loginStatusCookie("logout");
                response.addCookie(cookie);
                return new StreamMaskedResultVO(StatusEnum.NO_USER_FAIL);
            }
            Cookie cookie = CookiesUtil.loginStatusCookie(loginStatus);
            response.addCookie(cookie);

            String collectionName = queryStreamDataDTO.getCollectionName();
            UserStream userStream = streamDataManage.queryUserStream(collectionName);
            String fieldsStr = userStream.getFields();
            if (fieldsStr == null || "".equals(fieldsStr)) {
                log.info("无字段记录，表示暂时还未消费到数据");
                return new StreamMaskedResultVO(StatusEnum.SUCCESS);
            }
            List<String> fields = Arrays.asList(fieldsStr.split(","));
            List<String[]> newestMaskData = streamDataManage.newestMaskStreamData(collectionName, fields);
            return new StreamMaskedResultVO(StatusEnum.SUCCESS, fields, newestMaskData);
        } catch (Exception e) {
            e.printStackTrace();
            return new StreamMaskedResultVO(StatusEnum.FAIL);
        }
    }

    /**
     * 查找当前脱敏处理的topic的partition的开始offset 和 结束 offset
     * 前端要轮询
     * @param queryUserStreamDTO userStreamId
     * @return List<PartitionOffsetsVO>
     */
    @RequestMapping(value = "/partition_offset", method = RequestMethod.POST)
    @ResponseBody
    public StreamMaskStatusVO queryStreamMaskStatus(@RequestBody QueryUserStreamDTO queryUserStreamDTO,
                                                    HttpServletRequest request,
                                                    HttpServletResponse response) {
        try{
            HttpSession session = request.getSession();
            String loginStatus = (String) session.getAttribute("loginStatus");
            if (loginStatus == null || "".equals(loginStatus) || LOGOUT.equals(loginStatus)) {
                Cookie cookie = CookiesUtil.loginStatusCookie("logout");
                response.addCookie(cookie);
                return new StreamMaskStatusVO(StatusEnum.NO_USER_FAIL);
            } else {
                Cookie cookie = CookiesUtil.loginStatusCookie(loginStatus);
                response.addCookie(cookie);
            }

            int userStreamId = queryUserStreamDTO.getUserStreamId();
            List<StreamMaskStatus> streamMaskStatuses = maskExecuteManage.queryStreamMaskStatus(userStreamId);
            List<PartitionOffsetsVO> partitionOffsetsVOS = new ArrayList<>();
            for (StreamMaskStatus streamMaskStatus : streamMaskStatuses) {
                PartitionOffsetsVO partitionOffsetsVO = new PartitionOffsetsVO(streamMaskStatus);
                partitionOffsetsVOS.add(partitionOffsetsVO);
            }
            return new StreamMaskStatusVO(StatusEnum.SUCCESS, partitionOffsetsVOS);
        } catch (Exception e) {
            e.printStackTrace();
            return new StreamMaskStatusVO(StatusEnum.FAIL);
        }
    }

    @RequestMapping(value = "/stop_consume", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse stopConsumerListener(HttpServletRequest request,
                                             HttpServletResponse response) {
        try {
            HttpSession session = request.getSession();
            String loginStatus = (String) session.getAttribute("loginStatus");
            if (loginStatus == null || "".equals(loginStatus) || LOGOUT.equals(loginStatus)) {
                Cookie cookie = CookiesUtil.loginStatusCookie("logout");
                response.addCookie(cookie);
                return new BaseResponse(StatusEnum.NO_USER_FAIL);
            } else {
                Cookie cookie = CookiesUtil.loginStatusCookie(loginStatus);
                response.addCookie(cookie);
            }

            maskExecuteManage.stopListener(true);
            return new BaseResponse(StatusEnum.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse(StatusEnum.FAIL);
        }
    }

    /**
     * 查询流数据脱敏结果数据
     * @param queryUserStreamDTO userStreamId
     * @param request session id
     * @param response set cookie
     * @return fields and List<String[]> data
     */
    @RequestMapping(value = "/masked_data", method = RequestMethod.POST)
    @ResponseBody
    public StreamMaskedResultVO queryStreamMaskedData(@RequestBody QueryUserStreamDTO queryUserStreamDTO,
                                                      HttpServletRequest request,
                                                      HttpServletResponse response) {
        try{
            HttpSession session = request.getSession();
            String loginStatus = (String) session.getAttribute("loginStatus");
            if (loginStatus == null || "".equals(loginStatus) || LOGOUT.equals(loginStatus)) {
                Cookie cookie = CookiesUtil.loginStatusCookie("logout");
                response.addCookie(cookie);
                return new StreamMaskedResultVO(StatusEnum.NO_USER_FAIL);
            } else {
                Cookie cookie = CookiesUtil.loginStatusCookie(loginStatus);
                response.addCookie(cookie);
            }
            int userStreamId = queryUserStreamDTO.getUserStreamId();
            UserStream userStream = streamDataManage.queryUserStream(userStreamId);
            String fieldsStr = userStream.getFields();
            if (fieldsStr == null || "".equals(fieldsStr)) {
                log.info("无字段记录，表示未消费到数据");
                return new StreamMaskedResultVO(StatusEnum.SUCCESS);
            }
            List<String> fields = Arrays.asList(fieldsStr.split(","));
            // 调用方法查询得到所有数据。
            List<String[]> streamMaskedData = streamDataManage.allStreamMaskedData(userStream.getCollectionName(), fields);
            return new StreamMaskedResultVO(StatusEnum.SUCCESS, fields, streamMaskedData);
        } catch (Exception e) {
            e.printStackTrace();
            return new StreamMaskedResultVO(StatusEnum.FAIL);
        }
    }

    /**
     * 将流数据脱敏结果导出为指定格式的文件
     * @param streamExportFileDTO
     * @param request
     * @return
     */
    @RequestMapping(value = "/export_file", method = RequestMethod.POST)
    @ResponseBody
    public FileUrlVO exportFile(@RequestBody StreamExportFileDTO streamExportFileDTO,
                                HttpServletRequest request) {
        try {
            int userStreamId = streamExportFileDTO.getUserStreamId();
            String fileType = streamExportFileDTO.getFileType();
            UserStream userStream = streamDataManage.queryUserStream(userStreamId);
            String topic = userStream.getTopic();
            // 根据topic 和 fileType构建不包含路径的文件名
            String fileName = fileDataManage.maskedFileName(topic, fileType);
            //构造存放文件的路径
            String filePath = request.getSession().getServletContext().getRealPath("/files/");
            String fieldsStr = userStream.getFields();
            List<String> fields = Arrays.asList(fieldsStr.split(","));
            // 调用方法查询得到所有数据。
            List<String[]> streamMaskedData = streamDataManage.allStreamMaskedData(userStream.getCollectionName(), fields);
            //往文件中写数据并拿到这个文件对象的相对路径（访问url）
            String fileUrl = fileDataManage.exportFile(fileName, fileType, filePath, fields, streamMaskedData);
            log.info("流数据导出完成。{}", filePath);
            return new FileUrlVO(StatusEnum.SUCCESS, fileUrl, fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return new FileUrlVO(StatusEnum.FAIL);
        }
    }

}
