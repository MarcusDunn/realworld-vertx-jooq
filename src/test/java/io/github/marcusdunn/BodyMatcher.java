package io.github.marcusdunn;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class BodyMatcher<T extends HttpResponse<Buffer>> extends FeatureMatcher<T, Buffer> {
    public BodyMatcher(Matcher<? super Buffer> bodyMatcher) {
        super(bodyMatcher, "a body of", "body");
    }

    public static Matcher<? super HttpResponse<Buffer>> hasBody(Matcher<? super Buffer> bodyMatcher) {
        return new BodyMatcher<>(bodyMatcher);
    }

    @Override
    protected Buffer featureValueOf(T actual) {
        return actual.body();
    }
}

