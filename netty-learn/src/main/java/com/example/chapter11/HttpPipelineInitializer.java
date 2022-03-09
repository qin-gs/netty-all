package com.example.chapter11;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * 添加 http 支持
 */
public class HttpPipelineInitializer extends ChannelInitializer<Channel> {

    public final boolean client;

    public HttpPipelineInitializer(boolean client) {
        this.client = client;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (client) {
            // 如果是客户端
            // 解码来自服务器的响应
            pipeline.addLast("decoder", new HttpResponseDecoder());
            // 编码向服务器发送的数据
            pipeline.addLast("encoder", new HttpRequestEncoder());
        } else {
            // 如果是服务器端
            // 解码来自客户端的请求
            pipeline.addLast("decoder", new HttpRequestDecoder());
            // 编码发给客户端的响应
            pipeline.addLast("encoder", new HttpResponseEncoder());
        }
    }
}
