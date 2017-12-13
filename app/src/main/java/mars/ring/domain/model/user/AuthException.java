package mars.ring.domain.model.user;

/**
 * Created by developer on 12/12/17.
 */

public class AuthException extends Exception {
    AuthException(String message) {
        super(message);
    }
}
