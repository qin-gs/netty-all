## Netty



### NIO 基础

non-blocking io 非阻塞io

- Channel 通道

  读写数据的双向通道 (可以从 channel 读，也可以写入 channel)

  - FileChannel 文件，工作在阻塞模式下
  - DatagramChannel udp编程
  - SocketChannel tcp编程
  - ServerSocketChannel tcp 服务器端

- Buffer 数据缓冲区

  - ByteBuffer

    ```java
    // 向缓冲区 buffer 中写入数据
    channel.read(buffer);
    // 将缓存区切换至读模式
    buffer.flip();
    // 读取缓冲区的数据
    buffer.get();
    // 将缓存区切换至写模式
    buffer.clear();
    // ... 重复读取
    ```

    不能被多个 Channel 共同使用，需要为每个 Channel 都配置一个独立的 buffer

  - Short/Int/Long/Float/Double/CharBuffer

- Selector

  多线程的问题：内存占用高；上下文切换成本高；适合连接数少的场景

  线程池问题：阻塞模式下只能处理一个 socket 连接；适合短连接场景

  Selector 使用一个线程管理多个 Channel，Channel 都工作在非阻塞模式下，适合连接数多流量低的场景

