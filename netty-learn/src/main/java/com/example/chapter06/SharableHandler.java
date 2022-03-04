package com.example.chapter06;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 一个 ChannelHandler 可以从属多个 ChannelPipeline，所以可以绑定到多个 ChannelHandlerContext，
 * 需要添加 @Sharable 注解并且是线程安全的，否则被添加到多个 ChannelPipeline 时会触发异常，
 * 可以用来搜集跨越多个 Channel 的统计信息
 */
@ChannelHandler.Sharable
public class SharableHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("channel read message: " + msg);
        ctx.fireChannelRead(msg);
    }
}
