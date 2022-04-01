package com.example.nio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

@DisplayName("FileChannel 功能测试")
public class FileChannelTest {

    /**
     * 数据传输
     */
    @Test
    void transfer() {

        try (FileChannel from = new FileInputStream("src/test/resources/data.txt").getChannel();
             FileChannel to = new FileOutputStream("src/test/resources/data-to.txt").getChannel()) {

            // 会使用零拷贝进行优化，大小限制为 2g，所以需要多次传输
            long size = from.size();
            // left 记录还剩余多少字节
            for (long left = size; left > 0;) {
                left -= from.transferTo(size - left, left, to);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
