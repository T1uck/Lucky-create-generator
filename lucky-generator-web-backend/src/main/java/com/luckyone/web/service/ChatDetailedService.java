package com.luckyone.web.service;

import com.luckyone.web.model.entity.ChatDetailed;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface ChatDetailedService extends IService<ChatDetailed> {
    /**
     * 获取当前聊天的20条消息
     * @param uid   发消息者UID（对方）
     * @param aid   收消息者UID（自己）
     * @param offset    偏移量 从哪条开始数（已经查过了几条）
     * @return  消息列表以及是否还有更多 { list: List, more: boolean }
     */
    Map<String, Object> getDetails(Integer uid, Integer aid, Long offset);

    /**
     * 删除单条消息记录
     * @param id 消息记录的id
     * @param uid   当前登录用户的UID
     * @return  成功/失败
     */
    boolean deleteDetail(Integer id, Integer uid);

    /**
     * 获取当前聊天
     * @param uid
     * @param aid
     * @param offset
     * @return
     */
    List<ChatDetailed> getDetail(Integer uid, Integer aid, Long offset);
}
