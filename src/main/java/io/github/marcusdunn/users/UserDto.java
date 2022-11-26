package io.github.marcusdunn.users;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.jooq.generated.tables.records.JUserRecord;

/**
 * See <a href="https://realworld-docs.netlify.app/docs/specs/backend-specs/api-response-format/#users-for-authentication">User</a>
 */
public record UserDto(String email, String token, String username, String bio, String image) {

    public UserDto(JUserRecord user, String jwt) {
        this(user.getEmail(), jwt, user.getUsername(), null, null);
    }

    public Buffer toJsonBuffer() {
        return new JsonObject()
                .put("email", email)
                .put("token", token)
                .put("username", username)
                .put("bio", bio)
                .put("image", image)
                .toBuffer();
    }
}
