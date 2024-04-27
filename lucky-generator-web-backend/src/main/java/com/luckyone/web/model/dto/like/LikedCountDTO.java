package com.luckyone.web.model.dto.like;


import com.luckyone.web.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class LikedCountDTO extends PageRequest implements Serializable {
    private static final long serialVersionUID = 2222903543143705835L;

    private Long id;

    private Integer count;

    public LikedCountDTO() {
    }

    public LikedCountDTO(Long id, Integer count) {
        this.id = id;
        this.count = count;
    }
}
