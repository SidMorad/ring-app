package mars.ring.domain.model.beacontag;

import retrofit2.Callback;

/**
 * Created by developer on 15/01/18.
 */

public interface SendBeaconLTCallback {
    void call(HttpException e);
}
