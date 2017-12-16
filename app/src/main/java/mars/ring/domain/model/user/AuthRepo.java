package mars.ring.domain.model.user;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.customtabs.CustomTabsIntent;
import android.text.TextUtils;
import android.util.Log;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;
import net.openid.appauth.browser.AnyBrowserMatcher;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import mars.ring.application.RingApp;
import mars.ring.application.auth.AuthStateManager;
import mars.ring.application.auth.Configuration;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by developer on 12/12/17.
 */

public class AuthRepo {
    private final String TAG = AuthRepo.class.getSimpleName();

    RingApp app;

    private Semaphore loginLock;
    private AuthLoginListener loginListener;

    private AuthorizationService authService;
    private AuthStateManager authStateManager;
    private Configuration configuration;
    String accessToken;

    public AuthRepo(RingApp app) {
        this.app = app;

        loginLock = new Semaphore(1);

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

    public void login(AuthLoginListener loginListener) {
        Log.i(TAG, "login called");
        lockLogins();
        if(isAuthorized()) {
            unlockLogins();
            return;
        }

        this.loginListener = loginListener;
        loginListener.onStart(AuthRepo.this, AuthEvent.AUTH_LOGIN_START);

        if (!isConfigured()) {
            Log.i(TAG, "Oops is not even configured!");
//            startServiceConfig();
        } else {
            startUserAuth();
        }

    }

    public void logout(AuthLogoutListener logoutListener) {
        lockLogins();

        if (!isAuthorized()) {
            unlockLogins();
            return;
        }

        logoutListener.onStart(AuthRepo.this, AuthEvent.AUTH_LOGOUT_START);

        if (isConfigured()) {
            AuthState currentState = authStateManager.getCurrent();
            AuthState clearedState =
                    new AuthState(currentState.getAuthorizationServiceConfiguration());
            if (currentState.getLastAuthorizationResponse() != null) {
                clearedState.update(currentState.getLastRegistrationResponse());
            }
            authStateManager.replace(clearedState);
        } else {
            // what if is not configured
        }
        logoutListener.onSuccess(AuthRepo.this, AuthEvent.AUTH_LOGOUT_SUCCESS);
        unlockLogins();
    }

    @WorkerThread
    // dangerous; do not call on UI thread.
    public String getAccessToken() {
        if (!isAuthorized()) {
            Log.w(TAG, "Want to get accessToken but is not authorized!");
            login(loginListenerImpl);
            return null;
        }

        authStateManager.getCurrent().performActionWithFreshTokens(authService, (
                                                                    String authToken,
                                                                    String idToken,
                                                                    AuthorizationException ex) -> {
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
//            displayNotAuthorized("Client authentication method is unsupported");
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

    private void startUserAuth() {
        Log.i(TAG, "Starting user auth");

        loginListener.onEvent(AuthRepo.this, AuthEvent.AUTH_USER_AUTH_START);

        AuthorizationRequest authRequest = createAuthRequest(null);
        CustomTabsIntent.Builder intentBuilder = authService.createCustomTabsIntentBuilder(authRequest.toUri());
        CustomTabsIntent authIntent = intentBuilder.build();
        Intent intent = authService.getAuthorizationRequestIntent(authRequest, authIntent);
        loginListener.onUserAgentRequest(AuthRepo.this, intent);
    }

    public void notifyUserAgentResponse(Intent data, int returnCode) {
        if (returnCode != RingApp.RC_AUTH) {
            failLogin(new AuthException("User authorization was cancelled"));
            return;
        }
        AuthorizationResponse resp = AuthorizationResponse.fromIntent(data);
        AuthorizationException ex = AuthorizationException.fromIntent(data);
        if (resp == null) {
            failLogin(new AuthException("User authorization failed"));
            return;
        } else {
            authStateManager.updateAfterAuthorization(resp, ex);
            finishUserAuth();
        }
    }

    private void finishUserAuth() {
        Log.i(TAG, "Finishing user auth");

        loginListener.onEvent(AuthRepo.this, AuthEvent.AUTH_USER_AUTH_FINISH);

        startCodeExchange();
    }

    private void startCodeExchange() {
        Log.i(TAG, "Starting code exchange.");
        loginListener.onEvent(AuthRepo.this, AuthEvent.AUTH_CODE_EXCHANGE_START);

        AuthorizationResponse resp = authStateManager.getCurrent().getLastAuthorizationResponse();
        authService.performTokenRequest(resp.createTokenExchangeRequest(), this::onTokenRequestCompleted);
    }

    private void onTokenRequestCompleted(TokenResponse resp, AuthorizationException ex) {
        if (resp == null) {
            failLogin(new AuthException(ex.getMessage()));
            return;
        }
        authStateManager.updateAfterTokenResponse(resp, ex);
        finishCodeExchange();
    }

    private void finishCodeExchange() {
        Log.i(TAG, "Finishing code exchange");

        loginListener.onEvent(AuthRepo.this, AuthEvent.AUTH_CODE_EXCHANGE_FINISH);
    }

    private void failLogin(AuthException ex) {
        Log.i(TAG, "Failing login");

        loginListener.onFailure(AuthRepo.this, AuthEvent.AUTH_LOGIN_FAILURE, ex);

        unlockLogins();
    }

    private void finishLogin() {
        Log.i(TAG, "Finishing login");

        loginListener.onSuccess(AuthRepo.this, AuthEvent.AUTH_LOGIN_SUCCESS);

        unlockLogins();
    }

    private AuthorizationRequest createAuthRequest(@Nullable String loginHint) {
        Log.i(TAG, "Creating auth request for login hint: " + loginHint);
        AuthorizationRequest.Builder authRequestBuilder = new AuthorizationRequest.Builder(
                authStateManager.getCurrent().getAuthorizationServiceConfiguration(),
                configuration.getClientId(),
                ResponseTypeValues.CODE,
                configuration.getRedirectUri())
                .setScope(configuration.getScope());
        if(!TextUtils.isEmpty(loginHint)) {
            authRequestBuilder.setLoginHint(loginHint);
        }
        return authRequestBuilder.build();
    }

    private void lockLogins() {
        try {
            loginLock.acquire();
        } catch (InterruptedException ex) {
            throw new RuntimeException("Unexpected interrupt", ex);
        }
    }

    private void unlockLogins() {
        loginLock.release();
    }

    private final AuthLoginListener loginListenerImpl =  new AuthLoginListener() {
        public void onStart(AuthRepo repo, AuthEvent event) {
            String description = event.getDescription();
            Log.i(TAG, description);
        }

        public void onEvent(AuthRepo repo, AuthEvent event) {
            String description = event.getDescription();
            switch (event) {
                case AUTH_SERVICE_DISCOVERY_START:
                    Log.i(TAG, description);
                    break;
                case AUTH_SERVICE_DISCOVERY_FINISH:
                    Log.i(TAG, description);
                    break;
                case AUTH_USER_AUTH_START:
                    Log.i(TAG, description);
                    break;
                case AUTH_USER_AUTH_FINISH:
                    Log.i(TAG, description);
                    break;
                case AUTH_CODE_EXCHANGE_START:
                    Log.i(TAG, description);
                    break;
                case AUTH_CODE_EXCHANGE_FINISH:
                    Log.i(TAG, description);
                    break;
                case AUTH_USER_INFO_START:
                    Log.i(TAG, description);
                    break;
                case AUTH_USER_INFO_FINISH:
                    Log.i(TAG, description);
                    break;
                default:
                    Log.i(TAG, description);
                    break;
            }
        }

        public void onUserAgentRequest(AuthRepo repo, Intent intent) {

            Log.i(TAG, "User Agent Request!");

        }

        public void onSuccess(AuthRepo repo, AuthEvent event) {
            String description = event.getDescription();
            Log.i(TAG, description);
        }

        public void onFailure(AuthRepo repo, AuthEvent event, AuthException ex) {
            String description = event.getDescription() + ": " + ex.getMessage();
            Log.w(TAG, description);
        }
    };

}
