
package as.streamed;

import java.util.HashMap;

import android.os.AsyncTask;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;


public class TwitterClient extends AsyncTask<OAuthRequest, Integer, Response> {

    private Token accessToken;
    private OAuthService service;

    private Callbacks cb;
    private Exception exc_info;

    public TwitterClient(TwitterAuthInfo authInfo, Callbacks handlers) {
        service = authInfo.getService();
        accessToken = authInfo.getAccessToken();
        cb = handlers;
    }

    protected Response doInBackground(OAuthRequest... request) {
        Response rs = null;
        exc_info = null;
        try {
            this.service.signRequest(accessToken, request[0]);
            rs = request[0].send();
        }
        catch (Exception e) {
            exc_info = e;
        }
        return rs;
    }

    protected void onPostExecute(Response result) {
        if (result == null) {
            cb.onFailure(exc_info);
        }
        else {
            cb.onResponse(result);
        }
    }

    public interface Callbacks {
        abstract void onResponse(Response rs);
        abstract void onFailure(Exception e);
    }

}
