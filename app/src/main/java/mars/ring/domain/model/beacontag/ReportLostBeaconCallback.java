package mars.ring.domain.model.beacontag;

/**
 * Created by developer on 31/01/18.
 */

public interface ReportLostBeaconCallback {
    void call(HttpException httpException);
}
