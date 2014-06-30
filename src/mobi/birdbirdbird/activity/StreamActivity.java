package mobi.birdbirdbird.activity;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import java.util.ArrayList;
import java.util.List;
import mobi.birdbirdbird.R;
import mobi.birdbirdbird.adapter.TweetsArrayAdapter;
import mobi.birdbirdbird.model.TwitterApi;
import mobi.birdbirdbird.model.TwitterAuthInfo;
import mobi.birdbirdbird.task.RestListCall;
import mobi.birdbirdbird.typedef.Twitter;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Verb;

public class StreamActivity
    extends Activity
    implements MenuItem.OnMenuItemClickListener,
               View.OnCreateContextMenuListener,
               StreamFragment.Callbacks,
               View.OnClickListener
{
    private TwitterApi twitterApi;
    private AsyncTask getUsersCall;
    private TwitterAuthInfo authInfo;
    private SharedPreferences prefs;
    private StreamFragment home;
    private StreamFragment mentions;
    private ImageLoader imageLoader;
    private Button btnHome;
    private Button btnMentions;

    public void loadAuth() {
        prefs = getSharedPreferences("AuthData", MODE_PRIVATE);
        authInfo = new TwitterAuthInfo(prefs);
    }

    public void saveAuth() {
        SharedPreferences.Editor editor = prefs.edit();
        authInfo.savePreferences(editor);
        editor.apply();
        editor.commit();
    }

    public TwitterAuthInfo getAuthInfo() {
        if (authInfo == null)
            loadAuth();
        return authInfo;
    }

    public TwitterApi getTwitterApi() {
        if (twitterApi == null)
            twitterApi = new TwitterApi(getAuthInfo());
        Log.d("DEBUG", "returning api: " + twitterApi);
        return twitterApi;
    }

    public ImageLoader getImageLoader() {
        if (imageLoader == null) {
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder
                (getApplicationContext())
                .writeDebugLogs()
                .build();
            imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);
        }
        return imageLoader;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);
        Log.d("DEBUG", "StreamActivity.onCreate()");

        if (!getAuthInfo().haveAccessToken()) {
            Log.d("DEBUG", "StreamActivity.onCreate: no access token, returning");
            this.finish();
            return;
        }

        home = createStreamFragment(TwitterApi.STATUS_HOME);

        Toast.makeText
            (this, "Welcome, " + getAuthInfo().getUser().name,
             Toast.LENGTH_SHORT)
            .show();

        connectButtons();
    }

    void connectButtons() {
        btnHome = (Button) findViewById(R.id.btnHome);
        btnHome.setOnClickListener(this);
        btnMentions = (Button) findViewById(R.id.btnMentions);
        btnMentions.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btnHome:
            chooseStream(home);
            home.getNewTweets();
            break;
        case R.id.btnMentions:
            if (mentions != null) {
                chooseStream(mentions);
                mentions.getNewTweets();
            }
            else
                mentions = createStreamFragment(TwitterApi.STATUS_MENTIONS);
            break;
        default:
            Log.d("WARNING", "unknown click source");
            break;
        }
    }

    StreamFragment createStreamFragment(String endpoint) {
        StreamFragment stream = new StreamFragment(endpoint, this);
        chooseStream(stream);
        stream.getTweets();
        return stream;
    }

    void chooseStream(StreamFragment stream) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.flStream, stream);
        ft.commit();
    }

    public void onProfileClick(Twitter.User user) {
        Intent i = new Intent(this, ProfileActivity.class);
        i.putExtra("user", user);
        startActivity(i);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stream, menu);
        for (int i=0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            item.setOnMenuItemClickListener(this);
        }
		return true;
	}

    @Override
    public boolean onMenuItemClick(MenuItem mi) {
        switch (mi.getItemId()) {
        case (R.id.miAbout):
            Log.d("DEBUG", "About menu pressed");
            break;
        case (R.id.miProfile):
            Log.d("DEBUG", "Profile menu pressed");
            onProfileClick(getAuthInfo().getUser());
            break;
        case (R.id.miLogout):
            Log.d("DEBUG", "Logout menu button pressed");
            authInfo.clearSession();
            saveAuth();
            this.finish();
            break;
        case (R.id.miCompose):
            Log.d("DEBUG", "Compose menu button pressed");
            composeTweet();
            break;
        }
        return true;
    }

    private final int REQUEST_COMPOSE = 42;

    public void composeTweet() {
        Log.d("DEBUG", "About to create new intent; this = " + this);
        Intent i = new Intent(this, TweetActivity.class);
        startActivityForResult(i, REQUEST_COMPOSE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        if (resultCode == RESULT_OK && requestCode == REQUEST_COMPOSE) {
            Log.d("DEBUG", "Got result!");
            home.getNewTweets();
        }
    }
}
