
package mobi.birdbirdbird.activity;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import java.util.Date;
import mobi.birdbirdbird.R;
import mobi.birdbirdbird.model.TwitterApi;
import mobi.birdbirdbird.model.TwitterAuthInfo;
import mobi.birdbirdbird.task.RestCall;
import mobi.birdbirdbird.typedef.Twitter;

public class ProfileActivity
    extends Activity 
    implements StreamFragment.Callbacks,
               RestCall.RS<Twitter.User>
{
    // might/should probably move some of the commonality with this
    // class vs TweetActivity into either a base class or a fragment.
    // in the meantime, duplicate code is fine.
    private LinearLayout llProfile; 
    private ImageView imgProfile;
    private ImageView imgBanner;
    private TextView tvName;
    private TextView tvScreenName;
    private TextView tvTagLine;
    private TextView tvLocation;

    private TextView tvStatusesCount;
    private TextView tvFriendsCount;
    private TextView tvFollowersCount;
    private TextView tvFavouritesCount;

    private StreamFragment profileTweets;

    // FIXME - base class eg AuthenticatedActivity ?
    private TwitterAuthInfo authInfo;
    private TwitterApi twitterApi;
    private Twitter.User user;
    private SharedPreferences prefs;
    private ImageLoader imageLoader;
    private long time; 

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS); 
        setContentView(R.layout.activity_profile);

        time = (new Date()).getTime();
        findWidgets();

        user = (Twitter.User)
            getIntent().getSerializableExtra("user");
        setUser(user);

        createProfileTweetsFragment();

        setProgressBarIndeterminateVisibility(true); 
        getTwitterApi().getUser
            (TwitterApi.USERS_SHOW, user.id_str, null, this);
    }

    void createProfileTweetsFragment() {
        profileTweets = new StreamFragment(user, this);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.flStream, profileTweets);
        ft.commit();
        profileTweets.getTweets();
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

    public void findWidgets() {
        llProfile = (LinearLayout) findViewById(R.id.llProfile); 
        tvName = (TextView) findViewById(R.id.tvProfileName);
        tvScreenName = (TextView) findViewById(R.id.tvProfileScreenName);
        imgBanner = (ImageView) findViewById(R.id.imgBanner);
        imgProfile = (ImageView) findViewById(R.id.imgProfilePicture);
        tvTagLine = (TextView) findViewById(R.id.tvProfileTagLine);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvStatusesCount = (TextView) findViewById(R.id.tvStatusesCount);
        tvFriendsCount = (TextView) findViewById(R.id.tvFriendsCount);
        tvFollowersCount = (TextView) findViewById(R.id.tvFollowersCount);
        tvFavouritesCount = (TextView) findViewById(R.id.tvFavouritesCount);
    }

    public void onRestResponse(Twitter.User user) {
        setProgressBarIndeterminateVisibility(false); 
        this.user = user;
        setUser(user);
    }

    public void onRestFailure(Exception e) {
        setProgressBarIndeterminateVisibility(false); 
        Toast.makeText
            (this, "Error during API call: " + e.toString(),
             Toast.LENGTH_LONG).show();
        Log.d("DEBUG", "ProfileActivity.onRestFailure - " + e);
    }

    private void setUser(Twitter.User user) {
        tvName.setText(user.name);
        tvScreenName.setText("@" + user.screen_name);// +
                             //" (joined: " + user.getWhen(time) + ")");
        if (user.default_profile_image) {
            imgProfile.setImageResource(R.drawable.profile_default);
        }
        else {
            imgProfile.setImageResource(R.drawable.profile_none);
            getImageLoader().displayImage
                (user.profile_image_url_https, imgProfile);
        }
        if (user.description != null)
            tvTagLine.setText(user.description);
        if (user.location != null)
            tvLocation.setText(user.location);

        tvStatusesCount.setText(user.statuses_count + "");
        tvFriendsCount.setText(user.friends_count + "");
        tvFollowersCount.setText(user.followers_count + "");
        tvFavouritesCount.setText(user.favourites_count + "");

        imgBanner.setBackgroundColor(user.profileColor());
        if (user.hasBanner()) {
            getImageLoader().displayImage
                (user.profile_banner_url, imgBanner);
        }
    }

    public void onProfileClick(Twitter.User user) {
        if (this.user.id_str.equals(user.id_str)) {
            Toast.makeText
                (this, "I'm sorry, Dave. I'm afraid I can't let you do that.",
                 Toast.LENGTH_SHORT).show();
        }
        else {
            Log.d("DEBUG", "Selected profile user " + this.user.id_str + " != " + user.id_str + ", so let's go!");
            Intent i = new Intent(this, ProfileActivity.class);
            i.putExtra("user", user);
            startActivity(i);
        }
    }

    public void loadAuth() {
        prefs = getSharedPreferences("AuthData", MODE_PRIVATE);
        authInfo = new TwitterAuthInfo(prefs);
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
}
