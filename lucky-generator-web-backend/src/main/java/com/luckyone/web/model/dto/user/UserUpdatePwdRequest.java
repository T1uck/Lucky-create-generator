package com.luckyone.web.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新密码
 */
@Data
public class UserUpdatePwdRequest implements Serializable {

    /**
     * 老密码
     */
    private String originalPassword;

    /**
     * 新密码
     */
    private String newPassword;

    private static final long serialVersionUID = 1L;
}