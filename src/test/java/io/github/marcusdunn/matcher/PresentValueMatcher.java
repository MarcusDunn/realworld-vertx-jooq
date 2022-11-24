package io.github.marcusdunn.matcher;

import org.hamcrest.*;

import java.util.Optional;

public class PresentValueMatcher<U, T extends Optional<? extends U>> extends FeatureMatcher<T, U> {
    private PresentValueMatcher(Matcher<? super U> subMatcher) {
        super(subMatcher, "has present value of", "present value");
    }

    public static Matcher<? super Optional<?>> hasPresentValue() {
        return new PresentValueMatcher<>(Matchers.anything());
    }

    public static <U> Matcher<? super Optional<U>> hasPresentValue(Matcher<? super U> subMatcher) {
        return new PresentValueMatcher<>(subMatcher);
    }

    @Override
    protected U featureValueOf(T t) {
        return t.orElseThrow();
    }
}

