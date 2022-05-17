package com.example.chapter13;

import java.net.InetSocketAddress;

/**
 * 消息组件
 */
public class LogEvent {
    public static final byte SEPARATOR = ':';
    private final InetSocketAddress source;
    /**
     * 文件名
     */
    private final String logfile;
    /**
     * 消息内容
     */
    private final String msg;
    /**
     * 接收时间
     */
    private final long received;

    /**
     * 用于传出消息
     */
    public LogEvent(String logfile, String msg) {
        this(null, logfile, msg, -1);
    }

    /**
     * 用于传入消息
     */
    public LogEvent(InetSocketAddress source, String logfile, String msg, long received) {
        this.source = source;
        this.logfile = logfile;
        this.msg = msg;
        this.received = received;
    }

    public InetSocketAddress getSource() {
        return source;
    }

    public String getLogfile() {
        return logfile;
    }

    public String getMsg() {
        return msg;
    }

    public long getReceived() {
        return received;
    }

    public long getReceivedTimestamp() {
        return received;
    }

    @Override
    public String toString() {
        return "LogEvent{" +
                "source=" + source +
                ", logfile='" + logfile + '\'' +
                ", msg='" + msg + '\'' +
                ", received=" + received +
                '}';
    }
}
