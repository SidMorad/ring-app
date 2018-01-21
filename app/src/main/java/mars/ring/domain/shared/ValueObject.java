package mars.ring.domain.shared;

import java.io.Serializable;

/**
 * A value objects, as described in the DDD book.
 *
 * Created by developer on 15/01/18.
 */
public interface ValueObject<T> extends Serializable {

    /**
     * Values objects compare by the values of their attributies, they don't have an identity.
     *
     * @param other The other value object.
     * @return <code>true</code> if the given value object's and this value object's attributes are the same.
     */
    boolean sameValueAs(T other);
}
