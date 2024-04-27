package com.luckyone.web.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天表
 * @TableName chat
 */
@TableName(value ="chat")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Chat implements Serializable {
    /**
     * 唯一主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 对象ID
     */
    private Integer userId;

    /**
     * 用户UID
     */
    private Integer anotherId;

    /**
     * 是否移除聊天 0否 1是
     */
    private Integer isDeleted;

    /**
     * 消息未读数量
     */
    private Integer unread;

    /**
     * 最近接收消息的时间或最近打开聊天窗口的时间
     */
    private Date latestTime;

    /**
     * 最后消息
     */
    private String lastMessage;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}