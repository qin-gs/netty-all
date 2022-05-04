package com.example.protocol;

import com.example.message.LoginRequestMessage;
import com.example.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * 该类不能被 @ChannelHandler.Sharable 修饰 (父类中进行了限制)，
 * 这里会产生 粘包/半包 问题
 */
@Slf4j
public class MessageCodec extends ByteToMessageCodec<Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        // 魔数
        out.writeBytes(new byte[]{1, 2, 3, 4});
        // 版本
        out.writeByte(1);
        // 序列化方式 jdk 0, json 1
        out.writeByte(0);
        // 字节的指令类型 (登录，聊天，建群等)
        out.writeByte(msg.getMessageType());
        // 序列id
        out.writeInt(msg.getSequenceId());
        // 对齐填充 (将前面数据的内容填充成 12 字节)
        out.writeByte(0xff);
        // 序列化
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(msg);
        byte[] bytes = baos.toByteArray();

        // 长度
        out.writeInt(bytes.length);
        // 内容
        out.writeBytes(bytes);
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
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Message message = (Message) ois.readObject();

        log.debug("{}, {}, {}, {}, {}, {}", magicNum, version, serializerType, messageType, sequenceId, length);
        log.debug("{}", message);

        out.add(message);
    }

    public static void main(String[] args) throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(
                // 处理 粘包/半包 问题
                // 最长，长度字段的偏移量，长度本身占几个字节，长度调整，去除几个字节
                new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0),
                new LoggingHandler(),
                new MessageCodec());

        // 出站
        LoginRequestMessage message = new LoginRequestMessage("tom", "jerry");
        // channel.writeOutbound(message);

        // 入站
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null, message, buf);
        // channel.writeInbound(buf);


        // ----------
        // 测试 半包，如果只发送 100 字节，会出现问题
        ByteBuf slice1 = buf.slice(0, 100);
        ByteBuf slice2 = buf.slice(100, buf.readableBytes() - 100);

        // 将 引用计数 + 1，不然第一次写被释放，第二次写会报错
        buf.retain();
        channel.writeInbound(slice1); // 会将 引用计数-1
        channel.writeInbound(slice2);

    }
}
