package com.luckyone.web.model.dto.comment;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PostCommentRequest {
    //内容
    @NotBlank
    private String content;

    /**
     * 生成器id
     */
    private Long generatorId;

    /**
     * 根评论id
     */
    private Long rootId;

    //以下的属性只有子回复有
    /**
     * 回复id
     */
    private Long toId;

    /**
     * 被回复id
     */
    private Long toCommentId;
}
