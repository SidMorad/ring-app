/*package mars.ring.application.util;

import android.app.Activity;

import org.jboss.aerogear.android.authorization.AuthorizationManager;
import org.jboss.aerogear.android.authorization.AuthzModule;
import org.jboss.aerogear.android.authorization.oauth2.OAuth2AuthorizationConfiguration;
import org.jboss.aerogear.android.authorization.oauth2.OAuthWebViewDialog;
import org.jboss.aerogear.android.core.Callback;
import org.jboss.aerogear.android.pipe.PipeManager;
import org.jboss.aerogear.android.pipe.rest.RestfulPipeConfiguration;
import org.jboss.aerogear.android.pipe.rest.gson.GsonRequestBuilder;

import java.net.URL;

import mars.ring.domain.model.user.User;

/**
 * Keycloak helper class for handing OAuth 2.0 authentication/authorization
 * Created by developer on 06/12/17.
 *

public class KeycloakHelper {

    private static final String AUTH_SERVER_URL = "https://ring.webebook.org";
    private static final String AUTHZ_URL = AUTH_SERVER_URL + "/auth";
    private static final String AUTHZ_ENDPOINT = "/realms/jhipster/protocol/openid-connect/auth";
    private static final String ACCESS_TOKEN_ENDPOINT = "/realms/jhipster/protocol/openid-connect/token";
    private static final String REFRESH_TOKEN_ENDPOINT = "/realms/jhipster/protocol/openid-connect/token";
    private static final String AUTHZ_ACCOUNT_ID = "jhipster";
    private static final String AUTHZ_CLIENT_ID = "web_app";
    private static final String AUTHZ_REDIRECT_URL = "http://oauth2callback";
    private static final String MODULE_NAME = "keycloak";

    static {
        try {
            AuthorizationManager.config(MODULE_NAME, OAuth2AuthorizationConfiguration.class)
                    .setBaseURL(new URL(AUTHZ_URL))
                    .setAuthzEndpoint(AUTHZ_ENDPOINT)
                    .setAccessTokenEndpoint(ACCESS_TOKEN_ENDPOINT)
                    .setRefreshEndpoint(REFRESH_TOKEN_ENDPOINT)
                    .setAccountId(AUTHZ_ACCOUNT_ID)
                    .setClientId(AUTHZ_CLIENT_ID)
                    .setRedirectURL(AUTHZ_REDIRECT_URL)
                    .asModule();
            PipeManager.config("api-account-get", RestfulPipeConfiguration.class).module(AuthorizationManager.getModule(MODULE_NAME))
                    .withUrl(new URL(AUTH_SERVER_URL + "/api/account"))
                    .requestBuilder(new GsonRequestBuilder())
                    .forClass(User.class);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void connect(final Activity activity, final Callback callback) {
        try {
            final AuthzModule authzModule = AuthorizationManager.getModule(MODULE_NAME);

            authzModule.requestAccess(activity, new Callback<String>() {
                @Override
                public void onSuccess(String data) {
                    callback.onSuccess(data);
                }

                @Override
                public void onFailure(Exception e) {
                    if (!e.getMessage().matches(OAuthWebViewDialog.OAuthReceiver.DISMISS_ERROR)) {
                        authzModule.deleteAccount();
                    }
                    callback.onFailure(e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static boolean isConnected() {
        return AuthorizationManager.getModule(MODULE_NAME).isAuthorized();
    }

    public static void getAccount(final Callback callback, Activity activity) {
        PipeManager.getPipe("api-account-get", activity).read(callback);
    }
}
*/