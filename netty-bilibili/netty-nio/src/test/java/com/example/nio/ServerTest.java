package com.example.nio;

import com.example.util.ByteBufferUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务器
 */
@DisplayName("单线程 nio 阻塞模式")
public class ServerTest {

    Logger log = LoggerFactory.getLogger(ServerTest.class);

    @Test
    void server() throws IOException {
        // 创建服务器
        ServerSocketChannel channel = ServerSocketChannel.open();
        // 改为非阻塞模式
        channel.configureBlocking(false);
        // 绑定端口
        channel.bind(new InetSocketAddress(8080));
        List<SocketChannel> channels = new ArrayList<>();
        while (true) {
            // 建立与客户端的连接，与客户端通信
            // 等待连接，线程被阻塞，连接建立之后继续运行
            SocketChannel accept = channel.accept();
            // 切换成非阻塞模式
            if (accept != null) {
                accept.configureBlocking(false);
                channels.add(accept);
                log.debug(accept.toString());

                for (SocketChannel ch : channels) {
                    ByteBuffer buffer = ByteBuffer.allocate(32);
                    // read 也会阻塞，客户端发送数据 后才会继续运行
                    int read = ch.read(buffer);
                    if (read > 0) {
                        // 切换到读模式
                        buffer.flip();
                        ByteBufferUtil.printAll(buffer);
                        // 切换回写模式
                        buffer.clear();
                    }
                }
            }
        }
    }
}
