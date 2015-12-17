package com.github.gregwhitaker.asyncshowdown;

import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;

public class Application {

    public static void main(String... args) {
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(new PathHandler()
                        .addExactPath("/hello", new HelloHandler()))
                .build();

        server.start();
    }
}
