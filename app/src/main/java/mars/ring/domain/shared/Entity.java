package mars.ring.domain.shared;

/**
 * An entity, as explained in the DDD book.
 *
 * Created by developer on 15/01/18.
 */
public interface Entity<T> {

    /**
     * Entities compare by identity, not by attributes.
     *
     * @param other The other entity.
     * @return true if the identities are the same, regardless of other attributes.
     */
    boolean sameIdentityAs(T other);

}
