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
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import mobi.birdbirdbird.R;
import mobi.birdbirdbird.adapter.TweetsArrayAdapter;
import mobi.birdbirdbird.typedef.Twitter;
import mobi.birdbirdbird.model.TwitterApi;
import mobi.birdbirdbird.model.TwitterAuthInfo;
import mobi.birdbirdbird.task.RestListCall;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import java.util.ArrayList;
import java.util.List;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Verb;

public class StreamActivity
    extends Activity
    implements TweetsArrayAdapter.ActionCallbacks,
               MenuItem.OnMenuItemClickListener,
               AbsListView.OnScrollListener,
               View.OnCreateContextMenuListener,
               RestListCall.RS<Twitter.Tweet>
{
    private ListView lvTweetStream;
    private TweetsArrayAdapter tweetsAdapter;
    private ArrayList<Twitter.Tweet> tweets;
    private String max_id;
    private ImageLoader imageLoader;
    private TwitterApi twitterApi;
    private AsyncTask getUsersCall;
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

        twitterApi = new TwitterApi(authInfo);

        // init the imageloader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder
            (getApplicationContext())
            .writeDebugLogs()
            .build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);

        // connect tweet stream
        lvTweetStream = (ListView) findViewById(R.id.lvTweetStream);
        tweets = new ArrayList<Twitter.Tweet>();
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
        if (getUsersCall != null) {
            Log.d("DEBUG", "ignoring getNextPage(); client active");
        }
        else {
            int numItems = tweetsAdapter.getCount();
            if (numItems > 0)
                max_id = tweetsAdapter.getItem(numItems - 1).id_str;
            getUsersCall = twitterApi.getTweets
                (TwitterApi.STATUS_HOME, max_id, this);
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
    public void onRestResponse(List<Twitter.Tweet> tweets) {
        getUsersCall = null;
        Log.d("DEBUG", "StreamActivity.onResponse called with " + tweets.size() + " tweets");
        for (Twitter.Tweet tweet: tweets) {
            tweetsAdapter.add(tweet);
        }
        tweetsAdapter.notifyDataSetChanged();
    }

    public void onRestFailure(Exception e) {
        getUsersCall = null;
        Toast.makeText
            (this, "Error during API call: " + e.toString(),
             Toast.LENGTH_LONG).show();
        Log.d("DEBUG", "StreamActivity.onRestFailure - " + e);
    }
}
