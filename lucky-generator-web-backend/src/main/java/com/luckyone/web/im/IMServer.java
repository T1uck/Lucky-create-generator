package com.luckyone.web.im;

import com.luckyone.web.im.handler.WebSocketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class IMServer {

    public static final int WEB_SOCKET_PORT = 8666;

    // 存储每个用户的全部链接
    public static final Map<Integer, Set<Channel>> userChannel = new ConcurrentHashMap<>();

    public void start() throws InterruptedException {

        // 主从结构
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        // 绑定监听接口
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss,worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        // 通道拦截处理类
                        ChannelPipeline pipeline = socketChannel.pipeline(); // 处理流

                        // 添加 Http 编码解码器
                        pipeline.addLast(new HttpServerCodec())
                                // 添加处理大数据的组件
                                .addLast(new ChunkedWriteHandler())
                                // 对 Http 消息做聚合操作方便处理，产生 FullHttpRequest 和 FullHttpResponse
                                // 1024 * 64 是单条信息最长字节数
                                .addLast(new HttpObjectAggregator(1024 * 64))
                                // 添加 WebSocket 支持
                                .addLast(new WebSocketServerProtocolHandler("/"))
                                .addLast(new WebSocketHandler());

                    }
                });
        bootstrap.bind(8666).sync();
    }
}
