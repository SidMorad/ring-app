package mars.ring.domain.model.beacontag;

import java.util.List;

/**
 * Created by developer on 12/12/17.
 */

public interface BeaconListCallback {
    void call(List<BeaconDTO> beacons, Exception e);
}
