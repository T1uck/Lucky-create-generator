package com.luckyone.web.model.dto.msgUnread;

import com.luckyone.web.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class MsgUnreadQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;
}
