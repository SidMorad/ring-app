package mars.ring.domain.model.beacontag;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by developer on 12/12/17.
 */

public interface BeaconsAPI {

    @GET("r-beacons/")
    Call<List<BeaconDTO>> getBeacons();

    @POST("r-beacons/")
    Call<Void> createBeacon(@Body BeaconDTO beaconDTO);
}
