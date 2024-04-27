package com.luckyone.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luckyone.web.common.ErrorCode;
import com.luckyone.web.constant.CommonConstant;
import com.luckyone.web.exception.BusinessException;
import com.luckyone.web.exception.ThrowUtils;
import com.luckyone.web.mapper.NotificationMapper;
import com.luckyone.web.model.dto.notification.NotificationQueryRequest;
import com.luckyone.web.model.entity.Notification;
import com.luckyone.web.model.vo.notification.NotificationVO;
import com.luckyone.web.service.NotificationService;
import com.luckyone.web.service.UserService;
import com.luckyone.web.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
    implements NotificationService{

    @Resource
    private UserService userService;

    @Override
    public void validNotification(Notification notification,List<String> domains, boolean add) {
        if (notification == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"前端请求信息不存在");
        }
        String title = notification.getTitle();
        String content = notification.getContent();
        Date startTime = notification.getStartTime();
        Date endTime = notification.getEndTime();
        // 校验域名是否存在公告
        if (!ObjectUtils.isEmpty(domains)) {
            LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
            if (!add) {
                queryWrapper.ne(Notification::getId, notification.getId());
            }

            queryWrapper.and(wrapper -> domains.forEach(domain -> wrapper.or().like(Notification::getDomain, "\"" + domain + "\"")));
            long count = this.count(queryWrapper);
            log.info("repeat data count is : {}",count);
            ThrowUtils.throwIf(count > 0,ErrorCode.PARAMS_ERROR,"该域名已存在对应的公告");
        }

        if (ObjectUtil.isEmpty(startTime) || ObjectUtil.isEmpty(endTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"开始时间或结束时间为空");
        }
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title,content ), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 2048) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"内容过长");
        }
    }

    @Override
    public QueryWrapper<Notification> getQueryWrapper(NotificationQueryRequest notificationQueryRequest) {
        if (notificationQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        // 获取请求中的数据
        Long id = notificationQueryRequest.getId();
        String title = notificationQueryRequest.getTitle();
        String content = notificationQueryRequest.getContent();
        Date startTime = notificationQueryRequest.getStartTime();
        Date endTime = notificationQueryRequest.getEndTime();
        Long userId = notificationQueryRequest.getUserId();
        Integer status = notificationQueryRequest.getStatus();
        List<String> domains = notificationQueryRequest.getDomain();
        String sortField = notificationQueryRequest.getSortField();
        String sortOrder = notificationQueryRequest.getSortOrder();

        // 拼接查询条件
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id",id);
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.ge(ObjectUtils.isNotEmpty(startTime), "startTime",startTime);
        queryWrapper.le(ObjectUtils.isNotEmpty(endTime), "endTime",endTime);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status",status);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId",userId);
        queryWrapper.eq("isDelete", false);
        if (CollectionUtils.isNotEmpty(domains)) {
            for (String domain : domains) {
                queryWrapper.like("domain", domain);
            }
        }
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public NotificationVO getNotificationVO(Notification notification) {
        NotificationVO notificationVO = NotificationVO.objToVo(notification);
        return notificationVO;
    }

    @Override
    public Page<NotificationVO> getNotificationVOPage(Page<Notification> notificationPage, HttpServletRequest request) {
        List<Notification> notificationList = notificationPage.getRecords();
        Page<NotificationVO> notificationVOPage = new Page<>(notificationPage.getCurrent(), notificationPage.getSize(), notificationPage.getTotal());
        if (CollUtil.isEmpty(notificationList)) {
            return notificationVOPage;
        }
        List<NotificationVO> notificationVOS = notificationList.stream().map(notification -> {
            NotificationVO notificationVO = NotificationVO.objToVo(notification);
            return notificationVO;
        }).collect(Collectors.toList());
        notificationVOPage.setRecords(notificationVOS);
        return notificationVOPage;
    }
}




