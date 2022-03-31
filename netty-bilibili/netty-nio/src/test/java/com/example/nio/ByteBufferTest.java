package com.example.nio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

import static com.example.util.ByteBufferUtil.printAll;

@DisplayName("ByteBuffer 简单测试")
public class ByteBufferTest {

    @Test
    void test() {
        try (FileChannel channel = new FileInputStream("src/test/resources/data.txt").getChannel()) {
            // 准备一个缓存区
            ByteBuffer buffer = ByteBuffer.allocate(10);
            // 从 channel 读取数据，写入上面声明的 buffer
            int read = channel.read(buffer);
            while (read > 0) {
                // 切换成读模式
                buffer.flip();
                // 判断是否还有剩余数据，每次一个字节读出来
                while (buffer.hasRemaining()) {
                    byte b = buffer.get();
                    System.out.println(((char) b));
                }
                // 切换成写模式
                buffer.clear();
                read = channel.read(buffer);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void readByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(((byte) 'a'));
        printAll(buffer);

        buffer.put(((byte) 'b'));
        buffer.put(((byte) 'c'));
        buffer.put(((byte) 'd'));
        printAll(buffer);

        // 必须切换到读模式才能读出来数据
        buffer.flip();
        System.out.println(buffer.get());

        // 切换到写模式，将未读取往前移
        buffer.compact();
        printAll(buffer);

        // 继续往后写
        buffer.put(new byte[]{'e', 'f'});
        printAll(buffer);
    }

    @Test
    void allocateTest() {
        // class java.nio.HeapByteBuffer 堆内存，读写效率低，受到 gc 影响
        System.out.println(ByteBuffer.allocate(16).getClass());
        // class java.nio.DirectByteBuffer 直接内存，读写效率高，分配比较慢，不受 gc 影响
        System.out.println(ByteBuffer.allocateDirect(16).getClass());
    }

    @Test
    void read() {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.put("hello world".getBytes(StandardCharsets.UTF_8));
        buffer.flip();

        // 将数据读到一个数组中
        byte[] dst = new byte[4];
        buffer.get(dst);
        printAll(buffer);

        // 从头再次读，本来应该读到 o 的，这里又从头读了是 h
        buffer.rewind();
        System.out.println(((char) buffer.get()));

        // mark：做一个标记，记录 position 的位置
        // reset：将 position 重置到 mark 位置
        System.out.println(((char) buffer.get())); // e
        System.out.println(((char) buffer.get())); // l
        System.out.println(((char) buffer.get())); // l
        // 做一个标记
        buffer.mark();
        System.out.println(((char) buffer.get())); // o
        System.out.println(((char) buffer.get())); //
        // 回到标记位置，再次读取
        buffer.reset();
        System.out.println(((char) buffer.get())); // o
        System.out.println(((char) buffer.get())); //

        // get(index) 不改变索引位置
        System.out.println(((char) buffer.get(1)));
    }

    /**
     * ByteBuffer  <-->  String
     */
    @Test
    void toStringTest() {
        // String -> ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(16).put("hello world".getBytes(StandardCharsets.UTF_8));
        printAll(buffer);

        // 转换转换会自动转换成读模式
        ByteBuffer hello = StandardCharsets.UTF_8.encode("hello");
        printAll(hello);

        // 字段转到读模式
        ByteBuffer wrap = ByteBuffer.wrap("hello".getBytes(StandardCharsets.UTF_8));
        printAll(wrap);
    }

}
