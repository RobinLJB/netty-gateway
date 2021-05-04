package org.robin.gateway.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.robin.gateway.server.GatewayServer;

import java.net.InetSocketAddress;

public class ServerChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {

    static final AttributeKey<Channel> NEXT_CHANNEL = AttributeKey.newInstance("next_channel");

    GatewayServer gatewayServer;

    Bootstrap bootstrap;

    NioEventLoopGroup clientGroup;

    public ServerChannelHandler(GatewayServer gatewayServer){
        this.gatewayServer = gatewayServer;
        clientGroup = new NioEventLoopGroup();
    }

    /**
     * The {@link io.netty.channel.Channel} of the {@link io.netty.channel.ChannelHandlerContext} is now active
     * channel 活动时
     */
    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
        //向上游服务器构建socket客户端并且 存储在ChannelHandlerContext中
        final ChannelHandlerContext ctx = context;
        int index=(int)(Math.random()*gatewayServer.getSubServerList().size());
        InetSocketAddress address = gatewayServer.getSubServerList().get(index);
        //System.out.println("host : "+address.getHostName()+" , port : "+address.getPort());
        Bootstrap bootstrap = new Bootstrap();
        //用回本身的事件循环线程
        bootstrap.group(ctx.channel().eventLoop()).channel(NioSocketChannel.class).handler(new ChannelInitializer<NioSocketChannel>() {
            public void initChannel(NioSocketChannel ch) throws Exception {
                //ch.pipeline().addLast()
                ch.pipeline().addLast(new ClientChannelHandler(ctx.channel()));
            }
        }).remoteAddress(address);

        bootstrap.connect().addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    ctx.channel().attr(NEXT_CHANNEL).set(channelFuture.channel());
                } else {
                    ctx.close();
                }
            }
        });

        ctx.channel().config().setOption(ChannelOption.AUTO_READ, false);

        super.channelActive(ctx);
    }
    /**
     *  Invoked when the current {@link io.netty.channel.Channel} has read a message from the peer.
     *  channel 读取到信息时
     */

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        Channel channel = ctx.channel().attr(NEXT_CHANNEL).get();
        if(channel!=null){
            channel.writeAndFlush(msg.copy());
        }
    }

    /**
     * The {@link io.netty.channel.Channel} of the {@link ChannelHandlerContext} was registered is now inactive and reached its
     * end of lifetime.
     * channel 不活动时
     */
    public void channelInactive(ChannelHandlerContext ctx) throws Exception{
        Channel channel = ctx.channel().attr(NEXT_CHANNEL).get();
        channel.config().setOption(ChannelOption.AUTO_READ, true);
        if(channel!=null && channel.isOpen()){
            //阻塞关
            channel.close().sync();
        }
        ctx.close();
        super.channelInactive(ctx);
    }


    /**
     * Gets called if a {@link Throwable} was thrown.
     * 抛出异常时
     */
    @Override
    @SuppressWarnings("deprecation")
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception{
        final ChannelHandlerContext ctx = context;
        cause.printStackTrace();
        Channel channel = ctx.channel().attr(NEXT_CHANNEL).get();
        channel.config().setOption(ChannelOption.AUTO_READ, true);
        channel.close().addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                ctx.close();
            }
        });
    }


}
