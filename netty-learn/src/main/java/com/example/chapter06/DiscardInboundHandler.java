package com.example.chapter06;

import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;

/**
 * 消费并释放入站消息
 */
@ChannelHandler.Sharable
public class DiscardInboundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 释放资源
        ReferenceCountUtil.release(msg);
    }
}

/**
 * 丢弃并释放出站消息
 */
@ChannelHandler.Sharable
class DiscardOutboundHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // 释放资源
        // 如果消息被消费 或 被丢弃了，没有传递给 ChannelPipeline 中的下一个 ChannelOutboundHandler，用户需要调用 release 方法
        // 如果消息达到了实际的传输层，当它被写入或 Channel 关闭时，会被自动释放
        ReferenceCountUtil.release(msg);
        // 通知 ChannelPromise 数据已经被处理了
        promise.setSuccess();
    }
}
