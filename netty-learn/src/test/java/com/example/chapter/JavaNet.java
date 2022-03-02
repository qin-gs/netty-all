package com.example.chapter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

public class JavaNet {

    /**
     * 网络编程
     * <p>
     * 1. 任何时候都可能有大量线程处于休眠状态
     * 2. 需要为每个线程的调用栈都分配内存
     * 3. 存在上下文切换开销
     */
    public void test1() throws IOException {

        // 创建一个 ServerSocket，监听指定端口上的连接请求
        ServerSocket serverSocket = new ServerSocket(8080);
        // accept 方法会被阻塞，直到连接建立，返回一个新的 Socket 用于客户端 和 服务器 通信
        Socket clientSocket = serverSocket.accept();

        // 返回的 Socket 的输入输出流
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String request, response;
        // readLine 方法会被阻塞，直到 换行符 或 回车
        while ((request = in.readLine()) != null) {
            if ("done".equals(request)) {
                break;
            }
            // response = processRequest(request);
            response = "";
            out.println(response);
        }
    }

    public void test2(Channel channel) {
        // 连接到远程节点
        ChannelFuture future = channel.connect(new InetSocketAddress("192.168.0.1", 8080));
        // 注册一个监听器，操作完成后获得通知
        // 如果注册之前操作已经完成了，会被直接通知
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                // 如果操作成功，将数据发送到远程节点
                if (channelFuture.isSuccess()) {
                    ByteBuf buffer = Unpooled.copiedBuffer("hello", Charset.defaultCharset());
                    channelFuture.channel().writeAndFlush(buffer);
                } else {
                    // 如果操作失败，获取原因
                    Throwable cause = channelFuture.cause();
                    cause.printStackTrace();
                }
            }
        });

    }
}
