package com.example.chapter12;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * 处理 FullHttpRequest 消息
 */
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final String wsUri;
    private static final File INDEX;

    static {
        URL location = HttpRequestHandler.class.getProtectionDomain().getCodeSource().getLocation();

        try {
            String path = location.toURI() + "index.html";
            path = !path.contains("file:") ? path : path.substring(5);
            INDEX = new File(path);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("unable to locate index.html", e);
        }
    }

    public HttpRequestHandler(String wsUri) {
        this.wsUri = wsUri;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        // 如果请求了 WebSocket 协议升级，增加引用次数，传递给下一个 Handler
        if (wsUri.equalsIgnoreCase(request.getUri())) {
            ctx.fireChannelRead(request.retain());
        } else {
            // 处理 100 响应码
            if (HttpHeaders.is100ContinueExpected(request)) {
                // 发送一个 100 响应
                send100Continue(ctx);
            }
            // 读取 index.html
            RandomAccessFile file = new RandomAccessFile(INDEX, "r");
            DefaultHttpResponse response = new DefaultHttpResponse(request.getProtocolVersion(), HttpResponseStatus.OK);
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
            boolean keepAlive = HttpHeaders.isKeepAlive(request);
            // 如果请求 keep-alive，添加需要的 http 头信息
            if (keepAlive) {
                response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, file.length());
                response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            }
            ctx.write(response);
            // 检查是否有 SslHandler，没有的话使用 ChunkedNioFile
            if (ctx.pipeline().get(SslHandler.class) == null) {
                ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
            } else {
                ctx.write(new ChunkedNioFile(file.getChannel()));
            }
            // 写入一个响应结束标记
            ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            // 如果没有 keep-alive，添加一个监听器，写操作完成后关闭 Channel
            if (!keepAlive) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    /**
     * 发送一个 100 响应
     */
    private static void send100Continue(ChannelHandlerContext ctx) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
