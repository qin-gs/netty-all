package com.example.chapter11;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

/**
 * 发送心跳
 */
public class IdleStateHandlerInitializer extends ChannelInitializer<Channel> {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 如果连接超过 60 秒没有接收或者发送任何的数据，使用 IdleStateEvent 调用 userEventTriggered 方法
        pipeline.addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS));
        pipeline.addLast(new HeartbeatHandler());
    }

    /**
     * 发送心跳消息
     */
    public static final class HeartbeatHandler extends ChannelInboundHandlerAdapter {

        // 发送到远程节点的心跳消息
        public static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(
                Unpooled.copiedBuffer("HEARTBEAT", CharsetUtil.ISO_8859_1));

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateHandler) {
                // 检测 IdleStateHandler 事件
                // 发送心跳消息，发送失败时关闭连接
                ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate())
                        .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } else {
                // 不是想要的事件，发送给下一个 Handler
                super.userEventTriggered(ctx, evt);
            }

        }
    }
}
