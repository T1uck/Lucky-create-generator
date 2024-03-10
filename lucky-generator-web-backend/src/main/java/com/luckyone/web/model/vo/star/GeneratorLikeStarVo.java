package com.luckyone.web.model.vo.star;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GeneratorLikeStarVo {
    /**
     * 是否电钻
     */
    private Boolean isLiked;

    /**
     * 是否收藏
     */
    private Boolean isStared;
}
