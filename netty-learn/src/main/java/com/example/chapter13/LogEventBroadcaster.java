package com.example.chapter13;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class LogEventBroadcaster {

    private final NioEventLoopGroup group;
    private final Bootstrap bootstrap;
    private final File file;

    public LogEventBroadcaster(InetSocketAddress address, File file) {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        // 引导 NioDatagramChannel 无连接
        bootstrap.group(group).channel(NioDatagramChannel.class)
                // 设置套接字选项
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new LogEventEncoder(address));
        this.file = file;
    }

    public void run() throws InterruptedException, IOException {
        // 绑定 Channel
        Channel ch = bootstrap.bind(0).sync().channel();
        long pointer = 0;
        for (; ; ) {
            long len = file.length();
            if (len < pointer) {
                // 如果有必要将文件指针设置到该文件的最后一个字节
                pointer = len;
            } else if (len > pointer) {
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                // 设置当前的文件指针，确保没有任何旧日志被发送
                raf.seek(pointer);
                String line;
                while ((line = raf.readLine()) != null) {
                    // 对于每条日志，写入一个 LogEvent 到 Channel 中
                    ch.writeAndFlush(new LogEvent(null, file.getAbsolutePath(), line, -1));
                }
                // 存储其在文件中的当前位置
                pointer = raf.getFilePointer();
                raf.close();
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.interrupted();
                break;
            }

        }
    }

    public void stop() {
        group.shutdownGracefully();
    }

    public static void main(String[] args) throws IOException {
        LogEventBroadcaster broadcaster = new LogEventBroadcaster(
                new InetSocketAddress("255.255.255.255", 8080),
                new File("F:\\info.log")
        );
        try {
            broadcaster.run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            broadcaster.stop();
        }
    }
}
