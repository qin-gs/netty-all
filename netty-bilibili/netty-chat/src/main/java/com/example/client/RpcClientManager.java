package com.example.client;

import com.example.message.RpcRequestMessage;
import com.example.protocol.MessageCodecSharable;
import com.example.protocol.ProtocolFrameDecoder;
import com.example.server.handler.RpcResponseMessageHandler;
import com.example.server.service.HelloService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.Random;

@Slf4j
public class RpcClientManager {

    public static final LoggingHandler LOGGING_HANDLER = new LoggingHandler();
    public static final MessageCodecSharable MESSAGE_CODEC_SHARABLE = new MessageCodecSharable();
    public static final RpcResponseMessageHandler RPC_RESPONSE_MESSAGE_HANDLER = new RpcResponseMessageHandler();

    private static Channel channel = null;

    public static void main(String[] args) {

        // 初始化 channel
        channel = getChannel();
        // 获取一个 jdk 动态代理对象
        HelloService service = getProxyService(HelloService.class);
        System.err.println(service.hello("qqq"));
        System.err.println(service.hello("www"));
    }

    /**
     * 获取 Channel
     */
    public static Channel getChannel() {
        if (channel != null) {
            return channel;
        }
        synchronized (RpcClientManager.class) {
            if (channel == null) {
                initChannel();
            }
            return channel;
        }
    }

    static Random random = new Random();

    /**
     * 创建一个 jdk 动态代理对象
     */
    public static <T> T getProxyService(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, ((proxy, method, args) -> {
            // 将方法调用转换为对象
            int sequenceId = random.nextInt(Integer.MAX_VALUE);
            RpcRequestMessage message = new RpcRequestMessage(sequenceId, clazz.getName(), method.getName(), method.getReturnType(), method.getParameterTypes(), args);
            // 将消息对象发出去
            channel.writeAndFlush(message);
            // 准备一个 Promise 接收结果，指定 promise 对象异步接收结果的线程
            DefaultPromise promise = new DefaultPromise<Object>(channel.eventLoop());
            RpcResponseMessageHandler.PROMISE_MAP.put(sequenceId, promise);

            // promise 中获取到结果之后才能返回
            promise.await();
            if (promise.isSuccess()) {
                // 调用成功之后，返回结果
                return promise.getNow();
            } else {
                // 调用失败之后，抛异常
                throw new RuntimeException(promise.cause());
            }
        }));
    }

    /**
     * 初始化 channel
     */
    private static void initChannel() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        // 客户端建立连接
        Bootstrap bootstrap = new Bootstrap().group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ProtocolFrameDecoder());
                ch.pipeline().addLast(LOGGING_HANDLER);
                ch.pipeline().addLast(MESSAGE_CODEC_SHARABLE);
                ch.pipeline().addLast(RPC_RESPONSE_MESSAGE_HANDLER);
            }
        });
        try {
            channel = bootstrap.connect("localhost", 8080).sync().channel();
            // 通过 addListener 关闭 group, 不能使用 sync 会被阻塞
            channel.closeFuture().addListener(future -> group.shutdownGracefully());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
