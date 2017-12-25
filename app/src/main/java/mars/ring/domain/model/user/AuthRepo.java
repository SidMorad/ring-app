package mars.ring.domain.model.user;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthorizationException;

import net.openid.appauth.AuthorizationService;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;
import net.openid.appauth.browser.AnyBrowserMatcher;

import java.io.IOException;

import mars.ring.application.RingApp;
import mars.ring.interfaces.auth.LoginActivity;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by developer on 12/12/17.
 */

public class AuthRepo {
    private final String TAG = AuthRepo.class.getSimpleName();

    RingApp app;

    private AuthorizationService authService;
    private AuthStateManager authStateManager;
    private Configuration configuration;
    String accessToken;

    public AuthRepo(RingApp app) {
        this.app = app;

        authStateManager = AuthStateManager.getInstance(app);
        accessToken = authStateManager.getCurrent().getAccessToken();
        configuration = Configuration.getInstance(app);
        AppAuthConfiguration.Builder builder = new AppAuthConfiguration.Builder();
        builder.setBrowserMatcher(AnyBrowserMatcher.INSTANCE);
        builder.setConnectionBuilder(configuration.getConnectionBuilder());
        authService = new AuthorizationService(app, builder.build());
    }

    public boolean isConfigured() {
        return authStateManager.getCurrent().getAuthorizationServiceConfiguration() != null &&
                configuration.isValid();
    }

    public boolean isAuthorized() {
        Log.d(TAG, "isAuthorized: " + authStateManager.getCurrent().isAuthorized());
        return authStateManager.getCurrent().isAuthorized();
    }

    @WorkerThread
    public String getAccessToken() {
        if (!isAuthorized()) {
            Log.w(TAG, "Want to get accessToken but is not authorized!");
            app.startActivity(new Intent(app, LoginActivity.class));
            return null;
        }

        authStateManager.getCurrent().performActionWithFreshTokens(authService,
                (String authToken, String idToken, AuthorizationException ex) -> {
            if (ex != null) {
                Log.e(TAG, "performActionWithFreshTokens throw exception: ", ex);
                refreshAccessToken();
            } else {
                accessToken = authToken;
            }
        });

        return accessToken;
    }


    @WorkerThread
    private void refreshAccessToken() {
        Log.i(TAG,"RefreshAccessToken occoured");
        performTokenRequest(
                authStateManager.getCurrent().createTokenRefreshRequest(),
                this::handleAccessTokenResponse);
    }

    @WorkerThread
    private void performTokenRequest(
            TokenRequest request,
            AuthorizationService.TokenResponseCallback callback) {
        ClientAuthentication clientAuthentication;
        try {
            clientAuthentication = authStateManager.getCurrent().getClientAuthentication();
        } catch (ClientAuthentication.UnsupportedAuthenticationMethod ex) {
            Log.d(TAG, "Token request cannot be made, client authentication for the token "
                    + "endpoint could not be constructed (%s)", ex);
            return;
        }

        authService.performTokenRequest(
                request,
                clientAuthentication,
                callback);
    }

    @WorkerThread
    private void handleAccessTokenResponse(
            @Nullable TokenResponse tokenResponse,
            @Nullable AuthorizationException authException) {
        authStateManager.updateAfterTokenResponse(tokenResponse, authException);
    }

    public Interceptor getAccessTokenInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                String accessToken = getAccessToken();
                request = request.newBuilder()
                        .header("X-Android-Package", app.getPackageName())
                        .header("X-Android-Cert", app.getSignature())
                        .header("Authorization", "Bearer " + accessToken)
                        .build();
                return chain.proceed(request);
            }
        };
    }

    public Interceptor getAccessTokenRetryInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Response response = null;
                boolean responseOk = false;
                int responseCode = 0;
                int tryCount = 0;

                while(!responseOk && tryCount < 2) {
                    try {
                        if (responseCode == 401) {  // Here we check if error code is 401, if yes then we try one more time with new access token
                            request = request.newBuilder().header("Authorization", "Bearer " + accessToken).build();
                        } else if (responseCode == 0) {
                        } else {
                            tryCount++;
                            break;
                        }
                        response = chain.proceed(request);
                        responseOk = response.isSuccessful();
                        responseCode = response.code();
                    } catch (Exception e) {
                        Log.d(TAG, "Request was no successful try number " + tryCount);
                    } finally {
                        tryCount++;
                    }
                }

                return response;
            }
        };
    }

}
