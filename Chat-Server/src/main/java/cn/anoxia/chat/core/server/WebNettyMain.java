package cn.anoxia.chat.core.server;

import cn.anoxia.chat.core.handler.WebSocketFrameHandler;
import cn.anoxia.chat.core.codec.JsonMessageDecoder;
import cn.anoxia.chat.core.codec.JsonMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;


@Component
public class WebNettyMain {
    private static final Logger log = LoggerFactory.getLogger(WebNettyMain.class);

    public EventLoopGroup BossGroup;
    public EventLoopGroup WorkerGroup;

    public WebNettyMain(ConfigurableApplicationContext applicationContext) {
        BossGroup = new NioEventLoopGroup(1);
        WorkerGroup = new NioEventLoopGroup(); //8个NioEventLoop

        /**
         *
         * WebSocket
         * */
        ServerBootstrap serverBootstrapWeb = new ServerBootstrap();
        serverBootstrapWeb.group(BossGroup, WorkerGroup);
        serverBootstrapWeb.channel(NioServerSocketChannel.class);
        //设置服务端套接字（ServerSocketChannel）所能接收的连接请求队列的最大长度为 1024。
        serverBootstrapWeb.option(ChannelOption.SO_BACKLOG,1024);
        //开启TCP KeepAlive 机制，表示服务端和客户端长时间无数据通信时，仍会保持连接活性检测
        serverBootstrapWeb.childOption(ChannelOption.SO_KEEPALIVE, true);

        serverBootstrapWeb.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                //因为基于http协议，使用http的编码和解码器
                pipeline.addLast(new HttpServerCodec());
                //http是以块方式写，添加ChunkedWriteHandler处理器 分块向客户端写数据，防止发送大文件时导致内存溢出
                pipeline.addLast(new ChunkedWriteHandler());
                //http数据在传输过程中是分段, HttpObjectAggregator ，就是可以将多个段聚合 2. 这就就是为什么，当浏览器发送大量数据时，就会发出多次http请求
                pipeline.addLast(new HttpObjectAggregator(10240));
                // webSocket 数据压缩扩展，当添加这个的时候WebSocketServerProtocolHandler的第三个参数需要设置成true
                pipeline.addLast(new WebSocketServerCompressionHandler());
                // 聚合 websocket 的数据帧，因为客户端可能分段向服务器端发送数据
                pipeline.addLast(new WebSocketFrameAggregator(100 * 1024 * 1024));

                // 服务器端向外暴露的 web socket 端点，当客户端传递比较大的对象时，maxFrameSize参数的值需要调大
                pipeline.addLast(new WebSocketServerProtocolHandler("/chat", null, true, 100 * 1024 * 1024));
                //自定义的handler ，处理业务逻辑
                pipeline.addLast(new JsonMessageDecoder(), new JsonMessageEncoder(), new WebSocketFrameHandler(applicationContext));
                // 自定义处理器 - 处理 web socket 二进制消息
                //pipeline.addLast(new BinaryWebSocketFrameHandler());
            }
        });

        //启动服务器
        ChannelFuture channelFutureWeb = null;
        try {
            channelFutureWeb = serverBootstrapWeb.bind(8081).sync();
            if (channelFutureWeb != null && channelFutureWeb.isSuccess()) {
                log.info("NettyWebSocket(WebPort：{})->启动成功 时间：{}", 8081, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            }
        } catch (InterruptedException e) {
            log.error("Netty->启动失败", e);
        }
    }
}
