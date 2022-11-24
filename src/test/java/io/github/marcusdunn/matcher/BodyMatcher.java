package io.github.marcusdunn.matcher;

import io.vertx.ext.web.client.HttpResponse;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class BodyMatcher<E, T extends HttpResponse<E>> extends FeatureMatcher<T, E> {
    public BodyMatcher(Matcher<? super E> bodyMatcher) {
        super(bodyMatcher, "a body of", "body");
    }

    public static <E> Matcher<? super HttpResponse<E>> hasBody(Matcher<? super E> bodyMatcher) {
        return new BodyMatcher<>(bodyMatcher);
    }

    @Override
    protected E featureValueOf(T actual) {
        return actual.body();
    }
}

