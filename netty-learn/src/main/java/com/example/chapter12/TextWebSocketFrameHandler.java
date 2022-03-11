package com.example.chapter12;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

/**
 * 处理 WebSocket 中的文本帧 (TextWebSocketFrame)
 * 当和新客户端的 WebSocket 握手成功完成之后，把通知消息写入 group 中的所有 Channel 通知所有已连接的客户端，
 * 把新 Channel 加入 group
 */
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final ChannelGroup group;

    public TextWebSocketFrameHandler(ChannelGroup group) {
        this.group = group;
    }

    /**
     * 如果接收到了 TextWebSocketFrame 消息，TextWebSocketFrameHandler 将调用
     * TextWebSocketFrame 消息上的 retain() 方法，并使用 writeAndFlush()方法来将它传
     * 输给 ChannelGroup，以便所有已经连接的WebSocket Channel 都将接收到它
     * <p>
     * 对于 retain() 方法的调用是必需的，因为当 channelRead0() 方法返回时，
     * TextWebSocketFrame 的引用计数将会被减少。
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        // 增加消息的引用计数，写入 group 中所有已连接的客户端
        group.writeAndFlush(msg.retain());
    }

    /**
     * 处理自定义事件
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 如果握手成功
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            // 从 pipeline 中移除 HttpRequestHandler，因为不会接受 http 消息了
            ctx.pipeline().remove(HttpRequestHandler.class);
            // 通知所有已经连接的 WebSocket 客户端，新的客户端已经连接上了
            group.writeAndFlush(new TextWebSocketFrame("client " + ctx.channel() + " joined"));
            // 将新的 WebSocket 的 Channel 添加到 group 中，让它可以接受所有消息
            group.add(ctx.channel());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
