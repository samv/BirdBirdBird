
package as.streamed;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.skyscreamer.jsonassert.JSONAssert;

import as.streamed.TwitterApi;


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
}
