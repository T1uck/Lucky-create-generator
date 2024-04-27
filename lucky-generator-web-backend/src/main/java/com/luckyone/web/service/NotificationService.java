package com.luckyone.web.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luckyone.web.model.dto.generator.GeneratorQueryRequest;
import com.luckyone.web.model.dto.notification.NotificationQueryRequest;
import com.luckyone.web.model.entity.Generator;
import com.luckyone.web.model.entity.Notification;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luckyone.web.model.vo.GeneratorVO;
import com.luckyone.web.model.vo.notification.NotificationVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface NotificationService extends IService<Notification> {
    /**
     * 校验
     *
     * @param notification
     * @param add
     */
    void validNotification(Notification notification, List<String> domains, boolean add);

    /**
     * 获取查询条件
     *
     * @param notificationQueryRequest
     * @return
     */
    QueryWrapper<Notification> getQueryWrapper(NotificationQueryRequest notificationQueryRequest);

    /**
     * 获取帖子封装
     *
     * @param notification
     * @return
     */
    NotificationVO getNotificationVO(Notification notification);

    /**
     * 分页获取帖子封装
     *
     * @param notificationPage
     * @param request
     * @return
     */
    Page<NotificationVO> getNotificationVOPage(Page<Notification> notificationPage, HttpServletRequest request);
}
