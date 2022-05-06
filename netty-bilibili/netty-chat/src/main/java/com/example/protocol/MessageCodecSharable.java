package com.example.protocol;

import com.example.config.Config;
import com.example.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * 该类是可共享的，需要和 LengthFieldBasedFrameDecoder 一起使用，避免 粘包/半包 问题
 */
@Slf4j
@ChannelHandler.Sharable
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf, Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
        // 创建一个 ByteBuf 往里面写数据
        ByteBuf out = ctx.alloc().buffer();
        // 魔数
        out.writeBytes(new byte[]{1, 2, 3, 4});
        // 版本
        out.writeByte(1);
        // 序列化方式 jdk 0, json 1
        out.writeByte(Config.getSerializerAlgorithm().ordinal());
        // 字节的指令类型 (登录，聊天，建群等)
        out.writeByte(msg.getMessageType());
        // 序列id
        out.writeInt(msg.getSequenceId());
        // 对齐填充 (将前面数据的内容填充成 12 字节)
        out.writeByte(0xff);
        // 序列化
        byte[] bytes = Config.getSerializerAlgorithm().serialize(msg);

        // 长度
        out.writeInt(bytes.length);
        // 内容
        out.writeBytes(bytes);


        // 传递出去
        outList.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magicNum = in.readInt();
        byte version = in.readByte();
        byte serializerType = in.readByte();
        byte messageType = in.readByte();
        int sequenceId = in.readInt();
        // 取出对齐填充的那个字节
        in.readByte();

        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length);

        // 找到反序列化算法
        Serializer.Algorithm algorithm = Serializer.Algorithm.values()[serializerType];
        // 确定消息类型 (Message 是抽象的)
        Class<? extends Message> messageClass = Message.getMessageClass(messageType);
        // 进行反序列化
        Message message = algorithm.deserialize(messageClass, bytes);

        log.debug("{}, {}, {}, {}, {}, {}", magicNum, version, serializerType, messageType, sequenceId, length);
        log.debug("{}", message);

        out.add(message);
    }
}
