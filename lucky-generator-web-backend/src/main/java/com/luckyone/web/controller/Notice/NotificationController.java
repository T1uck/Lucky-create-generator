package com.luckyone.web.controller.Notice;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luckyone.web.annotation.AuthCheck;
import com.luckyone.web.common.BaseResponse;
import com.luckyone.web.common.DeleteRequest;
import com.luckyone.web.common.ErrorCode;
import com.luckyone.web.common.ResultUtils;
import com.luckyone.web.constant.UserConstant;
import com.luckyone.web.exception.BusinessException;
import com.luckyone.web.exception.ThrowUtils;
import com.luckyone.web.model.dto.notification.NotificationAddRequest;
import com.luckyone.web.model.dto.notification.NotificationEditRequest;
import com.luckyone.web.model.dto.notification.NotificationQueryRequest;
import com.luckyone.web.model.dto.notification.NotificationUpdateRequest;
import com.luckyone.web.model.entity.Notification;
import com.luckyone.web.model.entity.User;
import com.luckyone.web.model.vo.notification.NotificationVO;
import com.luckyone.web.service.NotificationService;
import com.luckyone.web.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * 帖子接口
 */
@RestController
@RequestMapping("/notification")
@Slf4j
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建
     *
     * @param notificationAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addNotification(@RequestBody NotificationAddRequest notificationAddRequest, HttpServletRequest request) {
        if (notificationAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Notification notification = new Notification();
        BeanUtils.copyProperties(notificationAddRequest, notification);
        List<String> domains = notificationAddRequest.getDomain();
        if (domains != null) {
            notification.setDomain(JSONUtil.toJsonStr(domains));
        }
        notificationService.validNotification(notification, domains,true);
        User loginUser = userService.getLoginUser(request);
        notification.setUserId(loginUser.getId());
        boolean result = notificationService.save(notification);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newNotificationId = notification.getId();
        return ResultUtils.success(newNotificationId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteNotification(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Notification oldNotification = notificationService.getById(id);
        ThrowUtils.throwIf(oldNotification == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldNotification.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = notificationService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param notificationUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateNotification(@RequestBody NotificationUpdateRequest notificationUpdateRequest) {
        if (notificationUpdateRequest == null || notificationUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Notification notification = new Notification();
        BeanUtils.copyProperties(notificationUpdateRequest, notification);
        List<String> domains = notificationUpdateRequest.getDomain();
        if (domains != null) {
            notification.setDomain(JSONUtil.toJsonStr(domains));
        }
        // 参数校验
        notificationService.validNotification(notification, domains,false);
        long id = notificationUpdateRequest.getId();
        // 判断是否存在
        Notification oldNotification = notificationService.getById(id);
        ThrowUtils.throwIf(oldNotification == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = notificationService.updateById(notification);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<NotificationVO> getNotificationVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Notification notification = notificationService.getById(id);
        if (notification == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(notificationService.getNotificationVO(notification));
    }

    /**
     * 根据 domain 获取
     * @param domain
     * @return
     */
    @GetMapping("/domain/get/vo")
    public BaseResponse<NotificationVO> getNotificationVOByDomain(@RequestParam String domain) {
        // 1，校验参数
        if (domain.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"域名为空");
        }
        // 2.查询通知
        Notification notification = notificationService.getOne(new QueryWrapper<Notification>().like("domain", "\"" + domain + "\""));
        if (notification == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"暂时没有通知");
        }
        // 3.校验通知是否开启
        Integer status = notification.getStatus();
        if (status == 0) {
            return ResultUtils.success(null);
        }
        // 4.校验是否在开始时间到结束时间类
        Date startTime = notification.getStartTime();
        Date endTime = notification.getEndTime();
        if (startTime == null || endTime == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"开始时间或结束时间为空");
        }
        // 当前时间
        Date date = DateUtil.date();
        // 判断当前时间是否在开始时间到结束时间内
        if (date.before(startTime) || date.after(endTime)) {
            return ResultUtils.success(null);
        }
        log.info("GetNotificationVO: {}",notification);
        NotificationVO notificationVO = notificationService.getNotificationVO(notification);
        return ResultUtils.success(notificationVO);
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param notificationQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Notification>> listNotificationByPage(@RequestBody NotificationQueryRequest notificationQueryRequest) {
        long current = notificationQueryRequest.getCurrent();
        long size = notificationQueryRequest.getPageSize();
        Page<Notification> notificationPage = notificationService.page(new Page<>(current, size),
                notificationService.getQueryWrapper(notificationQueryRequest));
        return ResultUtils.success(notificationPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param notificationQueryRequest
     * @param request
     * @returndi
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<NotificationVO>> listNotificationVOByPage(@RequestBody NotificationQueryRequest notificationQueryRequest,
            HttpServletRequest request) {
        long current = notificationQueryRequest.getCurrent();
        long size = notificationQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Notification> notificationPage = notificationService.page(new Page<>(current, size),
                notificationService.getQueryWrapper(notificationQueryRequest));
        return ResultUtils.success(notificationService.getNotificationVOPage(notificationPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param notificationQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<NotificationVO>> listMyNotificationVOByPage(@RequestBody NotificationQueryRequest notificationQueryRequest,
            HttpServletRequest request) {
        if (notificationQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        notificationQueryRequest.setUserId(loginUser.getId());
        long current = notificationQueryRequest.getCurrent();
        long size = notificationQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Notification> notificationPage = notificationService.page(new Page<>(current, size),
                notificationService.getQueryWrapper(notificationQueryRequest));
        return ResultUtils.success(notificationService.getNotificationVOPage(notificationPage, request));
    }

    // endregion
    /**
     * 编辑（用户）
     *
     * @param notificationEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editNotification(@RequestBody NotificationEditRequest notificationEditRequest, HttpServletRequest request) {
        if (notificationEditRequest == null || notificationEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Notification notification = new Notification();
        BeanUtils.copyProperties(notificationEditRequest, notification);
        // 参数校验
        List<String> domains = notificationEditRequest.getDomain();
        notificationService.validNotification(notification, domains,false);
        User loginUser = userService.getLoginUser(request);
        long id = notificationEditRequest.getId();
        // 判断是否存在
        Notification oldNotification = notificationService.getById(id);
        ThrowUtils.throwIf(oldNotification == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldNotification.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = notificationService.updateById(notification);
        return ResultUtils.success(result);
    }

}
