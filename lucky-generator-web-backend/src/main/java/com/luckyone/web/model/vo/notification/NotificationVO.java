package com.luckyone.web.model.vo.notification;

import com.luckyone.web.model.entity.Notification;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Data
public class NotificationVO {

    /**
     * 公告id
     */
    private long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 更新时间
     */
    private Date updateTime;
    
    private static final long serialVersionUID = 1L;

    /**
     * 包装类转对象
     *
     * @param notificationVO
     * @return
     */
    public static Notification voToObj(NotificationVO notificationVO) {
        if (notificationVO == null) {
            return null;
        }
        Notification notification = new Notification();
        BeanUtils.copyProperties(notificationVO, notification);
        return notification;
    }

    /**
     * 对象转包装类
     *
     * @param notification
     * @return
     */
    public static NotificationVO objToVo(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationVO notificationVO = new NotificationVO();
        BeanUtils.copyProperties(notification, notificationVO);
        return notificationVO;
    }
}
