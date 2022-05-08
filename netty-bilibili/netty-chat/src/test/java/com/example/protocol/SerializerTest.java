package com.example.protocol;

import com.example.message.LoginRequestMessage;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LoggingHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SerializerTest {

    public static void main(String[] args) {
        EmbeddedChannel channel = new EmbeddedChannel(
                new LoggingHandler(),
                new MessageCodecSharable(),
                new LoggingHandler());

        LoginRequestMessage message = new LoginRequestMessage("tom", "jerry");
        channel.writeOutbound(message);

    }

    /**
     * 虚拟机参数中添加 --add-opens java.base/jdk.internal.misc=ALL-UNNAMED --illegal-access=warn
     * 否则会抛异常
     * java.lang.IllegalAccessException: class io.netty.util.internal.PlatformDependent0$6
     * cannot access class jdk.internal.misc.Unsafe (in module java.base)
     * because module java.base does not export jdk.internal.misc to unnamed module @29d80d2b
     */
    @Test
    void test() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new LoggingHandler(),
                new MessageCodecSharable(),
                new LoggingHandler());

        LoginRequestMessage message = new LoginRequestMessage("tom", "jerry");
        channel.writeOutbound(message);

    }

}