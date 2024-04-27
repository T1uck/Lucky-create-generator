package com.luckyone.web.model.dto.like;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.luckyone.web.common.PageRequest;
import com.luckyone.web.model.enums.LikedStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GeneratorLikeQueryRequest extends PageRequest implements Serializable {

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

    private static final long serialVersionUID = 1L;
}