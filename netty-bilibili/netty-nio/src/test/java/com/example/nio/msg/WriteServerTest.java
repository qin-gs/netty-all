package com.example.nio.msg;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class WriteServerTest {

    @Test
    void test() throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        ssc.bind(new InetSocketAddress(8080));

        while (true) {
            selector.select();

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    // 发送大量数据
                    String message = getMessage();
                    ByteBuffer buffer = Charset.defaultCharset().encode(message);
                    while (buffer.hasRemaining()) {
                        // 写入数据，返回实际写入的字节数
                        int write = sc.write(buffer);
                        System.out.println("write = " + write);
                    }
                }
            }
        }
    }

    /**
     * 准备大量数据
     */
    String getMessage() {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < 1_0000_0000; i++) {
            buffer.append(i);
        }
        return buffer.toString();
    }
}
