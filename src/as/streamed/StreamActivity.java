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
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import as.streamed.TweetsArrayAdapter;
import as.streamed.TwitterApi;
import as.streamed.TwitterAuthInfo;
import as.streamed.TwitterClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import java.util.ArrayList;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Verb;

public class StreamActivity
    extends Activity
    implements TweetsArrayAdapter.ActionCallbacks,
               MenuItem.OnMenuItemClickListener,
               AbsListView.OnScrollListener,
               View.OnCreateContextMenuListener,
               TwitterClient.Callbacks
{
    private ListView lvTweetStream;
    private TweetsArrayAdapter tweetsAdapter;
    private ArrayList<TwitterApi.Tweet> tweets;
    private String max_id;
    private ImageLoader imageLoader;
    private TwitterClient tc;
    private TwitterAuthInfo authInfo;
    private SharedPreferences prefs;

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

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);
        Log.d("DEBUG", "StreamActivity.onCreate()");

        loadAuth();

        Log.d("DEBUG", "Stream.onCreate: authInfo = " + authInfo);
        if (!authInfo.haveAccessToken()) {
            Log.d("DEBUG", "Stream.onCreate: no access token, returning");
            this.finish();
            return;
        }

        tc = new TwitterClient
            (authInfo, new TypeReference<ArrayList<TwitterApi.Tweet>>() {},
             this);

        // init the imageloader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder
            (getApplicationContext())
            .writeDebugLogs()
            .build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);

        // connect tweet stream
        lvTweetStream = (ListView) findViewById(R.id.lvTweetStream);
        tweets = new ArrayList<TwitterApi.Tweet>();
        tweetsAdapter = new TweetsArrayAdapter(this, tweets, this);
        lvTweetStream.setAdapter(tweetsAdapter);
        lvTweetStream.setOnScrollListener(this);

        // now, fill it with data!
        getNextPage();

        Toast.makeText
            (this, "Welcome, " + authInfo.getUser().name, Toast.LENGTH_SHORT)
            .show();
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
        case (R.id.miLogout):
            Log.d("DEBUG", "Logout menu button pressed");
            authInfo.clearAuth();
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

    // TweetsArrayAdapter.ActionCallbacks
    public void setImage(Uri uri, ImageView v) {
        imageLoader.displayImage(uri.toString(), v);
    }

    // AbsListView.OnScrollListener callbacks
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount)
    {
        //Log.d("DEBUG", "onScroll(" + view + ", " + firstVisibleItem +
        //      ", " + visibleItemCount + ", " + totalItemCount + ")");
        if (tweetsAdapter == null)
            return;
        if (firstVisibleItem + visibleItemCount == tweetsAdapter.getCount()) {
            //Log.d("DEBUG", "let's get some more items");
            getNextPage();
        }
        else {
            //Log.d("DEBUG", "We already have " + tweetsAdapter.getCount() +
            //      " items");
        }
    }

    public void	onScrollStateChanged(AbsListView view, int scrollState) {
        Log.d("DEBUG", "Scrolling!  state = " + scrollState);
    }

    private void getNextPage() {
        if (tc.getStatus() != AsyncTask.Status.PENDING) {
            //Log.d("DEBUG", "ignoring getNextPage(); client not ready");
        }
        else {
            int numItems = tweetsAdapter.getCount();
            if (numItems > 0)
                max_id = tweetsAdapter.getItem(numItems - 1).id_str;
            tc.getTweets(TwitterClient.HOME_TIMELINE, max_id);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        if (resultCode == RESULT_OK && requestCode == REQUEST_COMPOSE) {
            Log.d("DEBUG", "Got result!");
            max_id = data.getStringExtra("tweet_id");
            tweetsAdapter.clear();
            getNextPage();
        }
    }

    // TwitterClient.Callbacks
    public void onResponse(Object rs) {
        Log.d("DEBUG", "StreamActivity.onResponse called with " + rs);
        tweetsAdapter.addAll((ArrayList<TwitterApi.Tweet>) rs);
        tweetsAdapter.notifyDataSetChanged();
        tc = new TwitterClient
                (authInfo,
                 new TypeReference<ArrayList<TwitterApi.Tweet>>() {},
                 this);
    }

    public void onFailure(Exception e) {
        Log.d("DEBUG", "StreamActivity.onFailure - " + e);
    }
}
