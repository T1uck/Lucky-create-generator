package com.luckyone.web.controller.Notice;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luckyone.web.common.BaseResponse;
import com.luckyone.web.common.ResultUtils;
import com.luckyone.web.model.dto.msgUnread.MsgUnreadQueryRequest;
import com.luckyone.web.model.entity.MsgUnread;
import com.luckyone.web.model.entity.User;
import com.luckyone.web.model.vo.msgUnread.CommentMsgUnreadVO;
import com.luckyone.web.model.vo.msgUnread.LoveMsgUnreadVO;
import com.luckyone.web.service.MsgUnreadService;
import com.luckyone.web.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/msg-unread")
public class MsgUnreadController {
    @Resource
    private MsgUnreadService msgUnreadService;

    @Resource
    private UserService userService;

    /**
     * 获取当前用户全部消息未读数
     * @return
     */
    @GetMapping("/all")
    public BaseResponse<MsgUnread> getMsgUnread(HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        MsgUnread unread = msgUnreadService.getUnread(Math.toIntExact(currentUser.getId()));
        return ResultUtils.success(unread);
    }

    /**
     * 清除某一列的未读消息提示
     * @param column    msg_unread表列名 "reply"/"love"/"systemNotice"/"whisper"
     */
    @PostMapping("/clear")
    public void clearUnread(@RequestParam("column") String column,HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        msgUnreadService.clearUnread(Math.toIntExact(currentUser.getId()), column);
    }


    /**
     * 分页查询点赞消息记录
     * @param msgUnreadQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/love")
    public BaseResponse<Page<LoveMsgUnreadVO>> listLoveMsgUnreadByPage(@RequestBody MsgUnreadQueryRequest msgUnreadQueryRequest, HttpServletRequest request) {
        Page<LoveMsgUnreadVO> loveMsgUnreadVOPage = msgUnreadService.listLoveMsgUnreadByPage(msgUnreadQueryRequest, request);
        return ResultUtils.success(loveMsgUnreadVOPage);
    }

    /**
     * 分页查询评论消息记录msg
     * @param msgUnreadQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/reply")
    public BaseResponse<Page<CommentMsgUnreadVO>> listCommentMsgUnreadByPage(@RequestBody MsgUnreadQueryRequest msgUnreadQueryRequest,HttpServletRequest request) {
        Page<CommentMsgUnreadVO> vos = msgUnreadService.listCommentMsgUnreadByPage(msgUnreadQueryRequest,request);
        return ResultUtils.success(vos);
    }
}
