package com.example.chapter04;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class PlainNioServer {

    public void server(int port) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        ServerSocket socket = serverChannel.socket();
        InetSocketAddress address = new InetSocketAddress(port);
        // 将服务器绑定到指定端口
        socket.bind(address);

        // 打开 Selector 处理 Channel
        Selector selector = Selector.open();
        // 将 ServerSocketChannel 注册到 Selector 接受连接
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        ByteBuffer msg = ByteBuffer.wrap("hi!\n\r".getBytes(StandardCharsets.UTF_8));

        for (; ; ) {
            // 等待需要处理的事件，一直阻塞直到下一个事件传入
            selector.select();
            // 获取所有接收事件的 SelectionKey 实例
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                try {
                    // 检查事件是否为一个新的已就绪可以被接受的连接
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        // 接受客户端，注册到选择器
                        client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, msg.duplicate());
                        System.out.println("accepted connection from " + client);
                    }
                    // 检查套接字是否已准备好写数据
                    if (key.isWritable())  {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        while (buffer.hasRemaining()) {
                            // 将数据写入已连接的客户端
                            if (client.write(buffer) == 0) {
                                break;
                            }
                        }
                        // 关闭连接
                        client.close();
                    }
                } catch (IOException e) {
                    key.cancel();
                    key.channel().close();
                }
            }
        }


    }
}
