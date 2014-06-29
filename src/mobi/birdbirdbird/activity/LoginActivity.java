package mobi.birdbirdbird.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import mobi.birdbirdbird.R;
import mobi.birdbirdbird.model.TwitterApi;
import mobi.birdbirdbird.model.TwitterAuthInfo;
import mobi.birdbirdbird.task.RestCall;
import mobi.birdbirdbird.task.TwitterOaLoginTask;
import mobi.birdbirdbird.typedef.Twitter;
import org.scribe.model.OAuthRequest;

public class LoginActivity
    extends Activity
    implements View.OnClickListener,
               MenuItem.OnMenuItemClickListener,
               TwitterOaLoginTask.Callbacks,
               RestCall.RS<Twitter.User>
{
    private TwitterOaLoginTask loginTask;
    private ImageButton btnLogin;

    private TwitterAuthInfo authInfo;
    private SharedPreferences prefs;

    public void loadAuth() {
        prefs = getSharedPreferences("AuthData", MODE_PRIVATE);
        authInfo = new TwitterAuthInfo(prefs);
    }

    public void saveAuth() {
        SharedPreferences.Editor editor = prefs.edit();
        authInfo.savePreferences(editor);
        editor.commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d("DEBUG", "LoginActivity.onCreate()");

        btnLogin = (ImageButton) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        loadAuth();
                
        if (authInfo.haveUser()) {
            // already have a token, so skip to the stream
            Log.d("DEBUG", "Already logged in, skipping to stream!");
            openStream();
        }
        else if (authInfo.haveAccessToken()) {
            Log.d("DEBUG", "Have access token, need to get user");
            getUser();
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);

        MenuItem item = menu.findItem(R.id.miLogin);
        if (item != null)
            item.setOnMenuItemClickListener(this);

        item = menu.findItem(R.id.miAbout);
        if (item != null)
            item.setOnMenuItemClickListener(this);
		return true;
	}
	
    public void onClick(View v) {
        // the only button is login, so let's go!
        loginToRest();
    }

    public boolean onMenuItemClick(MenuItem mi) {
        loginToRest();
        return true;
    }

    public void loginToRest() {
        loginTask = new TwitterOaLoginTask(this);
        Log.d("DEBUG", "Starting new loginTask");
        authInfo.clearSession();
        loginTask.execute(authInfo);
    }

    // TwitterOaLoginTask.Callbacks
    public void onOaLoginFailure(Exception e) {
        Log.d("DEBUG", "LoginActivity.onOaLoginFailure(" + e + ")");
        authInfo.clearSession();
        saveAuth();
        e.printStackTrace();
    }

    public void onRestFailure(Exception e) {
        Toast.makeText
            (this, "Error during API call: " + e.toString(),
             Toast.LENGTH_LONG).show();
        Log.d("DEBUG", "LoginActivity.onRestFailure(" + e + ")");
        authInfo.clearSession();
        saveAuth();
        e.printStackTrace();
    }

    public void onOaLoginProgress(String message) {
        Log.d("DEBUG", "LoginActivity.onOaLoginProgress('" + message + "')");
    }

    public void onOaLoginSuccess(TwitterAuthInfo authInfo) {
        Log.d("DEBUG", "LoginActivity.onOaLoginSuccess()");
        this.authInfo = authInfo;
        saveAuth();
        if (authInfo.haveAccessToken()) {
            if (authInfo.haveUser())
                openStream();
            else
                getUser();
        }
        else {
            openLogin();
        }
    }

    public void onRestResponse(Twitter.User user) {
        Log.d("DEBUG", "LoginActivity.onRestResponse(" + user + ")");
        this.authInfo.setUser(user);
        saveAuth();
        openStream();
    }

    public void openLogin() {
        Intent intent = new Intent(Intent.ACTION_VIEW, authInfo.getAuthUri());
        startActivity(intent);
    }

    // on return from the OAuth login...
	@Override
	protected void onNewIntent(Intent intent) {
        Log.d("DEBUG", "LoginActivity.onNewIntent()");
		super.onNewIntent(intent);
		setIntent(intent);
	}

    private void getUser() {
        TwitterApi twitterApi = new TwitterApi(authInfo);
        twitterApi.getUser(TwitterApi.ACCOUNT_VERIFY, this);
    }

	@Override
	protected void onResume() {
		super.onResume();
        Log.d("DEBUG", "LoginActivity.onResume()");
        loadAuth();
        if (authInfo.haveRequestToken()) {
            if (authInfo.haveAccessToken()) {
                if (authInfo.haveUser()) {
                    // already have a token, so skip to the stream
                    Log.d("DEBUG", "have user, opening stream");
                    openStream();
                }
                else {
                    getUser();
                }
            }
            else {
                Log.d("DEBUG", "no access token, checking intent");
                Uri uri = getIntent().getData();
                if (uri != null) {
                    Log.d("DEBUG", "found URI intent: " + uri);
                    authInfo.setAuthUri(uri);
                    saveAuth();
                    Log.d("DEBUG", "saved authInfo preferences");
                    loginTask = new TwitterOaLoginTask(this);
                    loginTask.execute(authInfo);
                }
            }
        }
	}

    public void openStream() {
    	Intent i = new Intent(this, StreamActivity.class);
    	startActivity(i);
    }
}
