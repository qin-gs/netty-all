package com.example.nio;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * 客户端
 */
public class ClientTest {

    @Test
    void client() throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress("localhost", 8080));
        channel.write(StandardCharsets.UTF_8.encode("hello\nworld"));
        System.out.println("channel.getLocalAddress() = " + channel.getLocalAddress());
        // channel.close();

        System.in.read();
    }
}
