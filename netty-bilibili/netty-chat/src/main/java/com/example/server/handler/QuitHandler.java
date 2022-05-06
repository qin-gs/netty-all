package com.example.server.handler;

import com.example.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理连接断开事件
 */
@Slf4j
@ChannelHandler.Sharable
public class QuitHandler extends ChannelInboundHandlerAdapter {

    /**
     * 处理连接正常断开
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 断开后，从会话管理器中移除该用户
        SessionFactory.getSession().unbind(ctx.channel());
        log.debug("{} 正常断开", ctx.channel());
    }

    /**
     * 处理连接异常断开
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SessionFactory.getSession().unbind(ctx.channel());
        log.debug("{} 异常断开，异常原因 {}", ctx.channel(), cause.getMessage());
    }
}
