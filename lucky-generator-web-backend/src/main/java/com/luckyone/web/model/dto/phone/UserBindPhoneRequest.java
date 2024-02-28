package com.luckyone.web.model.dto.phone;

import lombok.Data;

import java.io.Serializable;

/**
 *  用户绑定电话号码请求
 */
@Data
public class UserBindPhoneRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String phone;
}
