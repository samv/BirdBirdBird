package mobi.birdbirdbird.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.ImageLoader;
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

public class StreamFragment
    extends Fragment
    implements TweetsArrayAdapter.ActionCallbacks,
               AbsListView.OnScrollListener,
               RestListCall.RS<Twitter.Tweet>
{
    private ListView lvTweetStream;
    private TweetsArrayAdapter tweetsAdapter;
    private ArrayList<Twitter.Tweet> tweets = null;
    private String max_id;
    private ImageLoader imageLoader;
    private TwitterApi twitterApi;
    private Activity activity;
    private String endpoint;

    public StreamFragment(TwitterApi twitterApi, ImageLoader imageLoader, String endpoint) {
        this.imageLoader = imageLoader;
        this.twitterApi = twitterApi;
        this.endpoint = endpoint;
        Log.d("DEBUG", "NEW: Stream Fragment " + endpoint);
    }

    @Override
    public void onAttach(Activity activity)
    {
        Log.d("DEBUG", "ATTACH: Stream Fragment " + endpoint);
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
        Log.d("DEBUG", "CREATE: Stream Fragment " + endpoint);
		super.onCreate(savedInstanceState);
        if (tweets == null)
            tweets = new ArrayList<Twitter.Tweet>();
        tweetsAdapter = new TweetsArrayAdapter(activity, tweets, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Log.d("DEBUG", "CREATE VIEW: Stream Fragment " + endpoint);
        View v = inflater.inflate(R.layout.fragment_stream, container, false);
        // connect tweet stream
        lvTweetStream = (ListView) v.findViewById(R.id.lvTweetStream);
        lvTweetStream.setAdapter(tweetsAdapter);
        lvTweetStream.setOnScrollListener(this);
        return v;
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
            getTweets();
        }
        else {
            //Log.d("DEBUG", "We already have " + tweetsAdapter.getCount() +
            //      " items");
        }
    }

    public void	onScrollStateChanged(AbsListView view, int scrollState) {
        //Log.d("DEBUG", "Scrolling!  state = " + scrollState);
    }

    private AsyncTask getNewTweetsCall;
    private AsyncTask getTweetsCall;

    private boolean wantNew = false;
    private boolean atEnd = false;

    public void getTweets() {
        if (tweetsAdapter == null) {
            //Log.d("DEBUG", "not ready; not getting anything yet");
        }
        else if ((getTweetsCall != null) ||
                 (getNewTweetsCall != null)) {
            Log.d("DEBUG", "ignoring getTweets(); client active");
        }
        else if (!wantNew && atEnd) {
            Log.d("DEBUG", "reached end of results!");
        }
        else {
            if (wantNew) {
                String since_id = null;
                if (tweetsAdapter.getCount() > 0)
                    since_id = tweetsAdapter.getItem(0).id_str;
                Log.d("DEBUG", "fetching tweets since " + since_id);
                getNewTweetsCall = twitterApi.getTweets
                    (endpoint, null, since_id, this);
                wantNew = false;
            }
            else {
                int numItems = tweetsAdapter.getCount();
                if ((max_id == null) && (numItems > 0))
                    max_id = tweetsAdapter.getItem(numItems - 1).id_str;
                Log.d("DEBUG", "fetching tweets before " + max_id);
                getTweetsCall = twitterApi.getTweets
                    (endpoint, max_id, null, this);
            }
        }
    }

    public void getNewTweets() {
        wantNew = true;
        getTweets();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d("DEBUG", "RESUME: Stream Fragment " + endpoint);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("DEBUG", "PAUSE: Stream Fragment " + endpoint);
        wantNew = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("DEBUG", "DESTROY VIEW: Stream Fragment " + endpoint);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tweetsAdapter = null;
        Log.d("DEBUG", "DESTROY: Stream Fragment " + endpoint);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getTweetsCall != null) {
            getTweetsCall.cancel(true);
            getTweetsCall = null;
        }
        if (getNewTweetsCall != null) {
            getNewTweetsCall.cancel(true);
            getNewTweetsCall = null;
        }
        Log.d("DEBUG", "STOP: Stream Fragment " + endpoint);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("DEBUG", "DETACH: Stream Fragment " + endpoint);
        this.activity = null;
    }

    // TwitterClient.Callbacks
    public void onRestResponse(List<Twitter.Tweet> tweets) {
        boolean scrollUp = false;
        if (getTweetsCall != null) {
            if (tweets.size() < TwitterApi.MIN_TWEETS_EXPECTED)
                atEnd = true;
            getTweetsCall = null;
            if (wantNew)
                getTweets();
        }
        else {
            getNewTweetsCall = null;
            scrollUp = true;
        }
        Log.d("DEBUG", "StreamFragment.onRestResponse called with " +
              tweets.size() + " tweets");
        tweetsAdapter.merge(tweets);
        tweetsAdapter.notifyDataSetChanged();
        if (scrollUp)
            lvTweetStream.smoothScrollToPosition(0);
    }

    public void onRestFailure(Exception e) {
        getTweetsCall = null;
        Toast.makeText
            (this.activity, "Error during API call: " + e.toString(),
             Toast.LENGTH_LONG).show();
        Log.d("DEBUG", "StreamFragment.onRestFailure - " + e);
    }
}
