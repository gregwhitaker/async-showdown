package com.github.gregwhitaker.asyncshowdown;

import ratpack.server.RatpackServer;
import ratpack.server.ServerConfig;

public class Application {

    public static void main(String... args) throws Exception {
        RatpackServer.start(server -> server
                .serverConfig(ServerConfig.builder().port(8080).build())
                .handlers(chain -> chain
                        .get("hello", new HelloHandler())));
    }
}
