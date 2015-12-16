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
public class HelloWorldController {
    private static Random RANDOM = new Random(System.currentTimeMillis());

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public DeferredResult<ResponseEntity<String>> hello(@RequestParam(name = "minSleepMs", defaultValue = "500") long minSleep,
                                                        @RequestParam(name = "maxSleepMs", defaultValue = "500") long maxSleep) {
        final DeferredResult<ResponseEntity<String>> deferredResult = new DeferredResult<>();

        final FutureTask<String> helloTask = new FutureTask(new HelloTask(minSleep, maxSleep));
        Observable.from(helloTask)
                .last()
                .subscribeOn(Schedulers.io())
                .subscribe(message -> deferredResult.setResult(ResponseEntity.ok(message)),
                           error -> deferredResult.setResult(ResponseEntity.status(500).body(error.getMessage())));

        return deferredResult;
    }

    /**
     * Task that sleeps for a random amount of time, within a configurable interval, and then returns the string "Hello World!".
     */
    class HelloTask implements Callable<String> {
        private final long duration;

        public HelloTask(final long minSleep, final long maxSleep) {
            this.duration = minSleep + (long)(RANDOM.nextDouble() * (maxSleep - minSleep));
        }

        @Override
        public String call() throws Exception {
            Thread.sleep(duration);
            return "Hello World!";
        }
    }
}
