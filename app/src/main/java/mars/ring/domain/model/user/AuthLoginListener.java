package mars.ring.domain.model.user;

import android.content.Intent;

import mars.ring.domain.model.user.AuthEvent;
import mars.ring.domain.model.user.AuthException;
import mars.ring.domain.model.user.AuthRepo;

/**
 * Created by developer on 12/12/17.
 */

public interface AuthLoginListener {
    default void onStart(AuthRepo repo, AuthEvent event) {}
    default void onEvent(AuthRepo repo, AuthEvent event) {}
    void onUserAgentRequest(AuthRepo repo, Intent intent);
    default void onSuccess(AuthRepo repo, AuthEvent event) {}
    default void onFailure(AuthRepo repo, AuthEvent event, AuthException ex) {}
}
