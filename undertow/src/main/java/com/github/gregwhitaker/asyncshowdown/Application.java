package com.github.gregwhitaker.asyncshowdown;

import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.handlers.PathHandler;
import org.xnio.Options;

public class Application {

    public static void main(String... args) {
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setBufferSize(1024 * 16)
                .setIoThreads(Runtime.getRuntime().availableProcessors() * 2)
                .setSocketOption(Options.BACKLOG, 10000)
                .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false)
                .setHandler(new PathHandler()
                        .addExactPath("/hello", new HelloHandler()))
                .setWorkerThreads(200)
                .build();

        server.start();
    }
}
