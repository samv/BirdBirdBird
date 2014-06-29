
package mobi.birdbirdbird.activity;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import mobi.birdbirdbird.R;
import mobi.birdbirdbird.typedef.TwitterApi;
import mobi.birdbirdbird.model.TwitterAuthInfo;
import mobi.birdbirdbird.task.TwitterClient;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class TweetActivity extends Activity 
    implements View.OnClickListener,
               TwitterClient.Callbacks
{

    private ImageView imgProfile;
    private TextView tvName;
    private TextView tvScreenName;
    private Button btnTweet;
    private EditText etTweetText;
    private TextView tvCharsLeft;

    // FIXME - base class eg AuthenticatedActivity ?
    private TwitterAuthInfo authInfo;
    private SharedPreferences prefs;

    public void loadAuth() {
        prefs = getSharedPreferences("AuthData", MODE_PRIVATE);
        authInfo = new TwitterAuthInfo(prefs);
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);

        loadAuth();
        connectWidgets();
    }

    public void onResponse(Object rs) {
        Log.d("DEBUG", "TweetActivity onResponse called with " + rs);
        Intent i = new Intent();
        i.putExtra("tweet_id", ((TwitterApi.Tweet) rs).id_str);
        setResult(RESULT_OK, i);
        this.finish();
    }

    public void onFailure(Exception e) {
        Log.d("DEBUG", "TweetActivity failed; " + e);
    }

    public void onClick(View v) {
        TwitterClient tc = new TwitterClient(authInfo, TwitterApi.Tweet.class, this);
        TwitterApi.Tweet status = new TwitterApi.Tweet();
        status.text = etTweetText.getText().toString();
        tc.postTweet(TwitterClient.TWEET_NEW, status);
    }

    public void connectWidgets() {
        imgProfile = (ImageView) findViewById(R.id.imgProfile);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder
            (getApplicationContext())
            .writeDebugLogs()
            .build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
        imageLoader.displayImage
            (authInfo.getUser().profile_image_url_https, imgProfile);

        tvName = (TextView) findViewById(R.id.tvName);
        tvName.setText("@" + authInfo.getUser().name);
        tvScreenName = (TextView) findViewById(R.id.tvScreenName);
        tvScreenName.setText("@" + authInfo.getUser().screen_name);

        btnTweet = (Button) findViewById(R.id.btnTweet);
        btnTweet.setOnClickListener(this);

        etTweetText = (EditText) findViewById(R.id.etTweetText);
        tvCharsLeft = (TextView) findViewById(R.id.tvCharsLeft);
    }
}
