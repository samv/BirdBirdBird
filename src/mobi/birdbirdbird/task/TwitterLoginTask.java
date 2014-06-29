package mobi.birdbirdbird.task;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.scribe.exceptions.OAuthException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Verifier;
import org.scribe.model.Token;
import org.scribe.model.OAuthConstants;
import org.scribe.oauth.OAuthService;

import mobi.birdbirdbird.model.TwitterAuthInfo;

public class TwitterLoginTask extends AsyncTask<TwitterAuthInfo, String, TwitterAuthInfo> {

    private ResultCallbacks cb;
    private Exception exc_info = null;

    public TwitterLoginTask(ResultCallbacks rcb)
    {
        this.cb = rcb;
    }

    protected TwitterAuthInfo doInBackground(TwitterAuthInfo... authInfo) {
        TwitterAuthInfo newAuthInfo = new TwitterAuthInfo(authInfo[0]);
        try {
            OAuthService service = newAuthInfo.getService();
            if (!newAuthInfo.haveRequestToken())
                getRequestToken(service, newAuthInfo);
            else if (!newAuthInfo.haveAccessToken())
                getAccessToken(service, newAuthInfo);
        }
        catch (Exception e) {
            exc_info = e;
        }
        return newAuthInfo;
    }

    private void getRequestToken(OAuthService service,
                                 TwitterAuthInfo authInfo)
    {
        publishProgress("connecting");
        Token rqToken = service.getRequestToken();
        Log.d("DEBUG", "request token is " + rqToken);
        authInfo.setRequestToken(rqToken);
        publishProgress("got request Token");

        authInfo.authorizeUrl = service.getAuthorizationUrl(rqToken);
        publishProgress("got authorization URL");
    }

    private void getAccessToken(OAuthService service,
                                TwitterAuthInfo authInfo)
    {
        String oauth_verifier = null;
        Uri authUrl = authInfo.getAuthUri();
        if (authUrl.getQuery().contains(OAuthConstants.CODE)) {
            oauth_verifier = authUrl.getQueryParameter(OAuthConstants.CODE);
        }
        else if (authUrl.getQuery().contains(OAuthConstants.VERIFIER))
        {
            oauth_verifier = authUrl.getQueryParameter
                (OAuthConstants.VERIFIER);
        }

        // Use verifier token to fetch access token
        if (oauth_verifier != null) {
            publishProgress("getting access token");
            Token accessToken = service.getAccessToken
                (authInfo.getRequestToken(), new Verifier(oauth_verifier));
            authInfo.setAccessToken(accessToken);
        } else {
            throw new OAuthException
                ("No verifier code was returned with uri '" + authUrl +
                 "' and access token cannot be retrieved");
        }
    }

    protected void onProgressUpdate(String... progress) {
        cb.onProgress(progress[0]);
    }
    
    protected void onPostExecute(TwitterAuthInfo new_state) {
        if (exc_info == null) {
            cb.onSuccess(new_state);
        }
        else {
            cb.onFailure(exc_info);
        }
    }

    public interface ResultCallbacks {
        public void onSuccess(TwitterAuthInfo authInfo);
        public void onProgress(String message);
        public void onFailure(Exception e);
    }
}
