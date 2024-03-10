package com.luckyone.web.model.dto.user;

import lombok.Data;

@Data
public class UsernameAndAvtarDto {
    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;
}
