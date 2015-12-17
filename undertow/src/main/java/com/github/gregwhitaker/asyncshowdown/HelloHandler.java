package com.github.gregwhitaker.asyncshowdown;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

public class HelloHandler implements HttpHandler {
    private static Random RANDOM = new Random(System.currentTimeMillis());

    /**
     * Asynchronously waits a random random number of milliseconds, within the specified minimum and maximum, before
     * returning a 200 HTTP response with the body containing the string "Hello World!"
     *
     * @param exchange undertow exchange
     * @throws Exception
     */
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        // Dispatches the current request to a worker thread if it is on one of Undertow's main IO threads
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        final Map<String, Deque<String>> queryParams = exchange.getQueryParameters();
        Long minSleep = Long.parseLong(queryParams.getOrDefault("minSleepMs", new LinkedList<>(Arrays.asList("500"))).getFirst());
        Long maxSleep = Long.parseLong(queryParams.getOrDefault("maxSleepMs", new LinkedList<>(Arrays.asList("500"))).getFirst());

        HelloGenerator helloGenerator = new HelloGenerator(minSleep, maxSleep);
        String message = helloGenerator.call();

        exchange.setStatusCode(200);
        exchange.getResponseSender().send(message);
        exchange.endExchange();
    }

    /**
     * Task that sleeps for a random amount of time, within a configurable interval, and then returns the string "Hello World!".
     */
    class HelloGenerator implements Callable<String> {
        private final long duration;

        public HelloGenerator(final long minSleep, final long maxSleep) {
            this.duration = minSleep + (long)(RANDOM.nextDouble() * (maxSleep - minSleep));
        }

        @Override
        public String call() throws Exception {
            Thread.sleep(duration);
            return "Hello World!";
        }
    }
}
