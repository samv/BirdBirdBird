package mobi.birdbirdbird.model;

import android.os.AsyncTask;
import android.util.Log;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import mobi.birdbirdbird.task.RestCall;
import mobi.birdbirdbird.task.RestListCall;
import mobi.birdbirdbird.typedef.Twitter;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

public class TwitterApi {
    private Token accessToken;
    private OAuthService service;
    private ObjectMapper om;

    private static final String TWITTER_API = "https://api.twitter.com/1.1";
    private static final String API_EXT = ".json";

    public static final String ACCOUNT_VERIFY = "/account/verify_credentials";
    public static final String STATUS_HOME = "/statuses/home_timeline";
    public static final String STATUS_UPDATE = "/statuses/update";

    public TwitterApi(TwitterAuthInfo authInfo) {
        accessToken = authInfo.getAccessToken();
        service = authInfo.getService();
        om = new ObjectMapper();
        om.configure
            (DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public AsyncTask getUser(String endpoint,
                             RestCall.RS<Twitter.User> callbacks)
    {
        OAuthRequest oar = new OAuthRequest
            (Verb.GET, TWITTER_API + endpoint + API_EXT);
        service.signRequest(accessToken, oar);

        return new RestCall<Twitter.User>(om, callbacks)
            .setClass(Twitter.User.class)
            .execute(oar);
    }

    public AsyncTask getTweets(String endpoint, String max_id,
                               RestListCall.RS<Twitter.Tweet> callbacks)
    {
        OAuthRequest oar = new OAuthRequest
            (Verb.GET, TWITTER_API + endpoint + API_EXT);
        if (max_id != null) {
            oar.addQuerystringParameter("max_id", max_id);
        }
        service.signRequest(accessToken, oar);

        return new RestListCall<Twitter.Tweet>(om, callbacks)
            .setItemClass(Twitter.Tweet.class)
            .execute(oar);
    }

    public AsyncTask postTweet(String endpoint, Twitter.Tweet tweet,
                               RestCall.RS<Twitter.Tweet> callbacks)
    {
        OAuthRequest oar = new OAuthRequest
            (Verb.POST, TWITTER_API + endpoint + API_EXT);
        oar.addQuerystringParameter("status", tweet.text);
        service.signRequest(accessToken, oar);

        Log.d("DEBUG", "Tweeting '" + tweet.text + "'");
        return new RestCall<Twitter.Tweet>(om, callbacks)
            .setClass(Twitter.Tweet.class)
            .execute(oar);
    }
}
