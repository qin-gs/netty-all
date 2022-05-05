package com.example.server.handler;

import com.example.message.LoginRequestMessage;
import com.example.message.LoginResponseMessage;
import com.example.server.service.UserService;
import com.example.server.service.UserServiceFactory;
import com.example.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 处理登录消息
 */
@ChannelHandler.Sharable
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage msg) throws Exception {
        // 和内存中的用户名密码进行匹配
        UserService service = UserServiceFactory.getUserService();
        boolean login = service.login(msg.getUsername(), msg.getPassword());
        LoginResponseMessage message;
        if (login) {
            // 记录一下 channel 和 用户名 (不然消息不知道发给谁)
            SessionFactory.getSession().bind(ctx.channel(), msg.getUsername());
            message = new LoginResponseMessage(true, "登录成功");
        } else {
            message = new LoginResponseMessage(false, "登录失败");
        }
        ctx.writeAndFlush(message);
    }
}
