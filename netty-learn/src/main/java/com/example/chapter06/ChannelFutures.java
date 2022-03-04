package com.example.chapter06;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 出站操作会返回 ChannelFuture，向其中注册监听器判断是否成功
 */
public class ChannelFutures {

    private static final Channel CHANNEL_FROM_SOMEWHERE = new NioSocketChannel();
    private static final ByteBuf SOME_MSG_FROM_SOMEWHERE = Unpooled.buffer(1024);

    public static void addChanelFutureListener() {
        Channel channel = CHANNEL_FROM_SOMEWHERE;
        ByteBuf message = SOME_MSG_FROM_SOMEWHERE;

        ChannelFuture future = channel.write(message);
        future.addListener((ChannelFutureListener) future1 -> {
            // 出现异常之后，关闭 Channel
            if (!future1.isSuccess()) {
                future1.cause().printStackTrace();
                future1.channel().close();
            }
        });
    }
}
