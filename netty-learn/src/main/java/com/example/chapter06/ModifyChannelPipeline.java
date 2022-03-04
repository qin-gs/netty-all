package com.example.chapter06;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DummyChannelPipeline;

public class ModifyChannelPipeline {
    public static final ChannelPipeline CHANNEL_PIPELINE_FROM_SOMEWHERE = DummyChannelPipeline.DUMMY_INSTANCE;

    public static void modifyPipeline() {
        ChannelPipeline pipeline = CHANNEL_PIPELINE_FROM_SOMEWHERE;

        FirstHandler firstHandler = new FirstHandler();
        // 添加 ChannelHandler
        pipeline.addLast("handler1", firstHandler);
        pipeline.addFirst("handler2", new SecondHandler());
        pipeline.addLast("handler3", new ThirdHandler());

        // 移除 ChannelHandler，根据 名称 或 类型
        pipeline.remove("handler3");
        pipeline.remove(firstHandler);

        // 替换 ChannelHandler
        pipeline.replace("handler2", "handler4", new FourthHandler());


    }


    private static final class FirstHandler extends ChannelHandlerAdapter {

    }

    private static final class SecondHandler extends ChannelHandlerAdapter {

    }

    private static final class ThirdHandler extends ChannelHandlerAdapter {

    }

    private static final class FourthHandler extends ChannelHandlerAdapter {

    }
}
