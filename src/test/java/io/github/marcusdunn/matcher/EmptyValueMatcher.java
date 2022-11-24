package io.github.marcusdunn.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Optional;

public class EmptyValueMatcher extends TypeSafeMatcher<Optional<?>> {
    public static Matcher<? super Optional<?>> isEmpty() {
        return new EmptyValueMatcher();
    }

    @Override
    protected boolean matchesSafely(Optional<?> item) {
        return item.isEmpty();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("is empty");
    }
}
