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
 * 处理 FullHttpRequest 消息，
 * 只管理 http 的请求 和 响应
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
            // 之所以需要调用retain()方法,是因为调用channelRead()方法完成之后，
            // 它将调用FullHttpRequest对象上的release()方法以释放它的资源
            ctx.fireChannelRead(request.retain());
        } else {
            // 处理 100 响应码
            if (HttpUtil.is100ContinueExpected(request)) {
                // 发送一个 100 响应
                send100Continue(ctx);
            }
            // 读取 index.html
            RandomAccessFile file = new RandomAccessFile(INDEX, "r");
            // 创建一个响应对象
            DefaultHttpResponse response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
            boolean keepAlive = HttpUtil.isKeepAlive(request);
            // 如果请求 keep-alive，添加需要的 http 头信息
            if (keepAlive) {
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }
            // 将 response 写到客户端
            ctx.write(response);
            // 检查是否有 SslHandler，没有的话使用 ChunkedNioFile
            if (ctx.pipeline().get(SslHandler.class) == null) {
                // 将 index.html 写到客户端
                // 使用 DefaultFileRegion 零拷贝特性
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
