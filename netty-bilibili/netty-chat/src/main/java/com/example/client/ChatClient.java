package com.example.client;

import com.example.message.*;
import com.example.protocol.MessageCodecSharable;
import com.example.protocol.ProtocolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ChatClient {

    public static final LoggingHandler LOGGING_HANDLER = new LoggingHandler();
    public static final MessageCodecSharable MESSAGE_CODEC_SHARABLE = new MessageCodecSharable();

    public static void main(String[] args) throws InterruptedException {

        CountDownLatch WAIT_FOR_LOGIN = new CountDownLatch(1);
        AtomicBoolean IS_LOGIN_SUCCESS = new AtomicBoolean(false);

        NioEventLoopGroup group = new NioEventLoopGroup();
        new Bootstrap().channel(NioSocketChannel.class).group(group).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                // 处理 粘包/半包 (不能共享)
                ch.pipeline().addLast(new ProtocolFrameDecoder());
                // 日志 可共享
                ch.pipeline().addLast(LOGGING_HANDLER);
                // 消息解码，可以共享
                ch.pipeline().addLast(MESSAGE_CODEC_SHARABLE);

                ch.pipeline().addLast("client handler", new ChannelInboundHandlerAdapter() {
                    /**
                     * 连接建立完成后调用该方法
                     */
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        // 接收用户的输入，向服务器发送消息
                        new Thread(() -> {
                            Scanner sc = new Scanner(System.in);
                            System.out.println("输入用户名:");
                            String username = sc.nextLine();
                            System.out.println("输入密码:");
                            String password = sc.nextLine();

                            LoginRequestMessage message = new LoginRequestMessage(username, password);
                            ctx.writeAndFlush(message);

                            try {
                                WAIT_FOR_LOGIN.await();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            // 如果登录成功，处理消息
                            if (IS_LOGIN_SUCCESS.get()) {
                                afterLogin(ctx, sc, username);
                            } else {
                                // 登录失败，关闭 channel
                                ctx.channel().close();
                            }
                        }, "a new thread").start();
                    }

                    /**
                     * 处理服务器发送过来的数据
                     */
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        log.debug(msg.toString());
                        if (msg instanceof LoginResponseMessage) {
                            LoginResponseMessage response = (LoginResponseMessage) msg;
                            // 如果登录成功
                            if (response.isSuccess()) {
                                IS_LOGIN_SUCCESS.set(true);
                            }
                            WAIT_FOR_LOGIN.countDown();
                        }
                    }
                });
            }
        }).connect("localhost", 8080).sync().channel().closeFuture().sync();
    }

    private static void afterLogin(ChannelHandlerContext ctx, Scanner sc, String username) {
        // 如果登录成功了，开始接收消息
        while (true) {
            System.out.println("==================================");
            System.out.println("send [username] [content]");
            System.out.println("gsend [group name] [content]");
            System.out.println("gcreate [group name] [m1,m2,m3...]");
            System.out.println("gmembers [group name]");
            System.out.println("gjoin [group name]");
            System.out.println("gquit [group name]");
            System.out.println("quit");
            System.out.println("==================================");

            String[] commands = sc.nextLine().split(" ");
            switch (commands[0]) {
                case "send": {
                    ctx.writeAndFlush(new ChatRequestMessage(username, commands[1], commands[2]));
                    break;
                }
                case "gsend": {
                    ctx.writeAndFlush(new GroupChatRequestMessage(username, commands[1], commands[2]));
                    break;
                }
                case "gcreate": {
                    Set<String> set = new HashSet<>(Arrays.asList(commands[2].split(",")));
                    set.add(username); // 加入自己
                    ctx.writeAndFlush(new GroupCreateRequestMessage(commands[1], set));
                    break;
                }
                case "gmembers": {
                    ctx.writeAndFlush(new GroupMembersRequestMessage(commands[1]));
                    break;
                }
                case "gjoin": {
                    ctx.writeAndFlush(new GroupJoinRequestMessage(username, commands[1]));
                    break;
                }
                case "gquit": {
                    ctx.writeAndFlush(new GroupQuitRequestMessage(username, commands[1]));
                    break;
                }
                case "quit": {
                    ctx.channel().close();
                    break;
                }
                default: {
                    log.error("{} 消息不对", Arrays.toString(commands));
                    break;
                }
            }
        }
    }

}
