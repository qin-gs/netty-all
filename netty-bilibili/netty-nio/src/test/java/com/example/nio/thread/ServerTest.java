package com.example.nio.thread;

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
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用 selector 处理线程一直运行的问题
 */
public class ServerTest {

    private static final Logger log = LoggerFactory.getLogger(ServerTest.class);

    @Test
    void test() throws IOException, InterruptedException {
        Thread.currentThread().setName("master");
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));

        // 来管理多个 channel
        Selector master = Selector.open();
        // 建立 selector 和 channel 的联系
        // 事件发送后，通过 SelectionKey 得到具体的事件 和 来自哪个channel
        SelectionKey masterKey = ssc.register(master, 0, null);
        // 关注 accept 事件 (在有连接请求时触发)
        masterKey.interestOps(SelectionKey.OP_ACCEPT);

        Worker[] workers = new Worker[2];
        Arrays.setAll(workers, i -> new Worker("worker - " + i));
        AtomicInteger index = new AtomicInteger(0);

        while (true) {
            // 没有事件发生时会阻塞
            // 有未处理事件时也不会阻塞
            master.select();
            // 处理事件，获取所有发生的事件
            Iterator<SelectionKey> iterator = master.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                // 判断事件类型，如果是 accept事件
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);

                    // 读写事件，交给 worker 完成
                    workers[index.getAndIncrement() % workers.length].register(sc);
                    // sc.register(worker.selector, SelectionKey.OP_READ, null); // master
                }
            }
        }
    }

    /**
     * 检测读写事件
     */
    static class Worker implements Runnable {
        private Thread thread;
        private Selector selector;
        private String name;
        private volatile boolean isInit = false;

        public Worker(String name) {
            this.name = name;
        }

        public void register(SocketChannel sc) throws IOException {
            if (!isInit) {
                selector = Selector.open();

                thread = new Thread(this, name);
                thread.start();

                isInit = true;
            }
            // 唤醒 select
            selector.wakeup();
            sc.register(selector, SelectionKey.OP_READ, null);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    selector.select(); // worker
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (key.isReadable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(32);
                            SocketChannel channel = (SocketChannel) key.channel();
                            channel.read(buffer);
                            log.info("after read");
                            buffer.flip();
                            ByteBufferUtil.printAll(buffer);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
