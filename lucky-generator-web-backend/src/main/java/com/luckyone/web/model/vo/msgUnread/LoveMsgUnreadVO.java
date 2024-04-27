package com.luckyone.web.model.vo.msgUnread;

import lombok.Data;

import java.util.Date;

/**
 * 点赞信息
 */
@Data
public class LoveMsgUnreadVO {
    /**
     * id
     */
    private Long id;

    /**
     * 创建用户id
     */
    private Long fromId;

    /**
     * 创建用户名称
     */
    private String userName;

    /**
     * 创建用户头像
     */
    private String userAvatar;

    /**
     * 生成器名称
     */
    private String generatorName;

    /**
     * 生成器描述
     */
    private String description;

    /**
     * 生成器id
     */
    private Long generatorId;

    /**
     * 点赞创建时间
     */
    private Date createTime;
}
