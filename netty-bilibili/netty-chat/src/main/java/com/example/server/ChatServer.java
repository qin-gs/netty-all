package com.example.server;

import com.example.protocol.MessageCodecSharable;
import com.example.protocol.ProtocolFrameDecoder;
import com.example.server.handler.ChatRequestMessageHandler;
import com.example.server.handler.GroupCreateRequestMessageHandler;
import com.example.server.handler.LoginRequestMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务器
 */
@Slf4j
public class ChatServer {

    public static final LoggingHandler LOGGING_HANDLER = new LoggingHandler();
    public static final MessageCodecSharable MESSAGE_CODEC_SHARABLE = new MessageCodecSharable();
    /**
     * 处理登录信息
     */
    public static final LoginRequestMessageHandler LOGIN_REQUEST_MESSAGE_HANDLER = new LoginRequestMessageHandler();
    /**
     * 处理一对一的聊天消息
     */
    public static final ChatRequestMessageHandler CHAT_REQUEST_MESSAGE_HANDLER = new ChatRequestMessageHandler();
    /**
     * 处理建群消息
     */
    public static final GroupCreateRequestMessageHandler GROUP_CREATE_REQUEST_MESSAGE_HANDLER = new GroupCreateRequestMessageHandler();

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup master = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        new ServerBootstrap().channel(NioServerSocketChannel.class).group(master, worker).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                // 处理 粘包/半包 (不能共享)
                ch.pipeline().addLast(new ProtocolFrameDecoder());
                ch.pipeline().addLast(LOGGING_HANDLER);
                // 可以共享
                ch.pipeline().addLast(MESSAGE_CODEC_SHARABLE);

                // 添加业务处理的 handler，只处理指定的数据类型
                // 处理登录消息
                ch.pipeline().addLast(LOGIN_REQUEST_MESSAGE_HANDLER);
                // 处理一对一聊天消息
                ch.pipeline().addLast(CHAT_REQUEST_MESSAGE_HANDLER);
                // 处理建群消息
                ch.pipeline().addLast(GROUP_CREATE_REQUEST_MESSAGE_HANDLER);
            }
        }).bind(8080).sync().channel().closeFuture().sync();

        master.shutdownGracefully();
        worker.shutdownGracefully();
    }

}
