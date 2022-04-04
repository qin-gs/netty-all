package com.example.nio;

import com.example.util.ByteBufferUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * 用 Selector 管理 Channel
 */
public class SelectorTest {
    Logger log = LoggerFactory.getLogger(SelectorTest.class);

    /**
     * 检测多个 Channel 有没有事件
     */
    @Test
    void selector() throws IOException {
        // 创建 Selector
        Selector selector = Selector.open();
        // 创建 Channel
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        channel.bind(new InetSocketAddress(8080));

        // 关联 Selector 和 Channel
        // 可以通过 SelectionKey 知道哪个 Channel 的事件，什么事件
        SelectionKey selectionKey = channel.register(selector, 0, null);
        // 关注 accept 事件
        selectionKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug(selectionKey.toString());

        while (true) {
            // 没有事件则阻塞，有事件才会恢复运行
            // 如果有事件未处理，不会阻塞；cancel 之后会恢复阻塞
            selector.select();
            // 处理事件，获取所有可用事件
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                // 处理的 key 需要手动移除掉
                iterator.remove();
                log.debug(key.toString());

                // 区分事件类型
                if (key.isAcceptable()) {
                    // 得到是哪个 Channel 的事件
                    ServerSocketChannel ch = (ServerSocketChannel) key.channel();
                    SocketChannel accept = ch.accept();
                    accept.configureBlocking(false);
                    log.debug(accept.toString());

                    // 将 Channel 注册进去
                    SelectionKey acceptKey = accept.register(selector, 0, null);
                    acceptKey.interestOps(SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    try {
                        // 读取数据
                        SocketChannel ch = (SocketChannel) key.channel();
                        // 需要处理消息边界
                        ByteBuffer buffer = ByteBuffer.allocate(32);
                        // 正常断开，返回值为 -1
                        int read = ch.read(buffer);
                        if (read == -1) {
                            key.cancel();
                        } else {
                            buffer.flip();
                            System.out.println(StandardCharsets.UTF_8.decode(buffer));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        // 如果客户端出问题，需要取消
                        key.cancel();
                    }
                }
            }
        }
    }
}
