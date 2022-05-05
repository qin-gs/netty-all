package com.example.server.handler;

import com.example.message.ChatRequestMessage;
import com.example.message.ChatResponseMessage;
import com.example.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 处理聊天消息
 */
@ChannelHandler.Sharable
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage msg) throws Exception {
        // 找到将消息发送给谁
        Channel channel = SessionFactory.getSession().getChannel(msg.getTo());
        // 如果在线
        if (channel != null) {
            channel.writeAndFlush(new ChatResponseMessage(msg.getFrom(), msg.getContent()));
        } else {
            // 如果不在线，向发送者提示
            ctx.writeAndFlush(new ChatResponseMessage(false, "用户不存在或不在线"));
        }

    }
}
