package com.example.chapter12;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 初始化 pipeline
 */
public class ChatServerInitializer extends ChannelInitializer<Channel> {

    private final ChannelGroup group;

    public ChatServerInitializer(ChannelGroup group) {
        this.group = group;
    }

    /**
     * 添加 Channel
     */
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(
                // 将字节编码为 HttpRequest, HttpContent, LastHttpContent
                // 将 HttpRequest, HttpContent, LastHttpContent 编码为字节
                new HttpServerCodec(),
                // 写入一个文件内容
                new ChunkedWriteHandler(),
                // 将一个 HttpMessage 和 跟随他的多个 HttpContent 聚合为单个 FullHttpRequest 或 FullHttpResponse
                // 之后的 Channel 只会收到完整的 http 请求 或 响应
                new HttpObjectAggregator(64 * 1024),
                // 处理 FullHttpRequest
                new HttpRequestHandler("/ws"),
                // 处理 WebSocket 升级握手
                // 处理了所有委托管理的 WebSocket 帧类型以 及升级握手本身。
                // WebSocket 协议升级完成之后：
                // HttpRequestDecoder  ->  WebSocketFrameDecoder
                // HttpResponseDecoder  ->  WwbSocketFrameDecoder
                // 如果握手成功，那么所需的 ChannelHandler 将会被添加到 ChannelPipeline 中，
                // 而那些不再需要的 ChannelHandler 则将会被移除
                new WebSocketServerProtocolHandler("/ws"),
                // 处理 TextWebSocketFrame 和 握手完成事件
                new TextWebSocketFrameHandler(group)
        );

    }
}
