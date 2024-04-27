package com.luckyone.web.im.handler;


import cn.hutool.json.JSONUtil;
import com.luckyone.web.common.IMResponse;
import com.luckyone.web.im.IMServer;
import com.luckyone.web.model.dto.chat.Response;
import com.luckyone.web.model.entity.ChatDetailed;
import com.luckyone.web.model.entity.Command.Command;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x500.style.RFC4519Style;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.uid;

@Slf4j
@Component
public class ConnectionHandler {
    public static void execute(ChannelHandlerContext ctx,Command command){
        Long uid = command.getUid();
        //连接已经建立了（重复登录）
        if(IMServer.userChannel.containsKey(uid)){
            ctx.channel().writeAndFlush(IMResponse.error("已经链接了，请先退出"));
            ctx.channel().disconnect();
            return;
        }

        Set<Channel> set = new HashSet<>();
        set.add(ctx.channel());
        IMServer.userChannel.put(Math.toIntExact(uid), set);
        ctx.channel().writeAndFlush(Response.ok());

        //添加关闭连接事件监听
        ctx.channel().closeFuture().addListener((ChannelFutureListener) future -> {
            IMServer.userChannel.remove(uid);
            log.info("{}的连接已经断开", uid);
        });
    }
}
