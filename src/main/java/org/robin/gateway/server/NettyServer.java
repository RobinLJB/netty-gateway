package org.robin.gateway.server;

import java.io.IOException;

public interface NettyServer {

    void start() throws InterruptedException, IOException;

    void stop() throws InterruptedException;
}