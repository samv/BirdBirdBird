
package mobi.birdbirdbird.task;

import java.util.HashMap;

import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import mobi.birdbirdbird.model.TwitterAuthInfo;
import mobi.birdbirdbird.typedef.Twitter;

// TODO: should be able to make this work:

// implements TwitterClient.Resource<Twitter.User>.Callbacks
//            TwitterClient.ListResource<Twitter.Tweet>.Callbacks

//  that way, onResource(X) could overload, and a single class could
//  receive all the callback types it likes, without the requirement
//  of the caller to perform run-time type casting.

public class TwitterClient extends AsyncTask<OAuthRequest, Void, Object> {

    private static final String TWITTER_API = "https://api.twitter.com/1.1";
    public static final String HOME_TIMELINE = "/statuses/home_timeline";
    public static final String VERIFY_CREDENTIALS = "/account/verify_credentials";
    public static final String TWEET_NEW = "/statuses/update";
    private static final String API_EXT = ".json";

    private Token accessToken;
    private OAuthService service;

    private Callbacks cb;
    private ObjectMapper om;
    private Class valueType;
    private TypeReference valueTypeRef;
    private Exception exc_info;

    public TwitterClient(TwitterAuthInfo authInfo,
                         Class valueType,
                         Callbacks handlers)
    {
        this.valueType = valueType;
        cb = handlers;
        setUpService(authInfo);
        setUpMapper();
    }

    public TwitterClient(TwitterAuthInfo authInfo,
                         TypeReference typeRef, Callbacks handlers)
    {
        this.valueTypeRef = typeRef;
        cb = handlers;
        setUpService(authInfo);
        setUpMapper();
    }

    private void setUpService(TwitterAuthInfo authInfo) {
        service = authInfo.getService();
        accessToken = authInfo.getAccessToken();
    }

    private void setUpMapper() {
        om = new ObjectMapper();
        om.configure
            (DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    protected Object doInBackground(OAuthRequest... request) {
        Object converted = null;
        exc_info = null;
        try {
            this.service.signRequest(accessToken, request[0]);
            Response rs = request[0].send();
            if (rs.isSuccessful()) {
                if (valueType != null) {
                    converted = om.readValue(rs.getStream(), valueType);
                }
                else {
                    converted = om.readValue(rs.getStream(), valueTypeRef);
                }
            }
            else {
                throw new Exception
                    ("HTTP Error " + rs.getCode() + ": " + rs.getMessage());
            }
        }
        catch (Exception e) {
            exc_info = e;
        }
        return converted;
    }

    protected void onPostExecute(Object result) {
        if (result == null) {
            cb.onFailure(exc_info);
        }
        else {
            cb.onResponse(result);
        }
    }

    public void getTweets(String endpoint, String max_id) {
        OAuthRequest oar = new OAuthRequest
            (Verb.GET, TWITTER_API + endpoint + API_EXT);
        if (max_id != null) {
            oar.addQuerystringParameter("max_id", max_id);
        }
        execute(oar);
    }

    public void getUser(String endpoint) {
        OAuthRequest oar = new OAuthRequest
            (Verb.GET, TWITTER_API + endpoint + API_EXT);
        execute(oar);
    }

    public void postTweet(String endpoint, Twitter.Tweet tweet) {
        OAuthRequest oar = new OAuthRequest
            (Verb.POST, TWITTER_API + endpoint + API_EXT);
        oar.addQuerystringParameter("status", tweet.text);
        Log.d("DEBUG", "Tweeting '" + tweet.text + "'");
        execute(oar);
    }

    public interface Callbacks {
        abstract void onResponse(Object rs);
        abstract void onFailure(Exception e);
    }
}
