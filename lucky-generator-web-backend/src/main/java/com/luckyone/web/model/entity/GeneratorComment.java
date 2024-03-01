package com.luckyone.web.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 生成器评论表
 * @TableName generator_comment
 */
@TableName(value ="generator_comment")
@Data
public class GeneratorComment implements Serializable {
    /**
     * 评论id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 生成器id
     */
    private Long generatorId;

    /**
     * 根评论id，-1代表是根评论
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
     * 被评论者id
     */
    private Long toId;

    /**
     * 这条评论是回复那条评论的，只有子评论才有（子评论的子评论，树形）
     */
    private Long toCommentId;

    /**
     * 评论点赞数
     */
    private Integer likeComment;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}