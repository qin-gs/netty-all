package com.example.chapter11;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.LineBasedFrameDecoder;

/**
 * 添加解码器
 * 1. 传入数据流是一系列的帧，每个帧都由换行符（\n）分隔；
 * 2. 每个帧都由一系列的元素组成，每个元素都由单个空格字符分隔；
 * 3. 一个帧的内容代表一个命令，定义为一个命令名称后跟着数目可变的参数。
 */
public class CmdHandlerInitializer extends ChannelInitializer<Channel> {

    private static final byte SPACE = ' ';

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 添加解码器，提取 Cmd 对象，转发给下一个 Handler
        pipeline.addLast(new CmdDecoder(64 * 1024));
        // 处理上面传过来的 Cmd 对象
        pipeline.addLast(new CmdHandler());
    }

    public static final class Cmd {
        private final ByteBuf name;
        private final ByteBuf args;

        public Cmd(ByteBuf name, ByteBuf args) {
            this.name = name;
            this.args = args;
        }

        public ByteBuf getName() {
            return name;
        }

        public ByteBuf getArgs() {
            return args;
        }
    }

    public static final class CmdDecoder extends LineBasedFrameDecoder {

        public CmdDecoder(int maxLength) {
            super(maxLength);
        }

        @Override
        protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
            ByteBuf frame = (ByteBuf) super.decode(ctx, buffer);
            if (frame == null) {
                return null;
            }
            //查找第一个空格字符的索引，
            int index = frame.indexOf(frame.readerIndex(), frame.writerIndex(), SPACE);
            return new Cmd(frame.slice(frame.readerIndex(), index),
                    frame.slice(index + 1, frame.writerIndex()));
        }
    }

    public static final class CmdHandler extends SimpleChannelInboundHandler<Cmd> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Cmd msg) throws Exception {
            // 处理 Cmd 对象
        }
    }
}
