package mars.ring.domain.model.beacontag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import mars.ring.application.RingApp;
import mars.ring.domain.model.user.AuthRepo;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by developer on 12/12/17.
 */

public class BeaconsRepo {
    private final String TAG = this.getClass().getSimpleName();
    private final String BEACONS_URL_BASE = "https://ring.webebook.org/api/";

    private RingApp app;
    private AuthRepo authRepo;
    private BeaconsAPI beaconsAPI;

    public BeaconsRepo(RingApp app, AuthRepo authRepo) {
        this.app = app;
        this.authRepo = authRepo;
        this.beaconsAPI = createBeaconsAPI();
    }

    private BeaconsAPI createBeaconsAPI() {
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        logger.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder clientBuilder = new OkHttpClient().newBuilder();
        clientBuilder.addInterceptor(authRepo.getAccessTokenInterceptor());
        clientBuilder.addInterceptor(logger);
        clientBuilder.connectTimeout(30, TimeUnit.SECONDS);
        clientBuilder.readTimeout(30, TimeUnit.SECONDS);

        OkHttpClient client = clientBuilder.build();

        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BEACONS_URL_BASE)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit.create(BeaconsAPI.class);
    }

    public void getBeacons(BeaconListCallback callback) {
        Call<List<BeaconDTO>> request = beaconsAPI.getBeacons();
        request.enqueue(new BeaconListCallbackImpl(callback));
    }

    public void createBeacon(BeaconDTO model, CreateBeaconCallback callback) {
        Call<Void> request = beaconsAPI.createBeacon(model);
        request.enqueue(new CreateBeaconCallbackImpl(callback));
    }

    private static class CreateBeaconCallbackImpl implements Callback<Void> {
        private CreateBeaconCallback callback;
        public CreateBeaconCallbackImpl(CreateBeaconCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onResponse(Call<Void> call, Response<Void> response) {
            if (response.isSuccessful()) {
                callback.call(null);
            } else {
                callback.call(new Exception("Create a beacon failed"));
            }
        }

        @Override
        public void onFailure(Call<Void> call, Throwable t) {
            callback.call(new Exception("Create a Beacon failed: " + t.getMessage()));
        }
    }

    private static class BeaconListCallbackImpl implements Callback<List<BeaconDTO>> {
        private BeaconListCallback callback;
        public BeaconListCallbackImpl(BeaconListCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onResponse(Call<List<BeaconDTO>> call, Response<List<BeaconDTO>> response) {
            if (response.isSuccessful()) {
                List<BeaconDTO> result = response.body();
                callback.call(result, null);
            } else {
                callback.call(Collections.emptyList(), new Exception("Invalid response"));
            }
        }

        @Override
        public void onFailure(Call<List<BeaconDTO>> call, Throwable t) {
            callback.call(Collections.emptyList(), new Exception("Invalid response" + t.getMessage()));
        }
    }
}
