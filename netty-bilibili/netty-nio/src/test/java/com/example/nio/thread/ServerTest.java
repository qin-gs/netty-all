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

public class ServerTest {

    private static final Logger log = LoggerFactory.getLogger(ServerTest.class);

    @Test
    void test() throws IOException, InterruptedException {
        Thread.currentThread().setName("master");
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));

        Selector master = Selector.open();
        SelectionKey masterKey = ssc.register(master, 0, null);
        masterKey.interestOps(SelectionKey.OP_ACCEPT);

        Worker[] workers = new Worker[2];
        Arrays.setAll(workers, i -> new Worker("worker - " + i));
        AtomicInteger index = new AtomicInteger(0);

        while (true) {
            master.select();
            Iterator<SelectionKey> iterator = master.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
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
