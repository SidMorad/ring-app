package mars.ring.domain.model.beacontag;

import java.util.List;

/**
 * Created by developer on 05/02/18.
 */

public interface FoundNotificationsCallback {
    void call(List<FoundNotificationDTO> notes, HttpException e);
}
