package mars.ring.domain.model.beacontag;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import mars.ring.application.RingApp;
import mars.ring.application.util.GsonHelper;
import mars.ring.domain.shared.ErrorBodyDTO;
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
    private static final String TAG = BeaconsRepo.class.getSimpleName();
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
//        clientBuilder.addInterceptor(authRepo.getAccessTokenRetryInterceptor());
        clientBuilder.addInterceptor(logger);
        clientBuilder.connectTimeout(30, TimeUnit.SECONDS);
        clientBuilder.readTimeout(30, TimeUnit.SECONDS);

        OkHttpClient client = clientBuilder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BEACONS_URL_BASE)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(GsonHelper.GSON))
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

    public void updateBeacon(BeaconDTO dto, UpdateBeaconCallback callback) {
        Call<Void> request = beaconsAPI.updateBeacon(dto);
        request.enqueue(new UpdateBeaconCallbackImpl(callback));
    }

    public void deleteBeacon(Long id, DeleteBeaconCallback callback) {
        Call<Void> request = beaconsAPI.deleteBeacon(id);
        request.enqueue(new DeleteBeaconCallbackImpl(callback));
    }

    public void lastLocations(BeaconLocationsCallback callback) {
        Call<List<BeaconLTDTO>> request = beaconsAPI.lastLocations();
        request.enqueue(new BeaconLocationsCallbackImpl(callback));
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
                try {
                    String errorBody = response.errorBody().string();
                    Log.w(TAG, errorBody);
                    ErrorBodyDTO error = new Gson().fromJson(errorBody, ErrorBodyDTO.class);
                    callback.call(new HttpException(response.code(), error.getTitle()));
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }

        @Override
        public void onFailure(Call<Void> call, Throwable t) {
            callback.call(new HttpException(503, "Create a Beacon failed: " + t.getMessage()));
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
                callback.call(Collections.emptyList(), new Exception("Response failed: " + response.code()));
            }
        }

        @Override
        public void onFailure(Call<List<BeaconDTO>> call, Throwable t) {
            callback.call(Collections.emptyList(), new Exception("Invalid response" + t.getMessage()));
        }
    }

    private static class BeaconLocationsCallbackImpl implements Callback<List<BeaconLTDTO>> {
        private final BeaconLocationsCallback callback;
        BeaconLocationsCallbackImpl(BeaconLocationsCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onResponse(Call<List<BeaconLTDTO>> call, Response<List<BeaconLTDTO>> response) {
            if (response.isSuccessful()) {
                List<BeaconLTDTO> result = response.body();
                callback.call(result, null);
            } else {
                callback.call(Collections.emptyList(), new HttpException(response.code(), "Response failed."));
            }
        }

        @Override
        public void onFailure(Call<List<BeaconLTDTO>> call, Throwable t) {
            callback.call(Collections.emptyList(), new HttpException(503, t.getMessage()));
        }
    }

    private static class UpdateBeaconCallbackImpl implements Callback<Void> {
        private UpdateBeaconCallback callback;
        public UpdateBeaconCallbackImpl(UpdateBeaconCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onResponse(Call<Void> call, Response<Void> response) {
            if (response.isSuccessful()) {
                callback.call(null);
            } else {
                try {
                    String errorBody = response.errorBody().string();
                    Log.w(TAG, errorBody);
                    ErrorBodyDTO error = new Gson().fromJson(errorBody, ErrorBodyDTO.class);
                    callback.call(new HttpException(response.code(), error.getTitle()));
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    callback.call(new HttpException(0, e.getMessage()));
                }
            }
        }

        @Override
        public void onFailure(Call<Void> call, Throwable t) {
            callback.call(new HttpException(503, "Update a Beacon failed: " + t.getMessage()));
        }
    }

    private static class DeleteBeaconCallbackImpl implements Callback<Void> {
        private DeleteBeaconCallback callback;
        public DeleteBeaconCallbackImpl(DeleteBeaconCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onResponse(Call<Void> call, Response<Void> response) {
            if (response.isSuccessful()) {
                callback.call(null);
            } else {
                try {
                    String errorBody = response.errorBody().string();
                    Log.w(TAG, errorBody);
                    ErrorBodyDTO error = new Gson().fromJson(errorBody, ErrorBodyDTO.class);
                    callback.call(new HttpException(response.code(), error.getTitle()));
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    callback.call(new HttpException(0, e.getMessage()));
                }
            }
        }

        @Override
        public void onFailure(Call<Void> call, Throwable t) {
            callback.call(new HttpException(503, "Delete a beacon failed: " + t.getMessage()));
        }
    }
}
