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



- Eventloop: 控制流，多线程处理，并发

  定义 Netty 的核心抽象，处理连接的生命周期中发生的事件

  一个给定 Channel 的所有 IO 操作全部都由一个 Thread 执行

  - 一个 EventLoopGroup 包含一个或者多个 EventLoop；
  - 一个 EventLoop 在它的生命周期内只和一个 Thread 绑定；
  - 所有由 EventLoop 处理的 I/O 事件都将在它专有的 Thread 上被处理；
  - 一个 Channel 在它的生命周期内只注册于一个 EventLoop；
  - 一个 EventLoop 可能会被分配给一个或多个 Channel。



- ChanndlFuture: 异步通知

  对于异步操作，在特定的时间段确定其结果



##### 3.2 ChannelHandler, ChannelPipeline

- ChannelHandler

  处理出站 和 入站 数据的应用程序逻辑的容器

  

- ChannelPipeline
