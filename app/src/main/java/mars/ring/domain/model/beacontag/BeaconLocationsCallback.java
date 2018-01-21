package mars.ring.domain.model.beacontag;

import java.util.List;

/**
 * Created by developer on 18/01/18.
 */

public interface BeaconLocationsCallback {
    void call(List<BeaconLTDTO> locations, HttpException e);
}
