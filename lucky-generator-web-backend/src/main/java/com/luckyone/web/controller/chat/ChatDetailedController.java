package com.luckyone.web.controller.chat;


import com.luckyone.web.common.BaseResponse;
import com.luckyone.web.common.ErrorCode;
import com.luckyone.web.common.ResultUtils;
import com.luckyone.web.model.entity.ChatDetailed;
import com.luckyone.web.model.entity.User;
import com.luckyone.web.service.ChatDetailedService;
import com.luckyone.web.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
public class ChatDetailedController {
    @Resource
    private ChatDetailedService chatDetailedService;

    @Resource
    private UserService userService;

    /**
     * 获取历史消息记录
     * @param id
     * @param offset
     * @param request
     * @return
     */
    @GetMapping("/msg/chatDetailed")
    public BaseResponse<List<ChatDetailed>> getChatDetail(@RequestParam("id") Integer id, @RequestParam("offset") Long offset, HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        Long loginId = currentUser.getId();
        return ResultUtils.success(chatDetailedService.getDetail(id, Math.toIntExact(loginId), offset));
    }

    /**
     * 获取更多历史消息记录
     * @param id   聊天对象的UID
     * @param offset    偏移量，即已经获取过的消息数量，从哪条开始获取更多
     * @return  CustomResponse对象，包含更多消息记录的map
     */
    @GetMapping("/msg/chat-detailed/get-more")
    public BaseResponse<Map> getMoreChatDetails(@RequestParam("id") Integer id,
                                                @RequestParam("offset") Long offset, HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        Long loginId = currentUser.getId();
        return ResultUtils.success(chatDetailedService.getDetails(id, Math.toIntExact(loginId), offset));
    }

    /**
     * 删除消息
     * @param id    消息ID
     * @return  CustomResponse对象
     */
    @PostMapping("/msg/chat-detailed/delete")
    public BaseResponse<Boolean> delDetail(@RequestParam("id") Integer id,HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        Long loginId = currentUser.getId();
        if (!chatDetailedService.deleteDetail(id, Math.toIntExact(loginId))) {
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"删除消息失败");
        }
        return ResultUtils.success(true);
    }
}
