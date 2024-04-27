package com.luckyone.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luckyone.web.model.dto.msgUnread.MsgUnreadQueryRequest;
import com.luckyone.web.model.entity.MsgUnread;
import com.luckyone.web.model.vo.msgUnread.CommentMsgUnreadVO;
import com.luckyone.web.model.vo.msgUnread.LoveMsgUnreadVO;

import javax.servlet.http.HttpServletRequest;

public interface MsgUnreadService extends IService<MsgUnread> {

    /**
     * 给指定用户的某一列未读消息加一
     * @param id   用户ID
     * @param column    msg_unread表列名 "reply"/"love"/"system"
     */
    void addOneUnread(Integer id, String column);

    /**
     * 分页查询点赞信息记录
     * @param msgUnreadQueryRequest
     * @param request
     * @return
     */
    Page<LoveMsgUnreadVO> listLoveMsgUnreadByPage(MsgUnreadQueryRequest msgUnreadQueryRequest, HttpServletRequest request);

    /**
     * 分页查询评论信息记录
     * @param msgUnreadQueryRequest
     * @param request
     * @return
     */
    Page<CommentMsgUnreadVO> listCommentMsgUnreadByPage(MsgUnreadQueryRequest msgUnreadQueryRequest,HttpServletRequest request);

    /**
     * 清除指定用户的某一列未读消息
     * @param id   用户ID
     * @param column    msg_unread表列名 "reply"/"at"/"love"/"system"/"whisper"
     */
    void clearUnread(Integer id, String column);

    /**
     * 私聊消息特有的减除一定数量的未读数
     * @param id   用户ID
     * @param count 要减多少
     */
    void subtractWhisper(Integer id, Integer count);

    /**
     * 获取某人的全部消息未读数
     * @param id   用户ID
     * @return  MsgUnread对象
     */
    MsgUnread getUnread(Integer id);
}
