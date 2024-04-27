package com.luckyone.web.model.enums;

import lombok.Getter;

@Getter
public enum LikedStatusEnum {
    LIKE("1", "点赞"),
    UNLIKE("0", "取消点赞/未点赞");

    private final String code;

    private final String value;

    LikedStatusEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }
}
