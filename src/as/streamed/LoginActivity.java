package as.streamed;

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
import android.widget.Button;

import org.scribe.model.OAuthRequest;

import as.streamed.TwitterLoginTask;
import as.streamed.TwitterAuthInfo;

public class LoginActivity
    extends Activity
    implements View.OnClickListener,
               MenuItem.OnMenuItemClickListener,
               TwitterLoginTask.ResultCallbacks
{
    private TwitterLoginTask loginTask;
    private Button btnLogin;

    private TwitterAuthInfo authInfo;
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d("DEBUG", "onCreate()");

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        prefs = getPreferences(MODE_PRIVATE);
        authInfo = new TwitterAuthInfo(prefs);

        if (authInfo.haveAccessToken()) {
            // already have a token, so skip to the stream
            Log.d("DEBUG", "Already logged in, skipping to stream!");
            openStream();
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);

        MenuItem item = menu.findItem(R.id.miLogin);
        item.setOnMenuItemClickListener(this);

        item = menu.findItem(R.id.miAbout);
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
        loginTask = new TwitterLoginTask(this);
        Log.d("DEBUG", "Starting new loginTask");
        loginTask.execute(authInfo);
    }

    public void onFailure(Exception e) {
        Log.d("DEBUG", "onFailure(" + e + ")");
        authInfo.clearRequestToken();
        authInfo.savePreferences(prefs.edit());
        e.printStackTrace();
    }

    public void onProgress(String message) {
        Log.d("DEBUG", "onProgress('" + message + "')");
    }

    public void onSuccess(TwitterAuthInfo authInfo) {
        Log.d("DEBUG", "onSuccess()");
        this.authInfo = authInfo;
        authInfo.savePreferences(prefs.edit());
        if (authInfo.haveAccessToken()) {
            openStream();
        }
        else {
            openLogin();
        }
    }
    public void openLogin() {
        Intent intent = new Intent(Intent.ACTION_VIEW, authInfo.getAuthUri());
        startActivity(intent);
    }

    // on return from the OAuth login...
	@Override
	protected void onNewIntent(Intent intent) {
        Log.d("DEBUG", "onNewIntent()");
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
        Log.d("DEBUG", "onResume()");
        if (authInfo.haveAccessToken()) {
            // already have a token, so skip to the stream
            Log.d("DEBUG", "have access token, opening stream");
            openStream();
        }
        else {
            Log.d("DEBUG", "no access token, checking intent");
            Uri uri = getIntent().getData();
            if (uri != null) {
                Log.d("DEBUG", "found URI intent: " + uri);
                authInfo.setAuthUri(uri);
                authInfo.savePreferences(prefs.edit());
                Log.d("DEBUG", "saved authInfo preferences");
                loginTask = new TwitterLoginTask(this);
                loginTask.execute(authInfo);
            }
        }
	}

    public void openStream() {
    	Intent i = new Intent(this, StreamActivity.class);
        i.putExtra("auth", authInfo);
    	startActivity(i);
    }
    

}
