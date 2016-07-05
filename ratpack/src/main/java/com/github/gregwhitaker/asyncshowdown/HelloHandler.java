/*
 * Copyright 2016 Greg Whitaker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.gregwhitaker.asyncshowdown;

import ratpack.exec.Blocking;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.util.MultiValueMap;

import java.util.Random;
import java.util.concurrent.Callable;

public class HelloHandler implements Handler {
    private static Random RANDOM = new Random(System.currentTimeMillis());

    /**
     * Asynchronously waits a random random number of milliseconds, within the specified minimum and maximum, before
     * returning a 200 HTTP response with the body containing the string "Hello World!"
     *
     * @param ctx ratpack context
     * @throws Exception
     */
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
