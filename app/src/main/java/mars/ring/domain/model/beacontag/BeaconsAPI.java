package mars.ring.domain.model.beacontag;

import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by developer on 12/12/17.
 */
public interface BeaconsAPI {

    @GET("r-beacons/?sort=created,desc")
    Call<List<BeaconDTO>> getBeacons();

    @POST("r-beacons/")
    Call<Void> createBeacon(@Body BeaconDTO beaconDTO);

    @PUT("r-beacons/")
    Call<Void> updateBeacon(@Body BeaconDTO beaconDTO);

    @DELETE("r-beacons/{id}")
    Call<Void> deleteBeacon(@Path("id") Long id);

    @PUT("r-beacons/toggleIsMissing/{lost}")
    Call<Void> toggleIsMissing(@Body BeaconDTO beaconDTO, @Path("lost") Boolean lost);

    @POST("ble/trace")
    Call<Void> sendBeaconLT(@Body Set<BeaconLTCommand> beaconLtSet);

    @GET("ble/locations")
    Call<List<BeaconLTDTO>> lastLocations();

}
