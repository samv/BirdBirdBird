
package mobi.birdbirdbird;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.skyscreamer.jsonassert.JSONAssert;

import mobi.birdbirdbird.TwitterApi;


public class JsonTests {
    ObjectMapper mapper;

    public static final String FIXT_DIR = "tests/fixtures/";

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        mapper.configure
            (DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void testReadValue() throws IOException {
        TwitterApi.User user = mapper.readValue
            (new File(FIXT_DIR + "user.json"), TwitterApi.User.class);

        assertEquals("119476949", user.id_str);
        assertEquals("OAuth Dancer", user.name);
        assertEquals("oauth_dancer", user.screen_name);
        assertEquals(false, user.default_profile_image);
        assertEquals
            ("https://si0.twimg.com/profile_images/730275945/" +
             "oauth-dancer_normal.jpg", user.profile_image_url_https);
        assertEquals(null, user.utc_offset);
        assertEquals("", user.description);
    }

    @Test
    public void testReadTweets() throws IOException {
        List<TwitterApi.Tweet> tweetList = mapper.readValue
            (new File(FIXT_DIR + "example.json"),
             new TypeReference<List<TwitterApi.Tweet>>(){});

        assertEquals(3, tweetList.size());
        assertEquals("just another test", tweetList.get(0).text);
        assertEquals(0, tweetList.get(0).retweet_count);
        assertEquals(false, tweetList.get(0).retweeted);
        assertEquals(0, tweetList.get(0).favorite_count);
        assertEquals(false, tweetList.get(0).favorited);

        assertEquals(3, tweetList.get(1).retweet_count);
        assertEquals(1, tweetList.get(2).retweet_count);

        assertEquals("Taylor Singletary", tweetList.get(2).user.name);
        assertEquals("episod", tweetList.get(2).user.screen_name);
    }
}
