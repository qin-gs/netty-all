package com.example.nio;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class WriteClientTest {

    @Test
    void test() throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 8080));

        int all = 0;
        // 接收数据
        while (true) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            all += sc.read(buffer);
            buffer.clear();
            System.out.println("all = " + all);
        }
    }
}
