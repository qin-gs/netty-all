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
                new HttpServerCodec(),
                new ChunkedWriteHandler(),
                new HttpObjectAggregator(64 * 1024),
                new HttpRequestHandler("/ws"),
                new WebSocketServerProtocolHandler("/ws"),
                new TextWebSocketFrameHandler(group)
        );

    }
}
