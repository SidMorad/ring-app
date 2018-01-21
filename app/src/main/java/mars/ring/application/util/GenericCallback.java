package mars.ring.application.util;

/**
 * A generic callback, which is useful for simple cases that a callback is needed.
 *
 * Created by developer on 20/01/18.
 */

public interface GenericCallback {
    void onSuccess();
    void onFailure(Exception e);
}
