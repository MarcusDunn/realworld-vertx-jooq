package io.github.marcusdunn.matcher;

import io.vertx.core.json.JsonObject;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class JsonStringFieldMatcher<T extends JsonObject> extends FeatureMatcher<T, String> {
    private final String fieldName;

    public JsonStringFieldMatcher(String fieldName, Matcher<? super String> stringFieldMatcher) {
        super(stringFieldMatcher, fieldName + " of", fieldName);
        this.fieldName = fieldName;
    }

    public static Matcher<? super JsonObject> hasStringField(String fieldName, Matcher<? super String> stringFieldMatcher) {
        return new JsonStringFieldMatcher<>(fieldName, stringFieldMatcher);
    }

    @Override
    protected String featureValueOf(T actual) {
        return actual.getString(fieldName);
    }
}
