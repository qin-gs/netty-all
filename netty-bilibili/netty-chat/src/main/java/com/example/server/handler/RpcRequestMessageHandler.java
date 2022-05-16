package com.example.server.handler;

import com.example.message.RpcRequestMessage;
import com.example.message.RpcResponseMessage;
import com.example.server.service.ServicesFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage msg) throws Exception {
        RpcResponseMessage response = new RpcResponseMessage();
        response.setSequenceId(msg.getSequenceId());
        try {
            Object service = ServicesFactory.getService(Class.forName(msg.getInterfaceName()));
            Method method = service.getClass().getMethod(msg.getMethodName(), msg.getParameterTypes());
            Object invoke = method.invoke(service, msg.getParameterValue());
            log.info("invoke = " + invoke);

            response.setReturnValue(invoke);
        } catch (Exception e) {
            e.printStackTrace();
            // 直接写入 e 会导致错误信息太长
            response.setReturnValue(new Exception("远程调用出错: " + e.getCause().getMessage()));
        }
        ctx.writeAndFlush(response);
    }
}
