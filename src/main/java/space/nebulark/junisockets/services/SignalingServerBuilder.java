package space.nebulark.junisockets.services;

import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

public class SignalingServerBuilder {
   
    private Logger logger;
    private int port = -1;
    private String host;

    public SignalingServerBuilder setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public SignalingServerBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public SignalingServerBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public SignalingServer build() {

        if (logger == null || port == -1) {
            throw new IllegalArgumentException();
        } else if (logger != null && port != -1 && host == null) { 
            return new SignalingServer(logger, new InetSocketAddress(port));
        } else {
            return new SignalingServer(logger, new InetSocketAddress(host, port));
        }

    }

}
