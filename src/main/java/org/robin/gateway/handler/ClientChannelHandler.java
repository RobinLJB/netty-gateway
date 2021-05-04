package org.robin.gateway.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;

class ClientChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {

    Channel previousChannel;


    ClientChannelHandler(Channel previousChannel){
        this.previousChannel = previousChannel;
    }

    /**
     * The {@link io.netty.channel.Channel} of the {@link io.netty.channel.ChannelHandlerContext} is now active
     * channel 活动时
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        previousChannel.config().setOption(io.netty.channel.ChannelOption.AUTO_READ, true);
        super.channelActive(ctx);
    }

    /**
     *  Invoked when the current {@link io.netty.channel.Channel} has read a message from the peer.
     *  channel 读取到信息时
     */

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        //服务端写
        previousChannel.writeAndFlush(msg.copy());
    }
/**
     * The {@link io.netty.channel.Channel} of the {@link ChannelHandlerContext} was registered is now inactive and reached its
     * end of lifetime.
     * channel 不活动时
     */
    public void channelInactive(ChannelHandlerContext context) throws Exception{
        final ChannelHandlerContext ctx = context;
        previousChannel.close().addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                ctx.close();
            }
        });
        super.channelInactive(ctx);
    }



    /**
     * Gets called if a {@link Throwable} was thrown.
     * 抛出异常时
     */
    @Override
    @SuppressWarnings("deprecation")
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception{
        cause.printStackTrace();
        final ChannelHandlerContext ctx = context;
        previousChannel.close().addListener(new ChannelFutureListener() {
           public  void operationComplete(ChannelFuture future) throws Exception {
                ctx.close();
            }
        });
    }

}
