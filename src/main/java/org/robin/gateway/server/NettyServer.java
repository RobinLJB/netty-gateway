package org.robin.gateway.server;

public interface NettyServer {

    void start() throws InterruptedException;

    void stop() throws InterruptedException;
}