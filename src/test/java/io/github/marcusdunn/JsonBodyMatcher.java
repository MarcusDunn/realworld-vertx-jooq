package io.github.marcusdunn;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class JsonBodyMatcher<T extends Buffer> extends FeatureMatcher<T, JsonObject> {
    public JsonBodyMatcher(Matcher<? super JsonObject> jsonObjectMatcher) {
        super(jsonObjectMatcher,   "a json of", "json");
    }

    public static Matcher<? super Buffer> withJsonObject(Matcher<? super JsonObject> jsonObjectMatcher) {
        return new JsonBodyMatcher<>(jsonObjectMatcher);
    }

    @Override
    protected JsonObject featureValueOf(T actual) {
        return actual.toJsonObject();
    }
}
