package com.luckyone.web.model.dto.email;

import lombok.Data;

import java.io.Serializable;

/**
 *  用户注册请求体
 */
@Data
public class UserEmailRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String emailAccount;

    private String captcha;

    private String userName;

    private String agreeToAnAgreement;
}
