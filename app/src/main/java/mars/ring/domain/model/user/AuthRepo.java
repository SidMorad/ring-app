package mars.ring.domain.model.user;

import android.content.Intent;
import android.support.annotation.AnyThread;
import android.support.annotation.Nullable;
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

    int accessTokenFailureCount = 0;

    @AnyThread
    public String getAccessToken() {
        if (!isAuthorized()) {
            Log.w(TAG, "Want to get accessToken but is not authorized!");
//            Handler handler = new Handler(Looper.getMainLooper());
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
                    Intent loginActivity = new Intent();
                    loginActivity.setClass(app, LoginActivity.class);
                    loginActivity.setAction(LoginActivity.class.getName());
//                    loginActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    app.startActivity(loginActivity);
//                }
//            });
            return null;
        }

        authStateManager.getCurrent().performActionWithFreshTokens(authService,
                (String authToken, String idToken, AuthorizationException ex) -> {
            if (ex != null) {
                Log.e(TAG, "performActionWithFreshTokens throw exception: " + accessTokenFailureCount, ex);
                accessTokenFailureCount++;
                if (accessTokenFailureCount > 3) {
                    refreshAccessToken();
                    accessTokenFailureCount = 0;
                }
            } else {
                accessToken = authToken;
                accessTokenFailureCount = 0;
            }
        });

        return accessToken;
    }


    @AnyThread
    private void refreshAccessToken() {
        Log.i(TAG,"RefreshAccessToken occoured");
        performTokenRequest(
                authStateManager.getCurrent().createTokenRefreshRequest(),
                this::handleAccessTokenResponse);
    }

    @AnyThread
    private void performTokenRequest(
            TokenRequest request,
            AuthorizationService.TokenResponseCallback callback) {
        ClientAuthentication clientAuthentication;
        try {
            clientAuthentication = authStateManager.getCurrent().getClientAuthentication();
        } catch (ClientAuthentication.UnsupportedAuthenticationMethod ex) {
            Log.d(TAG, "Token request cannot be made, client authentication for the token endpoint could not be constructed (%s)", ex);
            return;
        }

        authService.performTokenRequest(
                request,
                clientAuthentication,
                callback);
    }

    @AnyThread
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
                request = request.newBuilder()
                        .header("X-Android-Package", app.getPackageName())
                        .header("X-Android-Cert", app.getSignature())
                        .header("Authorization", "Bearer " + getAccessToken())
                        .build();
                return chain.proceed(request);
            }
        };
    }
/*
    private Interceptor getAccessTokenRetryInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Response response = null;
                boolean responseOk = false;
                int responseCode = 0;
                int tryCount = 0;

                while(!responseOk && tryCount < 1) {
                    try {
                        if (responseCode == 401) {  // Here we check if error code is 401, if yes then we try one more time with new access token
                            request = request.newBuilder().header("Authorization", "Bearer " + getAccessToken()).build();
//                        } else if (responseCode == 0) {
                        } else {
                            break;
                        }
                        response = chain.proceed(request);
                        responseOk = response.isSuccessful();
                        responseCode = response.code();
                    } catch (Exception e) {
                        Log.e(TAG, "Request was no successful try number " + tryCount, e);
                    } finally {
                        tryCount++;
                    }
                }

                return response;
            }
        };
    }
*/
}
