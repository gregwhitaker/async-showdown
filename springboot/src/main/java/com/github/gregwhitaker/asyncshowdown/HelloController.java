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

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

@RestController
public class HelloController {
    private static Random RANDOM = new Random(System.currentTimeMillis());

    /**
     * Asynchronously waits a random random number of milliseconds, within the specified minimum and maximum, before
     * returning a 200 HTTP response with the body containing the string "Hello World!"
     *
     * @param minSleep minimum sleep time in milliseconds
     * @param maxSleep maximum sleep time in milliseconds
     * @return A 200 HTTP response with the body containing the string "Hello World!"
     */
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public DeferredResult<ResponseEntity<String>> hello(@RequestParam(name = "minSleepMs", defaultValue = "500") long minSleep,
                                                        @RequestParam(name = "maxSleepMs", defaultValue = "500") long maxSleep) {
        final DeferredResult<ResponseEntity<String>> deferredResult = new DeferredResult<>();

        final FutureTask<String> helloTask = new FutureTask(new HelloGenerator(minSleep, maxSleep));
        Observable.<String>create(sub -> {
            String message = null;
            try {
                helloTask.run();
                message = helloTask.get();
            } catch (Exception e) {
                sub.onError(e);
            }
            sub.onNext(message);
            sub.onCompleted();
        })
        .last()
        .subscribeOn(Schedulers.io())
        .subscribe(message -> deferredResult.setResult(ResponseEntity.ok(message)),
                   error -> deferredResult.setResult(ResponseEntity.status(500).body(error.getMessage())));

        return deferredResult;
    }

    /**
     * Blocks for a random random number of milliseconds, within the specified minimum and maximum, before returning a 200 HTTP
     * response with the body containing the string "Hello World!"
     *
     * @param minSleep minimum sleep time in milliseconds
     * @param maxSleep maximum sleep time in milliseconds
     * @return A 200 HTTP response with the body containing the string "Hello World!"
     * @throws Exception
     */
    @RequestMapping(value = "/helloblocking", method = RequestMethod.GET)
    public ResponseEntity<String> helloBlocking(@RequestParam(name = "minSleepMs", defaultValue = "500") long minSleep,
                                                @RequestParam(name = "maxSleepMs", defaultValue = "500") long maxSleep) throws Exception {
        return ResponseEntity.ok(new HelloGenerator(minSleep, maxSleep).call());
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
