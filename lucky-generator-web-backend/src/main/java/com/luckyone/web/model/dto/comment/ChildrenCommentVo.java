package com.luckyone.web.model.dto.comment;

import lombok.Data;

import java.util.Date;

@Data
public class ChildrenCommentVo {
    /**
     * 评论id
     */
    private Long id;

    /**
     * 生成器id
     */
    private Long generatorId;

    /**
     * 根id，-1代表根评论
     */
    private Long rootId;

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
     * 被评论者id
     */
    private Long toId;

    /**
     * 被评论者姓名
     */
    private String toUsername;

    /**
     *  被评论id
     */
    private Long toCommentId;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 创建时间
     */
    private Date createTime;
}
