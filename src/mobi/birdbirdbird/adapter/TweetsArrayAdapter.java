
package mobi.birdbirdbird.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import mobi.birdbirdbird.R;
import mobi.birdbirdbird.typedef.Twitter;

public class TweetsArrayAdapter
    extends ArrayAdapter<Twitter.Tweet>
    implements View.OnClickListener
{
    public interface ActionCallbacks {
        //abstract void reply(Tweet tweet);
        //abstract boolean toggleRetweet(Tweet tweet);
        //abstract boolean toggleFavorite(Tweet tweet);
        //abstract boolean toggleFollow(Tweet tweet);
        abstract void setImage(Uri uri, ImageView v);
        abstract void onProfileClick(Twitter.User user);
    }

    private ActionCallbacks cb;

    public TweetsArrayAdapter(Context context,
                              ArrayList<Twitter.Tweet> tweets,
                              ActionCallbacks acb)
    {
        super(context, 0, tweets);
        this.cb = acb;
        time = (new Date()).getTime();
    }

    public long time;

    @Override
    public View getView(int position, View recycleView, ViewGroup parent) {
        // Get the data item for this position
        Twitter.Tweet tweet = this.getItem(position);
        Twitter.User user = tweet.user;

        View view;
        Integer viewId;
        if (recycleView == null) {
            view = LayoutInflater.from(getContext())
                .inflate(R.layout.view_tweet, parent, false);
        }
        else {
            view = recycleView;
        }
        view.setTag(new Integer(position));
        Log.d("DEBUG", "View " + view + " now holds item " + position +
              ", tweet " + tweet.id_str);

        maybeSetText(view.findViewById(R.id.tvName), user.name);
        maybeSetText(view.findViewById(R.id.tvScreenName),
                     "@" + user.screen_name);
        maybeSetText(view.findViewById(R.id.tvTweetText), tweet.text);
        setMutable(view, tweet);

        ImageView imgProfile = (ImageView) view.findViewById(R.id.imgProfile);
        if (user.default_profile_image) {
            imgProfile.setImageResource(R.drawable.profile_default);
        }
        else {
            imgProfile.setImageResource(R.drawable.profile_none);
            cb.setImage(Uri.parse(user.profile_image_url_https), imgProfile);
        }
        imgProfile.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        Log.d("DEBUG", "Received click to " + v);
        switch (v.getId()) {
        case R.id.imgProfile:
            View row = (View) v.getParent();
            cb.onProfileClick(getItem((Integer) row.getTag()).user);
            break;
        case View.NO_ID:
            Log.d("DEBUG", "Source view has no ID");
            break;
        default:
            Log.d("DEBUG", "Source view has ID " + v.getId());
            break;
        }
    }

    private void maybeSetText(View textView, String text) {
        TextView tv = (TextView) textView;
        if ((tv != null) && (tv.getText() != null) &&
                tv.getText().toString() != text)
            tv.setText(text);
    }

    private void setMutable(View view, Twitter.Tweet tweet) {
        maybeSetText
            (view.findViewById(R.id.tvTweetAge), tweet.getAge(time));
        maybeSetText
            (view.findViewById(R.id.tvRetweetCount), tweet.retweet_count + "");
        maybeSetText
            (view.findViewById(R.id.tvFavoriteCount),
             tweet.favorite_count + "");

        ((ImageView) view.findViewById(R.id.imgRetweet)).setImageResource
            (tweet.retweeted
             ? R.drawable.retweeted_tiny
             : R.drawable.retweet_tiny);
        ((ImageView) view.findViewById(R.id.imgFavorite)).setImageResource
            (tweet.favorited
             ? R.drawable.favorited_tiny
             : R.drawable.favorite_tiny);
    }

    public int insertIndex(Twitter.Tweet tweet) {
        //Log.d("DEBUG", "where should I insert tweet " + tweet.id_str);
        int min = 0;
        int max = getCount() - 1;
        while (min != max) {
            int middle = (min + max) / 2;
            Twitter.Tweet midTweet = getItem(middle);
            if (tweet.isAfter(midTweet))
                max = middle;
            else if (tweet.isBefore(midTweet)) {
                if (min == middle)
                    min += 1;
                else
                    min = middle;
            }
            else {
                min = middle;
                max = middle;
            }
        }
        //Log.d("DEBUG", "at position " + min + ", before " + getItem(min).id_str);
        return min;
    }

    public void merge(List<Twitter.Tweet> tweets) {
        if (tweets.size() == 0)
            return;
        int size = getCount();
        int oldest = size - 1;
        if ((size == 0) || (getItem(oldest).isAfter(tweets.get(0)))) {
            //Log.d("DEBUG", "adding tweets to end of list");
            this.addAll(tweets);
        }
        else {
            int newest = insertIndex(tweets.get(0));
            Twitter.Tweet lastTweet = null;
            for (Twitter.Tweet t: tweets) {
                this.insert(t, newest);
                lastTweet = t;
                newest += 1;
            }
            while (newest < getCount() &&
                   !lastTweet.isAfter(getItem(newest))) {
                //Log.d("DEBUG", "removing an obsolete tweet from position " + newest);
                this.remove(getItem(newest));
            }
        }
    }
}
