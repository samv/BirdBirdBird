
package as.streamed;

import java.io.Serializable;

import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

public class TwitterAuthInfo implements Serializable {

    private String consumerKey;
    private String consumerSecret;
    private String callbackUrl = "oauth://streamed.as";
    public String requestToken;
    public String requestSecret;
    public String authorizeUrl;
    public String accessToken;
    public String accessSecret;

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
    }

    private void _secrets() {
        consumerKey = "API_KEY_API_KEY_API_KEY_A";
        consumerSecret = "API_SECRET_API_SECRET_API_SECRET_API_SECRET_API_SE";
    }

    public TwitterAuthInfo(SharedPreferences prefs) {
        _secrets();
        if (prefs.contains("oauth_request_token")) {
            requestToken = prefs.getString("oauth_request_token", "");
            requestSecret = prefs.getString("oauth_request_secret", "");
        }
        else {
            requestToken = null;
        }

        authorizeUrl = prefs.getString("oauth_auth_url", null);

        if (prefs.contains("oauth_access_token")) {
            accessToken = prefs.getString("oauth_access_token", "");
            accessSecret = prefs.getString("oauth_access_secret", "");
        }
        else {
            accessToken = null;
        }
    }

    public Token getRequestToken() {
        return new Token(requestToken, requestSecret);
    }

    public boolean haveRequestToken() {
        return requestToken != null;
    }

    public void setRequestToken(Token rqToken) {
        requestToken = rqToken.getToken();
        requestSecret = rqToken.getSecret();
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
    }

    public void savePreferences(SharedPreferences.Editor editor) {
        if (haveRequestToken()) {
            editor.putString("oauth_request_token", requestToken);
            editor.putString("oauth_request_secret", requestSecret);
            editor.putString("oauth_auth_url", authorizeUrl);
        }
        else {
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
            editor.remove("oauth_access_token");
            editor.remove("oauth_access_secret");
        }
        editor.apply();
    }

    public OAuthService getService() {
        return new ServiceBuilder()
            .provider(TwitterApi.class)
            .apiKey(consumerKey)
            .apiSecret(consumerSecret)
            .callback(callbackUrl)
            .build();
    }
}



