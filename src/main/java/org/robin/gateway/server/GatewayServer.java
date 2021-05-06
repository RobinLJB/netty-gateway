package org.robin.gateway.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.robin.gateway.handler.ServerChannelHandler;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

public class GatewayServer implements NettyServer{

    private Integer port;

    private String path;

    //upstream server list
    private List<InetSocketAddress> subServerList = new ArrayList<InetSocketAddress>();

    private NioEventLoopGroup serverWorkerGroup ;
    private NioEventLoopGroup serverBossGroup;

    public GatewayServer() {
        this.serverBossGroup = new NioEventLoopGroup();
        this.serverWorkerGroup = new NioEventLoopGroup();
    }


    public GatewayServer(String path) {
        this();
        this.path = path;
    }

    public GatewayServer(int port) {
        this();
        this.port = port;
    }


    public void start() throws InterruptedException, IOException {
        SocketAddress localAddress;
        if(port!=null){
            localAddress = new InetSocketAddress(port);
        }else if(path!=null) {
            localAddress = new AFUNIXSocketAddress(new File(path));
        }else{
            throw new RuntimeException("could not initialize server socket");
        }
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

            ChannelFuture f = bootstrap.bind(localAddress).sync();

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<InetSocketAddress> getSubServerList() {
        return subServerList;
    }

    public void setSubServerList(List<InetSocketAddress> subServerList) {
        this.subServerList = subServerList;
    }
}
