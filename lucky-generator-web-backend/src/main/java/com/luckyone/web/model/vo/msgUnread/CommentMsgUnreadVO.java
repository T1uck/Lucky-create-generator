package com.luckyone.web.model.vo.msgUnread;

import lombok.Data;

import java.util.Date;

@Data
public class CommentMsgUnreadVO {
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
     * 评论
     */
    private String fromContent;

    /**
     * 评论内容
     */
    private String content;

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
