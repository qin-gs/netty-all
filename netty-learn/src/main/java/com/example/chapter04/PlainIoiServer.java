package com.example.chapter04;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class PlainIoiServer {

    public void server(int port) throws IOException {
        // 将服务器连接到指定端口
        ServerSocket socket = new ServerSocket(port);
        for (;;) {
            // 接受连接
            Socket clientSocket = socket.accept();
            System.out.println("accepted connection from " + clientSocket);

            // 创建一个线程处理连接
            new Thread(() -> {
                try (OutputStream out = clientSocket.getOutputStream()){
                    // 将消息写给已连接的客户端
                    out.write("hi!\n".getBytes(StandardCharsets.UTF_8));
                    out.flush();
                    // 关闭连接
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
