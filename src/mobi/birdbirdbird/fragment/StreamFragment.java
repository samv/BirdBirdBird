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
    private ArrayList<Twitter.Tweet> tweets;
    private String max_id;
    private ImageLoader imageLoader;
    private TwitterApi twitterApi;
    private Activity activity;

    public StreamFragment(TwitterApi twitterApi, ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
        this.twitterApi = twitterApi;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        tweets = new ArrayList<Twitter.Tweet>();
        tweetsAdapter = new TweetsArrayAdapter(activity, tweets, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Log.d("DEBUG", "StreamFragment.onCreateView()");
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
            //Log.d("DEBUG", "let's get some more items");
            getNextPage();
        }
        else {
            //Log.d("DEBUG", "We already have " + tweetsAdapter.getCount() +
            //      " items");
        }
    }

    public void	onScrollStateChanged(AbsListView view, int scrollState) {
        //Log.d("DEBUG", "Scrolling!  state = " + scrollState);
    }

    private AsyncTask getTweetsCall;

    private String maxId;

    public void getNextPage() {
        if (tweetsAdapter == null) {
            //Log.d("DEBUG", "not ready; not getting anything yet");
        }
        else if (getTweetsCall != null) {
            //Log.d("DEBUG", "ignoring getNextPage(); client active");
        }
        else {
            int numItems = tweetsAdapter.getCount();
            String max_id = maxId;
            if ((max_id == null) && (numItems > 0))
                max_id = tweetsAdapter.getItem(numItems - 1).id_str;
            getTweetsCall = twitterApi.getTweets
                (TwitterApi.STATUS_HOME, max_id, this);
            maxId = null;
        }
    }
    public void setNextPage(String maxId) {
        this.maxId = maxId;
    }

    // TwitterClient.Callbacks
    public void onRestResponse(List<Twitter.Tweet> tweets) {
        getTweetsCall = null;
        Log.d("DEBUG", "StreamFragment.onRestResponse called with " +
              tweets.size() + " tweets");
        tweetsAdapter.merge(tweets);
        tweetsAdapter.notifyDataSetChanged();
    }

    public void onRestFailure(Exception e) {
        getTweetsCall = null;
        Toast.makeText
            (this.activity, "Error during API call: " + e.toString(),
             Toast.LENGTH_LONG).show();
        Log.d("DEBUG", "StreamFragment.onRestFailure - " + e);
    }
}
