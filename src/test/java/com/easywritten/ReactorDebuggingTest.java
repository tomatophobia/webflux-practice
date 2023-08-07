// https://spring.io/blog/2019/04/16/flight-of-the-flux-2-debugging-caveats
package com.easywritten;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class ReactorDebuggingTest {
    @Test
    public void imperative() throws ExecutionException, InterruptedException {
        final ScheduledExecutorService executor =
                Executors.newSingleThreadScheduledExecutor();

        final int seconds = LocalTime.now().getSecond();
        final List<Integer> source;
        if (seconds % 2 == 0) {
            source = IntStream.range(1, 11).boxed().collect(Collectors.toList());
        } else if (seconds % 3 == 0) {
            source = IntStream.range(0, 4).boxed().collect(Collectors.toList());
        } else {
            source = Arrays.asList(1, 2, 3, 4);
        }

        executor.submit(() -> source.get(5))
                .get();
    }

    @Test
    public void reactive() {
        int seconds = LocalTime.now().getSecond();
        Mono<Integer> source;
        if (seconds % 2 == 0) {
            source = Flux.range(1, 10)
                         .elementAt(5);
        } else if (seconds % 3 == 0) {
            source = Flux.range(0, 4)
                         .elementAt(5);
        } else {
            source = Flux.just(1, 2, 3, 4)
                         .elementAt(5);
        }

        source.subscribeOn(Schedulers.parallel())  // 병렬 실행하지 않아도 여전히 스택 트레이스는 알아보기 어렵다.
              .block();
    }

    @Test
    public void log() {
        int seconds = LocalTime.now().getSecond();
        Mono<Integer> source;
        if (seconds % 2 == 0) {
            source = Flux.range(1, 10)
                         .elementAt(5)
                         .log("source A");
        }
        else if (seconds % 3 == 0) {
            source = Flux.range(0, 4)
                         .elementAt(5)
                         .log("source B");
        }
        else {
            source = Flux.just(1, 2, 3, 4)
                         .elementAt(5)
                         .log("source C");
        }

        source.block(); //line 138
    }

    @Test
    public void hook() {
        Hooks.onOperatorDebug();
        try {
            int seconds = LocalTime.now().getSecond();
            Mono<Integer> source;
            if (seconds % 2 == 0) {
                source = Flux.range(1, 10)
                             .elementAt(5); //line 149
            }
            else if (seconds % 3 == 0) {
                source = Flux.range(0, 4)
                             .elementAt(5); //line 153
            }
            else {
                source = Flux.just(1, 2, 3, 4)
                             .elementAt(5); //line 157
            }

            source.block(); //line 160
        }
        finally {
            Hooks.resetOnOperatorDebug();
        }
    }

    @Test
    public void checkpoint() {
        int seconds = LocalTime.now().getSecond();
        Mono<Integer> source;
        if (seconds % 2 == 0) {
            source = Flux.range(1, 10)
                         .elementAt(5)
                         .checkpoint("source range(1,10)");
        }
        else if (seconds % 3 == 0) {
            source = Flux.range(0, 4)
                         .elementAt(5)
                         .checkpoint("source range(0,4)");
        }
        else {
            source = Flux.just(1, 2, 3, 4)
                         .elementAt(5)
                         .checkpoint("source just(1,2,3,4)");
        }

        source.block(); //line 186
    }
}
