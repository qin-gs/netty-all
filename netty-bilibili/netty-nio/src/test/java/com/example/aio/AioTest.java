package com.example.aio;

import com.example.util.ByteBufferUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 异步 io
 */
public class AioTest {

    @Test
    void test() throws IOException {
        AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get("src/test/resources/data.txt"), StandardOpenOption.READ);
        ByteBuffer buffer = ByteBuffer.allocate(32);
        System.out.println("begin");
        channel.read(buffer, 0, buffer, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                attachment.flip();
                ByteBufferUtil.printAll(attachment);
                System.out.println("read over");
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                exc.printStackTrace();
            }
        });
        System.out.println("finished");

        System.in.read();
    }
}
