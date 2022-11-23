package io.github.marcusdunn;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import java.util.Optional;
import java.util.concurrent.Flow;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class ReactiveFutureBridge {
    private ReactiveFutureBridge() {
    }

    public static <T> Future<Optional<T>> fetchOne(Publisher<T> publisher) {
        FutureSubscriber<T> s = new FutureSubscriber<>();
        publisher.subscribe(s);
        return s.getFuture();
    }

    private static class FutureSubscriber<T> implements Flow.Subscriber<T>, Subscriber<T> {
        private final Promise<Optional<T>> promise;

        public FutureSubscriber() {
            this.promise = Promise.promise();
        }

        public Future<Optional<T>> getFuture() {
            return promise.future();
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(1);
        }

        @Override
        public void onSubscribe(Subscription s) {
            s.request(1);
        }

        @Override
        public void onNext(T t) {
            promise.complete(Optional.of(t));
        }

        @Override
        public void onError(Throwable t) {
            promise.fail(t);
        }

        @Override
        public void onComplete() {
            promise.tryComplete(Optional.empty());
        }
    }
}
