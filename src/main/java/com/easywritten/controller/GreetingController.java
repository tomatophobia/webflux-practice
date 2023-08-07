// https://hackernoon.com/how-to-debug-a-spring-webflux-application
package com.easywritten.controller;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/greeting")
public class GreetingController {
    private static final Logger logger = LoggerFactory.getLogger(GreetingController.class);

    @GetMapping("/{firstName}/{lastName}")
    public Mono<String> greeting(@PathVariable String firstName, @PathVariable String lastName) {
//        Hooks.onOperatorDebug();

        return Flux.fromIterable(Arrays.asList(firstName, lastName))
                   .filter(GreetingController::wasWorkingNiceBeforeRefactoring)
                   .checkpoint("After filtering")
                   .transform(GreetingController::senselessTransformation)
                   .checkpoint("After transformation")
                   .collect(Collectors.joining())
                   .checkpoint("After joining")
                   .map(names -> "Hello, " + names);
    }

    private static boolean wasWorkingNiceBeforeRefactoring(String aName) {
        // We don't want to greet with John, sorry
        return !aName.equals("John");
    }

    private static Flux<String> senselessTransformation(Flux<String> flux) {
        return flux
                .single()
                .flux()
                .subscribeOn(Schedulers.parallel());
    }
}
