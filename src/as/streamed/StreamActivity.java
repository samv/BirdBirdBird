package as.streamed;

import java.util.ArrayList;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Verb;

import as.streamed.TweetsArrayAdapter;
import as.streamed.TwitterApi;
import as.streamed.TwitterAuthInfo;
import as.streamed.TwitterClient;

public class StreamActivity
    extends Activity
    implements TweetsArrayAdapter.ActionCallbacks,
               AbsListView.OnScrollListener,
               TwitterClient.Callbacks
{
    private ListView lvTweetStream;
    private TweetsArrayAdapter tweetsAdapter;
    private ArrayList<TwitterApi.Tweet> tweets;
    private String max_id;
    private ImageLoader imageLoader;
    private TwitterClient tc;
    private TwitterAuthInfo authInfo;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

        authInfo = (TwitterAuthInfo)
            getIntent().getSerializableExtra("authInfo");

        Log.d("DEBUG", "Stream.onCreate: authInfo = " + authInfo);

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
    }

    // TweetsArrayAdapter.ActionCallbacks
    public void setImage(Uri uri, ImageView v) {
        imageLoader.displayImage(uri.toString(), v);
    }

    // AbsListView.OnScrollListener callbacks
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount)
    {
        Log.d("DEBUG", "onScroll(" + view + ", " + firstVisibleItem +
              ", " + visibleItemCount + ", " + totalItemCount + ")");
        if (tweetsAdapter == null)
            return;
        if (firstVisibleItem + visibleItemCount == tweetsAdapter.getCount()) {
            Log.d("DEBUG", "let's get some more");
            getNextPage();
        }
        else {
            Log.d("DEBUG", "We already have " + tweetsAdapter.getCount() +
                  " items");
        }
    }

    public void	onScrollStateChanged(AbsListView view, int scrollState) {
        Log.d("DEBUG", "Scrolling!  state = " + scrollState);
    }

    private void getNextPage() {
        if (tc.getStatus() != AsyncTask.Status.PENDING) {
            Log.d("DEBUG", "ignoring getNextPage(); client not ready");
        }
        else {
            int numItems = tweetsAdapter.getCount();
            if (numItems > 0)
                max_id = tweetsAdapter.getItem(numItems - 1).id_str;
            tc.getTweets(TwitterClient.HOME_TIMELINE, max_id);
        }
    }

    // TwitterClient.Callbacks
    public void onResponse(Object rs) {
        Log.d("DEBUG", "onResponse called with " + rs);
        tweetsAdapter.addAll((ArrayList<TwitterApi.Tweet>) rs);
        tweetsAdapter.notifyDataSetChanged();
        tc = new TwitterClient
                (authInfo,
                 new TypeReference<ArrayList<TwitterApi.Tweet>>() {},
                 this);
    }

    public void onFailure(Exception e) {
        Log.d("DEBUG", "onFailure - " + e);
    }

    
}
