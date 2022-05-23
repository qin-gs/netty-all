package com.example.source;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;

public class EventLoopTest {

    public static void main(String[] args) {
        EventLoop next = new NioEventLoopGroup().next();
        next.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("hi");
            }
        });
    }
}
