package io.github.marcusdunn;

import io.vertx.ext.web.client.HttpResponse;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;


public class StatusCodeMatcher<T extends HttpResponse<?>> extends FeatureMatcher<T, Integer> {
    public StatusCodeMatcher(Matcher<? super Integer> statusCodeMatcher) {
        super(statusCodeMatcher, "a status code of", "status code");
    }

    public static Matcher<? super HttpResponse<?>> hasStatusCode(Matcher<? super Integer> statusCodeMatcher) {
        return new StatusCodeMatcher<>(statusCodeMatcher);
    }

    @Override
    protected Integer featureValueOf(T actual) {
        return actual.statusCode();
    }
}

