package com.example.chapter10;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.*;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WebSocketConvertHandler extends MessageToMessageCodec<WebSocketFrame, WebSocketConvertHandler.MyWebSocketFrame> {

    /**
     * 编码：MyWebSocketFrame  ->  WebSocketFrame
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, MyWebSocketFrame msg, List<Object> out) throws Exception {
        ByteBuf payload = msg.getData().duplicate().retain();
        switch (msg.getType()) {
            case BINARY: {
                out.add(new BinaryWebSocketFrame(payload));
                break;
            }
            case TEXT: {
                out.add(new TextWebSocketFrame(payload));
                break;
            }
            case CLOSE: {
                out.add(new CloseWebSocketFrame(true, 0, payload));
                break;
            }
            case CONTINUATION: {
                out.add(new ContinuationWebSocketFrame(payload));
                break;
            }
            case PONG: {
                out.add(new PongWebSocketFrame(payload));
                break;
            }
            case PING: {
                out.add(new PingWebSocketFrame(payload));
                break;
            }
            default: {
                throw new IllegalStateException("unsupported websocket msg: " + msg);
            }
        }
    }

    /**
     * 解码：WebSocketFrame  ->  MyWebSocketFrame
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) throws Exception {
        ByteBuf payload = msg.content().duplicate().retain();
        Map<Class<WebSocketFrame>, MyWebSocketFrame.FrameType> map = MapUtils.putAll(new HashMap<>(8),
                new Object[]{
                        BinaryWebSocketFrame.class, MyWebSocketFrame.FrameType.BINARY,
                        CloseWebSocketFrame.class, MyWebSocketFrame.FrameType.CLOSE,
                        PingWebSocketFrame.class, MyWebSocketFrame.FrameType.PING,
                        PongWebSocketFrame.class, MyWebSocketFrame.FrameType.PONG,
                        TextWebSocketFrame.class, MyWebSocketFrame.FrameType.TEXT,
                        ContinuationWebSocketFrame.class, MyWebSocketFrame.FrameType.CONTINUATION,
                });
        Optional<MyWebSocketFrame.FrameType> frameType = Optional.ofNullable(map.get(msg.getClass()));
        if (frameType.isPresent()) {
            out.add(new MyWebSocketFrame(map.get(msg.getClass()), payload));
        } else {
            throw new IllegalStateException("unsupported websocket msg " + msg);
        }
    }

    static class MyWebSocketFrame {
        public enum FrameType {
            BINARY, CLOSE, PING, PONG, TEXT, CONTINUATION
        }

        private final FrameType type;
        private final ByteBuf data;

        public MyWebSocketFrame(FrameType type, ByteBuf data) {
            this.type = type;
            this.data = data;
        }

        public FrameType getType() {
            return type;
        }

        public ByteBuf getData() {
            return data;
        }

    }
}
