package com.luckyone.web.controller.chat;


import com.luckyone.web.common.BaseResponse;
import com.luckyone.web.common.ErrorCode;
import com.luckyone.web.common.ResultUtils;
import com.luckyone.web.model.entity.Chat;
import com.luckyone.web.model.entity.User;
import com.luckyone.web.service.ChatService;
import com.luckyone.web.service.UserService;
import com.luckyone.web.utils.RedisUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("")
public class ChatController {
    @Resource
    private ChatService chatService;

    @Resource
    private UserService userService;
    @Resource
    private RedisUtil redisUtil;

    /**
     * 新建一个聊天，与其他用户首次聊天时调用
     * @param id  对方用户ID
     * @return message可能值："新创建"/"已存在"/"未知用户"
     */
    @GetMapping("/msg/chat/create/{id}")
    public BaseResponse createChat(@PathVariable("id") Integer id, HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        Map<String, Object> result = chatService.createChat(id, Math.toIntExact(currentUser.getId()));
       if (Objects.equals(result.get("msg").toString(), "新创建")) {
           return ResultUtils.success(result);  // 返回新创建的聊天
       } else if (Objects.equals(result.get("msg").toString(), "未知用户")) {
           return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR);
       }
       return ResultUtils.error(ErrorCode.PARAMS_ERROR,result.get("msg").toString());
    }

    /**
     * 获取用户最近的聊天列表
     * @param offset    分页偏移量（前端查询了多少个聊天）
     * @return  CustomResponse对象 包含带用户信息和最近一条消息的聊天列表以及是否还有更多数据
     */
    @GetMapping("/msg/chat/recent-list")
    public BaseResponse<Map> getRecentList(@RequestParam("offset") Long offset,HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        Long id = currentUser.getId();
        Map<String, Object> map = new HashMap<>();
        map.put("list", chatService.getChatListWithData(Math.toIntExact(id), offset));
        // 检查是否还有更多
        if (offset + 10 < redisUtil.zCard("chat_zset:" + id)) {
            map.put("more", true);
        } else {
            map.put("more", false);
        }
        return ResultUtils.success(map);
    }

    /**
     * 获取聊天列表
     * @param offset
     * @param request
     * @return
     */
    @GetMapping("/msg/getChats")
    public BaseResponse<List<Chat>> getChat(@RequestParam("offset") Long offset,HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        Long id = currentUser.getId();
        List<Chat> chats = chatService.getChats(Math.toIntExact(id), offset);
        return ResultUtils.success(chats);
    }

    /**
     * 移除聊天
     * @param id  对方用户ID
     * @return  CustomResponse对象
     */
    @GetMapping("/msg/chat/delete/{id}")
    public BaseResponse<Boolean> deleteChat(@PathVariable("id") Integer id, HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        chatService.delChat(id, Math.toIntExact(currentUser.getId()));
        return ResultUtils.success(true);
    }

    /**
     * 切换窗口时 更新在线状态以及清除未读
     * @param from  对方UID
     */
    @GetMapping("/msg/chat/online")
    public void updateWhisperOnline(@RequestParam("from") Integer from,HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        Long id = currentUser.getId();
        chatService.updateWhisperOnline(from, Math.toIntExact(id));
    }

    /**
     * 切换窗口时 更新为离开状态 （该接口要放开，无需验证token，防止token过期导致用户一直在线）
     * @param from  对方UID
     */
    @GetMapping("/msg/chat/outline")
    public void updateWhisperOutline(@RequestParam("from") Integer from, @RequestParam("to") Integer to) {
        chatService.updateWhisperOutline(from, to);
    }
}
