package com.luckyone.web.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息未读数
 * @TableName msg_unread
 */
@TableName(value ="msg_unread")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MsgUnread implements Serializable {
    /**
     * 用户ID
     */
    @TableId
    private Integer id;

    /**
     * 回复我的
     */
    private Integer reply;

    /**
     * 收到的赞
     */
    private Integer love;

    /**
     * 系统通知
     */
    private Integer systemNotice;

    /**
     * 我的消息
     */
    private Integer whisper;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}