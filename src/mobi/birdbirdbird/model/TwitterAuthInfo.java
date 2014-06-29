
package mobi.birdbirdbird.model;

import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import mobi.birdbirdbird.typedef.Twitter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Serializable;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

public class TwitterAuthInfo {

    private String consumerKey;
    private String consumerSecret;
    private String callbackUrl = "oauth://birdbirdbird.mobi";
    private String requestToken;
    private String requestSecret;
    public String authorizeUrl; // FIXME
    private String accessToken;
    private String accessSecret;
    private Twitter.User user;
    private String userInfoJson;
    private ObjectMapper om;

    private ObjectMapper getOM() {
        if (om == null)
            om = new ObjectMapper();
        return om;
    }

    public TwitterAuthInfo() {
        _secrets();
    }

    public TwitterAuthInfo(TwitterAuthInfo other) {
        _secrets();
        this.requestToken = other.requestToken;
        this.requestSecret = other.requestSecret;
        this.authorizeUrl = other.authorizeUrl;
        this.accessToken = other.accessToken;
        this.accessSecret = other.accessSecret;
        this.userInfoJson = other.userInfoJson;
    }

    private void _secrets() {
        consumerKey = "v1jMG98f71HLs2xEi6fbb9JAL";
        consumerSecret = "pJ5CYbmAFw6KfW5IwAJ9oHA5qxi09ueCxqWYZr9pvdpsIPpEjD";
    }

    public TwitterAuthInfo(SharedPreferences prefs) {
        _secrets();
        Log.d("DEBUG", "Loading auth from " + prefs);
        if (prefs.contains("oauth_request_token")) {
            requestToken = prefs.getString("oauth_request_token", "");
            requestSecret = prefs.getString("oauth_request_secret", "");
            Log.d("DEBUG", "Loaded request token " + requestToken);
        }
        else {
            requestToken = null;
            Log.d("DEBUG", "No request token found.");
        }

        authorizeUrl = prefs.getString("oauth_auth_url", null);

        if (prefs.contains("oauth_access_token")) {
            accessToken = prefs.getString("oauth_access_token", "");
            accessSecret = prefs.getString("oauth_access_secret", "");
            Log.d("DEBUG", "Loaded access token " + accessToken);
        }
        else {
            accessToken = null;
            Log.d("DEBUG", "No access token found.");
        }

        userInfoJson = prefs.getString("user_info_json", null);
    }

    public Token getRequestToken() {
        return new Token(requestToken, requestSecret);
    }

    public void clearSession() {
        requestToken = null;
        requestSecret = null;
        clearAuth();
    }

    public void clearAuth() {
        authorizeUrl = null;
        accessToken = null;
        userInfoJson = null;
        user = null;
    }

    public boolean haveRequestToken() {
        return requestToken != null;
    }

    public void setRequestToken(Token rqToken) {
        requestToken = rqToken.getToken();
        requestSecret = rqToken.getSecret();
        clearAuth();
    }

    public Uri getAuthUri() {
        return Uri.parse(authorizeUrl);
    }

    public void setAuthUri(Uri uri) {
        authorizeUrl = uri.toString();
    }
    
    public Token getAccessToken() {
        return new Token(accessToken, accessSecret);
    }

    public boolean haveAccessToken() {
        return accessToken != null;
    }

    public void setAccessToken(Token accessToken) {
        this.accessToken = accessToken.getToken();
        this.accessSecret = accessToken.getSecret();
        this.userInfoJson = null;
    }

    public void savePreferences(SharedPreferences.Editor editor) {
        if (haveRequestToken()) {
            Log.d("DEBUG", "Saving request token " + requestToken);
            editor.putString("oauth_request_token", requestToken);
            editor.putString("oauth_request_secret", requestSecret);
            Log.d("DEBUG", "Saving auth URL " + authorizeUrl);
            editor.putString("oauth_auth_url", authorizeUrl);
        }
        else {
            Log.d("DEBUG", "Clearing request token");
            editor.remove("oauth_request_token");
            editor.remove("oauth_request_secret");
            editor.remove("oauth_auth_url");
        }
        if (haveRequestToken() && haveAccessToken()) {
            Log.d("DEBUG", "Saving access token " + accessToken);
            editor.putString("oauth_access_token", accessToken);
            editor.putString("oauth_access_secret", accessSecret);
        }
        else {
            Log.d("DEBUG", "Clearing access token");
            editor.remove("oauth_access_token");
            editor.remove("oauth_access_secret");
        }
        if (haveUser()) {
            Log.d("DEBUG", "Saving user info: " + userInfoJson);
            editor.putString("user_info_json", userInfoJson);
        }
        else {
            Log.d("DEBUG", "Clearing user");
            editor.remove("user_info_json");
        }
    }

    public boolean haveUser() {
        return userInfoJson != null;
    }

    public Twitter.User getUser() {
        if (user == null) {
            if (userInfoJson != null) {
                try {
                    user = getOM().readValue(userInfoJson, Twitter.User.class);
                }
                catch (IOException e) {
                    Log.d("DEBUG", "Jackson says: " + e);
                    clearSession();
                }
            }
        }
        return user;
    }

    public void setUser(Twitter.User user) {
        this.user = user;
        if (user == null)
            userInfoJson = null;
        else {
            try {
                userInfoJson = getOM().writeValueAsString(user);
            }
            catch (JsonProcessingException jpe) {
                Log.d("DEBUG", "Jackson says: " + jpe);
           }
        } 
    }

    public OAuthService getService() {
        return new ServiceBuilder()
            .provider(org.scribe.builder.api.TwitterApi.class)
            .apiKey(consumerKey)
            .apiSecret(consumerSecret)
            .callback(callbackUrl)
            .build();
    }
}
