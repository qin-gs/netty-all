package com.example.server;

import com.example.protocol.MessageCodecSharable;
import com.example.protocol.ProtocolFrameDecoder;
import com.example.server.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

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
    /**
     * 处理群消息
     */
    public static final GroupChatRequestMessageHandler GROUP_CHAT_REQUEST_MESSAGE_HANDLER = new GroupChatRequestMessageHandler();
    public static final QuitHandler QUIT_HANDLER = new QuitHandler();

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

                // 空闲检测，判断空闲时间是否超过指定值，超过后会触发相应事件
                ch.pipeline().addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS));
                // 处理空闲检测触发的事件
                ch.pipeline().addLast(new ChannelDuplexHandler() {
                    @Override
                    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                        IdleState state = ((IdleStateEvent) evt).state();
                        if (state == IdleState.READER_IDLE) {
                            log.debug("读空闲时间超过 {} 秒", 5);
                        }
                    }
                });

                // 添加业务处理的 handler，只处理指定的数据类型
                // 处理登录消息
                ch.pipeline().addLast(LOGIN_REQUEST_MESSAGE_HANDLER);
                // 处理一对一聊天消息
                ch.pipeline().addLast(CHAT_REQUEST_MESSAGE_HANDLER);
                // 处理建群消息
                ch.pipeline().addLast(GROUP_CREATE_REQUEST_MESSAGE_HANDLER);
                // 处理群消息
                ch.pipeline().addLast(GROUP_CHAT_REQUEST_MESSAGE_HANDLER);
                // 处理客户端退出事件(正常/异常)，不处理消息
                ch.pipeline().addLast(QUIT_HANDLER);
            }
        }).bind(8080).sync().channel().closeFuture().sync();

        master.shutdownGracefully();
        worker.shutdownGracefully();
    }

}
