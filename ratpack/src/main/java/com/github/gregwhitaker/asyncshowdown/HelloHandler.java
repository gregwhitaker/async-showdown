package com.github.gregwhitaker.asyncshowdown;

import ratpack.exec.Blocking;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.util.MultiValueMap;

import java.util.Random;
import java.util.concurrent.Callable;

public class HelloHandler implements Handler {
    private static Random RANDOM = new Random(System.currentTimeMillis());

    @Override
    public void handle(Context ctx) throws Exception {
        MultiValueMap<String, String> queryParams = ctx.getRequest().getQueryParams();
        Long minSleep = Long.parseLong(queryParams.getOrDefault("minSleepMs", "500"));
        Long maxSleep = Long.parseLong(queryParams.getOrDefault("maxSleepMs", "500"));

        Blocking.get(() -> new HelloGenerator(minSleep, maxSleep).call())
                .then(message -> ctx.getResponse().status(200).send(message));
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
