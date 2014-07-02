
package mobi.birdbirdbird.activity;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import mobi.birdbirdbird.R;
import mobi.birdbirdbird.model.TwitterApi;
import mobi.birdbirdbird.model.TwitterAuthInfo;
import mobi.birdbirdbird.task.RestCall;
import mobi.birdbirdbird.typedef.Twitter;

public class TweetActivity extends Activity 
    implements View.OnClickListener,
               RestCall.RS<Twitter.Tweet>
{

    private ImageView imgProfile;
    private TextView tvName;
    private TextView tvScreenName;
    private Button btnTweet;
    private EditText etTweetText;
    private TextView tvCharsLeft;
    private TwitterApi twitterApi;

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
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS); 
        setContentView(R.layout.activity_tweet);

        loadAuth();
        connectWidgets();
    }

    public void onRestResponse(Twitter.Tweet rs) {
        Log.d("DEBUG", "TweetActivity onResponse called with " + rs);
        Intent i = new Intent();
        i.putExtra("tweet_id", rs.id_str);
        setResult(RESULT_OK, i);
        setProgressBarIndeterminateVisibility(false); 
        this.finish();
    }

    public void onRestFailure(Exception e) {
        setProgressBarIndeterminateVisibility(false); 
        Toast.makeText
            (this, "Error during API call: " + e.toString(),
             Toast.LENGTH_LONG).show();
    }

    public void onClick(View v) {
        twitterApi = new TwitterApi(authInfo);
        Twitter.Tweet status = new Twitter.Tweet();
        status.text = etTweetText.getText().toString();
        setProgressBarIndeterminateVisibility(true); 
        twitterApi.postTweet(TwitterApi.STATUS_UPDATE, status, this);
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
        tvName.setText(authInfo.getUser().name);
        tvScreenName = (TextView) findViewById(R.id.tvScreenName);
        tvScreenName.setText("@" + authInfo.getUser().screen_name);

        btnTweet = (Button) findViewById(R.id.btnTweet);
        btnTweet.setOnClickListener(this);

        etTweetText = (EditText) findViewById(R.id.etTweetText);
        tvCharsLeft = (TextView) findViewById(R.id.tvCharsLeft);
    }
}
