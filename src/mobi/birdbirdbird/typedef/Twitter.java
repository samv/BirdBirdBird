
package mobi.birdbirdbird.typedef;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;

public class Twitter implements Serializable {
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

    public static class Tweet implements Serializable {
        public Tweet() { }

        public boolean isAfter(Tweet other) {
            return this.getId() > other.getId();
        }
        public boolean isBefore(Tweet other) {
            return this.getId() < other.getId();
        }

        public long getId() {
            return Long.parseLong(id_str);
        }
        public String id_str;
        @JsonFormat(shape=JsonFormat.Shape.STRING,
                    pattern="EEE MMM dd HH:mm:ss zzz yyyy")
        public Date created_at;
        public String text;
        public User user;

        public int favorite_count;
        public boolean favorited;

        public int retweet_count;
        public boolean retweeted;

        public String getAge(long sinceTime) {
            long ago = sinceTime - created_at.getTime();
            ago = ago / 1000;
            if (ago < 0) {
                return "the future";
            }
            else if (ago < 120) {
                return ago + "s";
            }
            else if (ago < (90 * 60)) {
                return (ago / 60) + "m";
            }
            else if (ago < (72 * 3600)) {
                return (ago / 3600) + "h";
            }
            else {
                return (ago / 86400) + "d";
            }
            // todo: date formatting!
        }

        public String source;
        // todo: entities for hashtag etc highlighting
    }
}
