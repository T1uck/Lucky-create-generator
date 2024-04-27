package com.luckyone.web.model.entity.Command;

import com.luckyone.web.model.entity.ChatDetailed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Command {
    /**
     * 业务编码
     */
    private Integer code;

    /**
     * 用户id（唯一标识）
     */
    private Long uid;

    /**
     * 聊天id
     */
    private Long chatId;

    /**
     * 聊天内容
     */
    private ChatDetailed chatDetailed;
}
