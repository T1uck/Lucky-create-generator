package com.luckyone.web.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.luckyone.web.model.enums.LikedStatusEnum;
import lombok.Data;

/**
 * 生成器点赞表
 * @TableName genrator_like
 */
@TableName(value ="generator_like")
@Data
public class GeneratorLike implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 生成器id
     */
    private Long generatorId;


    /**
     * 创建用户id
     */
    private Long createBy;

    /**
     * 点赞状态 0为未点赞 1为点赞
     */
    private Integer status = 0;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}