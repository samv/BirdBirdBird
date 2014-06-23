
package as.streamed;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import as.streamed.TwitterApi;

public class TweetsArrayAdapter
    extends ArrayAdapter<TwitterApi.Tweet>
{
    public interface ActionCallbacks {
        //abstract void reply(Tweet tweet);
        //abstract boolean toggleRetweet(Tweet tweet);
        //abstract boolean toggleFavorite(Tweet tweet);
        //abstract boolean toggleFollow(Tweet tweet);
        abstract void setImage(Uri uri, ImageView v);
    }

    private ActionCallbacks cb;

    public TweetsArrayAdapter(Context context,
                              ArrayList<TwitterApi.Tweet> tweets,
                              ActionCallbacks acb)
    {
        super(context, 0, tweets);
        this.cb = acb;
        time = (new Date()).getTime();
    }

    private HashMap<String, View> activeViews;
    private HashMap<Integer, String> viewIds;
    public long time;

    @Override
    public View getView(int position, View recycleView, ViewGroup parent) {
        // Get the data item for this position
        TwitterApi.Tweet tweet = getItem(position);    
        TwitterApi.User user = tweet.user;

        View view;
        Integer viewId;
        if (recycleView == null) {
            view = LayoutInflater.from(getContext())
                .inflate(R.layout.view_tweet, parent, false);
            viewId = new Integer(view.getId());
        }
        else {
            viewId = new Integer(recycleView.getId());
            activeViews.remove(viewIds.get(viewId));
            view = recycleView;
        }
        viewIds.put(viewId, tweet.id_str);
        activeViews.put(tweet.id_str, view);

        maybeSetText(view.findViewById(R.id.tvName), user.name);
        maybeSetText(view.findViewById(R.id.tvScreenName), user.screen_name);
        maybeSetText(view.findViewById(R.id.tvTweetText), tweet.text);

        setMutable(view, tweet);

        ImageView imgProfile = (ImageView) view.findViewById(R.id.imgProfile);
        if (user.default_profile_image) {
            imgProfile.setImageResource(R.drawable.profile_default);
        }
        else {
            cb.setImage(Uri.parse(user.profile_image_url_https), imgProfile);
        }

        return view;
    }

    private void maybeSetText(View textView, String text) {
        TextView tv = (TextView) textView;
        if (tv.getText().toString() != text)
            tv.setText(text);
    }

    private void setMutable(View view, TwitterApi.Tweet tweet) {
        maybeSetText
            (view.findViewById(R.id.tvTweetAge), tweet.getAge(time));
        maybeSetText
            (view.findViewById(R.id.tvRetweetCount), tweet.retweet_count + "");
        maybeSetText
            (view.findViewById(R.id.tvFavoriteCount),
             tweet.favorite_count + "");

        ((ImageView) view.findViewById(R.id.btnRetweet)).setImageResource
            (tweet.retweeted
             ? R.drawable.retweeted_tiny
             : R.drawable.retweet_tiny);
        ((ImageView) view.findViewById(R.id.btnFavorite)).setImageResource
            (tweet.favorited
             ? R.drawable.favorited_tiny
             : R.drawable.favorite_tiny);
    }
}
