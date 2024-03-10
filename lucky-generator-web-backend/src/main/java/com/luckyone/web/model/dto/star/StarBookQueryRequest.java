package com.luckyone.web.model.dto.star;


import com.luckyone.web.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class StarBookQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 2222903543143705835L;

    private Long bookId;
}
