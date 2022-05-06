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

}