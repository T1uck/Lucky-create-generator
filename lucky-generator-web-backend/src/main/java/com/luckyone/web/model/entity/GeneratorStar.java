package com.luckyone.web.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 生成器收藏表
 * @TableName genrator_star
 */
@TableName(value ="generator_star")
@Data
public class GeneratorStar implements Serializable {
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
     * 收藏夹id
     */
    private Long bookId;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}