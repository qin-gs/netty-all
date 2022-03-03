### Netty

#### 1. 异步 和 事件驱动

NIO

![使用Selector的非阻塞IO](../img/使用Selector的非阻塞IO.png)

Netty 通过触发事件将 Selector 从应用程序中抽象出来，消除了本来需要手动编写的派发代码。在内部，为每个 Channel 分配一个 EventLoop(本身由一个线程驱动，无需考虑同步问题)，用来处理所有的事件

netty 的**核心组件**

- Channel

  可以看作是传入 或 传出 数据的载体，可以被打开或关闭

- 回调

  操作完成后通知相关方

- Future

  ChannelFuture 异步操作结果的占位符

  ChannelFutureListener 监听操作是否完成，完成后调用，避免了手动检查操作是否完成

- 事件 和 ChannelHandler

  基于已发生的事件触发适当的动作

    - 入站事件：李恩杰已被激活或连接失活，数据读取，用户事件，错误事件
    - 出站事件：打开或关闭到远程节点的连接，将数据写入套接字

  入站事件 -> 入站处理器

  出站事件 -> 出站处理器

  ChannelHandler 处理器的基本抽象

#### 2. 应用实例

**服务端**

1. ChannelHandler 和 业务逻辑

    - 针对不同类型的事件调用 ChannelHandler
    - 应用程序通过实现 或 扩展 ChannelHandler 来挂钩到事件的生命周期，提供自定义的应用程序逻辑
    - 架构上，ChannelHandler 有助于报错业务逻辑 和 网络处理代码 的分离。简化了开发过程

![服务端的ChannelHandler](../img/服务端的ChannelHandler.png)

**客户端**

![客户端的ChannelHandler](../img/客户端的ChannelHandler.png)



#### 3. netty 组件 和 设计



##### 3.1 Channel, EventLoop, ChannelFuture

- Channel: Socket

  基本 IO 操作：bind, connect, read, write

  Channel 接口提供的 api 简化 java 原生 Socket 的复杂性



- EventLoop: 控制流，多线程处理，并发

  定义 Netty 的核心抽象，处理连接的生命周期中发生的事件

  一个给定 Channel 的所有 IO 操作全部都由一个 Thread 执行

  - 一个 EventLoopGroup 包含一个或者多个 EventLoop；
  - 一个 EventLoop 在它的生命周期内只和一个 Thread 绑定；
  - 所有由 EventLoop 处理的 I/O 事件都将在它专有的 Thread 上被处理；
  - 一个 Channel 在它的生命周期内只注册于一个 EventLoop；
  - 一个 EventLoop 可能会被分配给一个或多个 Channel。



- ChannelFuture: 异步通知

  对于异步操作，在特定的时间段确定其结果



##### 3.2 ChannelHandler, ChannelPipeline

- ChannelHandler

  处理出站 和 入站 数据的应用程序逻辑的容器

  ChannelInboundHandler: 接收入站事件和数据

- ChannelPipeline

  提供了 ChannelHandler 链的容器，并定义了用于该链上传播入站 和 出站 时间流的 api。Channel 被创建时，会自动分派到专属的 Pipeline

  ChannelPipeline 是 ChannelHandler 的编排顺序，使事件流经 ChannelPipeline 是 ChannelHandler 的工作

  将 ChannelHandler 安装到 ChannelPipeline 的过程：

  - 一个 ChannelInitializer 的实现被注册到 ServerBootstrap
  - 当 ChannelInitializer#initChannel 被调用时，ChannelInitializer 将在 ChannelPipeline 中安装一组自定义的 ChannelHandler
  - ChannelInitializer 将自己从 ChannelPipeline 中移除

ChannelHandler 被添加到 ChannelPipeline 时，会被分配一个 ChannelHandlerContext

ChannelHandlerAdapter 作为 ChannelHandler 的默认实现，可以只重写需要的方法 或 事件



- 编码器 / 解码器

  发送 或 接收消息时，可能需要进行数据转换

  从入站 Channel 中读取消息时，ChannelInBoundHandler#channelRead 方法会被调用

  扩展 SimpleChannelInBoundHandler<T>  完成解码



**引导**

- Bootstrap: 用于客户端，连接到远程主机端口，需要一个 EventLoopGroup
- ServerBootstrap: 用于服务端，绑定到一个本地端口，需要两个 EventLoopGroup
  - 第一个只包含一个 ServerChannel，代表服务器自身的已绑定到某个本地端口的正在监听的台阶在
  - 第二个将包含所有已创建的用来处理传入客户端连接(每个服务器已接受的连接都有一个)的 Channel



#### 4. 传输

每个 Channel 都被被分配一个 ChannelPipeline 和 ChannelConfig(包含该 Channel 的所有配置)

ChannelPipeline 持有所有 ChannelHandler(应用与入站 和 出站数据 和 事件)

- 转换数据格式
- 提供异常通知
- 提供 Channel 变成获得 或 非活动的通知
- 提供 Channel 注册 或 注销 到 EventLoop 的通知
- 提供用户自定义事件的通知

内置的传输

- NIO: 基于选择器(java.nio.channels)

  所有 IO 操作的全异步实现，可以请求在 Channel 状态发生变化时得到通知

  - OP_ACCEPT:          新 Channel 被接受并就绪
  - OP_CONNECT:      Channel 连接已完成
  - OP_READ:              Channel 有已就绪的可供读取的数据
  - OP_WRITE:             Channel 可用于写数据

- Epoll: netty 特有的实现，更加适配 netty 现有的线程模型

  用于 linux 的本地非阻塞传输

- OIO: 使用阻塞流(java.net)

- Local: 可在 JVM 内部通过管道进行通信的本地传输

  用于在同一个 JVM 中运行的客户端 和 服务器程序之间的异步通信

- Embedded: 用于测试 ChannelHandler

  可以将一组 ChannelHandler 作为帮助器嵌入到其他的 ChannelHandler 内部(可以扩展其功能而不用修改内部代码)

  

#### 5. ByteBuf 数据容器

java.nio.ByteBuffer -> io.netty.buffer.ByteBuf

1. 工作原理

   维护两个索引(读取，写入)

2. 使用模式

   - 堆缓冲区

     支撑数组(backing array)

   - 直接缓冲区

   - 复合缓冲区

     `io.netty.buffer.CompositeByteBuf` 提供一个将多个缓冲区表示为单个合并缓冲区的虚拟表示



字节级操作

- 随机访问索引

  ```java
  buf.getByte(i)
  ```

- 顺序访问索引

  通过 读索引，写索引 将数据划分为三个区域

- 可丢弃字节

  discardReadBytes 方法丢弃已经读过的数据，会导致内存复制(需要将可读字节移动到缓冲区的开始位置)

- 可读字节

- 可写字节

- 索引管理

  markReaderIndex, markWriteIndex, resetWriteIndex, resetReadIndex, clear

- 查找操作

  确定索引 indexOf

  io.netty.util.ByteProcessor

- 派生缓冲区

  展示内容视图





















