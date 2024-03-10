package com.luckyone.web.model.vo.star;

import lombok.Data;

@Data
public class StarBookBoolVo {
    /**
     * 收藏夹id
     */
    private Long id;

    /**
     * 收藏夹名字
     */
    private String name;
    /**
     * 收藏数
     */
    private Integer count;

    /**
     * 是否包含
     */
    private Boolean isContain=false;
}
