package org.robin.gateway.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.robin.gateway.handler.ServerChannelHandler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class GatewayServer implements NettyServer{

    private int port;

    //upstream server list
    private List<InetSocketAddress> subServerList = new ArrayList<InetSocketAddress>();

    private NioEventLoopGroup serverWorkerGroup ;
    private NioEventLoopGroup serverBossGroup;

    public GatewayServer(Integer port) {
        this.port = port;
        this.serverBossGroup = new NioEventLoopGroup();
        this.serverWorkerGroup = new NioEventLoopGroup();
    }


    public void start() throws InterruptedException {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(serverBossGroup, serverWorkerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //ch.pipeline().addLast(new LineBasedFrameDecoder(1024))
                            ch.pipeline().addLast(new ServerChannelHandler(GatewayServer.this));
                        }
                    });
            ChannelFuture f = bootstrap.bind(port).sync();
            if (f.isSuccess()) {
                System.out.println("proxy starts successfully at "+port);
            }
            ChannelFuture closeFuture = f.channel().closeFuture();
            closeFuture.sync();
            if (closeFuture.isSuccess()) {
                System.out.println("gateway closed");
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }finally{
            serverBossGroup.shutdownGracefully().sync();
            serverWorkerGroup.shutdownGracefully().sync();
        }
    }

    public void stop() throws InterruptedException {
        serverBossGroup.shutdownGracefully().sync();
        serverWorkerGroup.shutdownGracefully().sync();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<InetSocketAddress> getSubServerList() {
        return subServerList;
    }

    public void setSubServerList(List<InetSocketAddress> subServerList) {
        this.subServerList = subServerList;
    }
}
