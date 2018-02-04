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
import mars.ring.domain.model.user.AuthRepo;
import mars.ring.domain.shared.ErrorBodyDTO;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by developer on 18/01/18.
 */

public class BeaconsPublicRepo {
    private static final String TAG = BeaconsRepo.class.getSimpleName();
    private final String BEACONS_URL_BASE = "https://ring.webebook.org/api/";

    private RingApp app;
    private BeaconsAPI beaconsAPI;

    public BeaconsPublicRepo(RingApp app) {
        this.app = app;
        this.beaconsAPI = createBeaconsPublicAPI();
    }

    private BeaconsAPI createBeaconsPublicAPI() {
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        logger.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder clientBuilder = new OkHttpClient().newBuilder();
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

    public void sendBeaconLT(Set<BeaconLTCommand> beaconLTSet, SendBeaconLTCallback callback) {
        Call<Void> request = beaconsAPI.sendBeaconLT(beaconLTSet);
        request.enqueue(new SendBeaconLTCallbackImpl(callback));
    }

    private static class SendBeaconLTCallbackImpl implements Callback<Void> {
        private final SendBeaconLTCallback callback;
        SendBeaconLTCallbackImpl(SendBeaconLTCallback callback) {
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
                } catch(Exception e) {
                    callback.call(new HttpException(0, "Error on parse error!"));
                    Log.e(TAG, "Exception: ", e);
                }
            }
        }
        @Override
        public void onFailure(Call<Void> call, Throwable t) {
            callback.call(new HttpException(503, "Server is not available!"));
        }
    }

}
