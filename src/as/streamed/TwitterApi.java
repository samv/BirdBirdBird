
package as.streamed;

import java.util.Date;

public class TwitterApi {
    public TwitterApi() {
    }

    public static class User {
        public User() {}
        public String id_str;
        public String name;
        public String screen_name;

        public boolean default_profile_image;
        public String profile_image_url_https;

        public String description;
        public Integer utc_offset;
    }

    public static class Tweet {
        public Tweet() {}

        public String id_str;
        public Date created_at;
        public String text;
        public User user;

        public int favorite_count = 0;
        public boolean favorited;
        public int retweet_count = 0;
        public boolean retweeted;

        public String source;
        // todo: entities for hashtag etc highlighting
    }
}
