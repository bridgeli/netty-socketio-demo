package cn.bridgeli.msg.push.impl.server.check;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpCheckServer {

    private Logger logger = LoggerFactory.getLogger(HttpCheckServer.class);

    private int port;
    private ChannelFuture future = null;
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    public HttpCheckServer(int port) {
        this.port = port;
    }

    public void stop() {
        try {
            if (future != null) {
                future.channel().closeFuture().sync();
            }
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Check server stop exception", e);
        }
    }

    public void start() {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpRequestDecoder());
                            pipeline.addLast(new HttpResponseEncoder());
                            pipeline.addLast(new SimpleServerHandler());
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            logger.info("Check server started at port: {}", this.port);
        } catch (Exception e) {
            e.printStackTrace();
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            logger.error("Check server start exception", e);
        }
    }

    class SimpleServerHandler extends ChannelInboundHandlerAdapter {

        private Logger logger = LoggerFactory.getLogger(SimpleServerHandler.class);

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

            if (msg instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) msg;
                logger.info("check");
            }

            if (msg instanceof HttpContent) {
                HttpContent httpContent = (HttpContent) msg;
                ByteBuf content = httpContent.content();
                content.release();

                ByteBuf byteBuf = Unpooled.wrappedBuffer("ok".getBytes("UTF-8"));
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);
                response.headers().set("Content-Type", "text/html; charset=UTF-8");
                response.headers().set("Content-Length", byteBuf.readableBytes());
                ctx.channel().write(response);
                ctx.channel().flush();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            logger.error("check server catch exception", cause);
        }
    }

}