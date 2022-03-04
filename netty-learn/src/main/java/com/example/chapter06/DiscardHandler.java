package com.example.chapter06;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

/**
 * 释放消息资源
 */
@ChannelHandler.Sharable
public class DiscardHandler extends ChannelInboundHandlerAdapter {

    /**
     * 重写该方法时，需要手动释放 或 池化 ByteBuf 实例相关的内存
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 手动释放资源
        ReferenceCountUtil.release(msg);
    }
}

/**
 * SimpleChannelInboundHandler 会自动释放资源，不要存储执行 msg 的引用
 */
@ChannelHandler.Sharable
class SimpleDiscardHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 不需要释放资源
    }
}
