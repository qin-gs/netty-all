package com.example.chapter11;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

/**
 * 添加 ssl 支持
 */
public class SslChannelInitializer extends ChannelInitializer<Channel> {

    private final SslContext context;
    private final boolean startTls;

    public SslChannelInitializer(SslContext context, boolean startTls) {
        this.context = context;
        // 如果设置为 true，第一个写入的消息将不会被加密（客户端应该设置为 true）
        this.startTls = startTls;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        SSLEngine sslEngine = context.newEngine(ch.alloc());
        // 将 SslHandler 作为第一个 ChannelHandler 添加到 ChannelPipeline 中
        // 确保其他 ChannelHandler 将逻辑应用到数据之后，才进行加密
        ch.pipeline().addFirst("ssl", new SslHandler(sslEngine, startTls));
    }
}
