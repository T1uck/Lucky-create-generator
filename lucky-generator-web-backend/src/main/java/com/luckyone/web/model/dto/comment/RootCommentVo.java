package com.luckyone.web.model.dto.comment;

import lombok.Data;

import java.util.Date;

@Data
public class RootCommentVo {
    /**
     * 评论id
     */
    private Long id;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论者id
     */
    private Long fromId;

    /**
     * 评论者姓名
     */
    private String fromUsername;

    /**
     * 评论者头像
     */
    private String userAvatar;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 回复次数
     */
    private Integer replyCount;
}
