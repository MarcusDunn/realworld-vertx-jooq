package io.github.marcusdunn.matcher;

import org.approvaltests.Approvals;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.opentest4j.AssertionFailedError;

public class ApprovalMatcher<T> extends TypeSafeMatcher<T> {
    public static Matcher<? super Object> isApproved() {
        return new ApprovalMatcher<>();
    }

    @Override
    protected boolean matchesSafely(T s) {
        try {
            Approvals.verify(s);
            return true;
        } catch (AssertionFailedError e) {
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("approved");
    }
}
