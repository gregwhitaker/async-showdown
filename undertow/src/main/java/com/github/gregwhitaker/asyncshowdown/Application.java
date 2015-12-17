package com.github.gregwhitaker.asyncshowdown;

import io.undertow.Undertow;

public class Application {

    public static void main(String... args) {
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(new HelloHandler())
                .build();

        server.start();
    }
}
