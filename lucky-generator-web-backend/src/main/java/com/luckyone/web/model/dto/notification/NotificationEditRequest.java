package com.luckyone.web.model.dto.notification;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.luckyone.maker.meta.Meta;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 编辑请求
 */
@Data
public class NotificationEditRequest implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 所属用户
     */
    private Long userId;

    /**
     * 0: 关闭，1：开启
     */
    private Integer status;

    /**
     * 域名
     */
    private List<String> domain;

    private static final long serialVersionUID = 1L;
}