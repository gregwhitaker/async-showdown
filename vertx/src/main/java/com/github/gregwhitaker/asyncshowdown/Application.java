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

import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServerRequest;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.Random;
import java.util.concurrent.Callable;

public class Application extends AbstractVerticle {
    private static Random RANDOM = new Random(System.currentTimeMillis());

    public static void main(String... args) {

    }

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        router.get("/hello").handler(routingContext -> {
            HttpServerRequest request = routingContext.request();
            HttpServerResponse response = routingContext.response();

            Long minSleep = (request.getParam("minSleepMs") != null) ? Long.parseLong(request.getParam("minSleepMs")) : 500L;
            Long maxSleep = (request.getParam("maxSleepMs") != null) ? Long.parseLong(request.getParam("maxSleepMs")) : 500L;

            HelloGenerator helloTask = new HelloGenerator(minSleep, maxSleep);

            Observable.<String>create(sub -> {
                String message = null;
                try {
                    message = helloTask.call();
                } catch (Exception e) {
                    sub.onError(e);
                }
                sub.onNext(message);
                sub.onCompleted();
            })
            .last()
            .subscribeOn(Schedulers.io())
            .subscribe(message -> routingContext.response().setStatusCode(200).end(message),
                       error -> routingContext.response().setStatusCode(500).end());
        });

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
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
