package reactor.com;

import com.sun.jdi.request.DuplicateRequestException;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Array;
import java.time.Duration;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Mono.empty();
        Flux.empty();

        Mono<Integer> mono = Mono.just(1);
        Flux<Integer> flux = Flux.just(1, 2, 3);

        Flux<Integer> fluxToMono = mono.flux();
        Mono<Boolean> monoFromFlux = flux.any(s -> s.equals(1));
        Mono<Integer> integerMono = flux.elementAt(1);


        Flux.range(1, 5).blockFirst();
        Flux.range(1, 5).subscribe(System.out::println);
        System.out.println(" range flux ====================");

        Flux.fromIterable(Arrays.asList(1, 2, 3)).subscribe(System.out::println);
        System.out.println("to create flux fromIterable ====================");
        // Unlimited stream "Hello" in to console
//        Flux.<String>generate(sink -> {
//                    sink.next("Hello");
//                }).subscribe(System.out::println);


        // Stream write 10 time "Hello" with time period 5 msec in to console
        // and sleep tread 4 sec after for close
        Flux.<String>generate(sink -> {
                    sink.next("Hello ++ ");
                })
                .delayElements(Duration.ofMillis(500))
                .take(10)
                .subscribe(System.out::println);
        System.out.println(" generate flux ====================");



        // Stream write "Step: state + 3" start from 2354 until sink != 2366
        // when sink is = 2366 the stream stop run
        Flux<Object> tekegrammProducer = Flux.generate(
                () -> 2354,
                (state, sink) -> {
                    if (state > 2366) {
                        sink.complete(); // to stop run method
                    } else {
                        sink.next("Step: " + state);
                    }
                    return state + 3;
                }

        );
            //    .subscribe(System.out::println);


        // For creating Flux we can to use push or create methods
        // different between push and create is push - monoTread and creat - multiTread
        // we can to use this for telegramm service
        Flux
                .create(sink -> {
                        tekegrammProducer
                                .subscribe(new BaseSubscriber<Object>() {
                            @Override
                            protected void hookOnNext(Object value) {
                                sink.next(value);
                            }

                            @Override
                            protected void hookOnComplete() {
                                sink.complete();
                            }
                        });
                        }

                )
                .subscribe(System.out::println);
        System.out.println("sreate push flux ====================");


        // Example to use flux for produce request in to database
        Flux
                .create(sink ->
                        sink.onRequest(r -> {
                            sink.next("DB returns : " + tekegrammProducer.blockFirst());
                        })
                        )
                .subscribe(System.out::println);
        System.out.println(" create or push flux to make request ====================");

        Flux<String> first = Flux
                .just("World", "coder")
                .repeat();

        // to make from two flux object one unite this two flux objects
        Flux<String> sumFlux = Flux
                .just("hello", "dru", "java", "Asia", "java")
                .zipWith(first, (firstThread, secondThread) -> String.format("%s, %s ", firstThread,secondThread));

        sumFlux.subscribe(System.out::println);
        System.out.println("sumFlux ====================");

        Flux<String> stringFlux = sumFlux
                .delayElements(Duration.ofMillis(1300))
                .timeout(Duration.ofMillis(1))
                .retry(3)
//                .onErrorReturn("To slow")
                .onErrorResume((throwable ->
                        Flux
                                .interval((Duration.ofMillis(300)))
                                .map(String::valueOf)


//                {
//                        return Flux.just("one" , "two");
//                    }
                ))
                .skip(2)
                .take(3);



                stringFlux.subscribe(
                        v -> System.out.printf(v),
                        er -> System.out.println(er),
                        () -> System.out.println("finished")
                );
        System.out.println("stringFlux ====================");


         stringFlux.toIterable().forEach(e -> System.out.println(e));
        System.out.println("stringFlux.toIterable ====================");

        Thread.sleep(4000L);
    }
}
