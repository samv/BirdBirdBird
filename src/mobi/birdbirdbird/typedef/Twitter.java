
package mobi.birdbirdbird.typedef;

import android.graphics.Color;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import mobi.birdbirdbird.R;

public class Twitter implements Serializable {
    private static String convertAge(Date time, long sinceTime) {
        long ago = sinceTime - time.getTime();
        ago = ago / 1000;
        // FIXME: localization!
        if (ago < -600) {
            return "the future";
        }
        else if (ago < 1) {
            return "just now";
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
    
    public static class User implements Serializable {
        public User() {}
        public String id_str;
        public String name;
        public String screen_name;

        public boolean default_profile_image;
        public String profile_image_url_https;

        public String description;
        public String location;

        public int favourites_count;
        public int followers_count;
        public int friends_count;
        public int statuses_count;
        @JsonFormat(shape=JsonFormat.Shape.STRING,
                    pattern="EEE MMM dd HH:mm:ss zzz yyyy")
        public Date created_at;
        public String getWhen(long sinceTime) {
            return convertAge(created_at, sinceTime);
        }

        // banner
        public String profile_banner_url;
        public boolean hasBanner() {
            return ((profile_banner_url != null) &&
                    (profile_banner_url.length() > 5));
        }

        // background stuff
        public String profile_background_color;

        public int profileColor() {
            int color;
            if (profile_background_color != null) {
                try {
                    color = Color.parseColor("#" + profile_background_color);
                }
                catch (IllegalArgumentException iae) {
                    color = R.color.profile_default;
                }
            }
            else {
                color = R.color.profile_default;
            }
            return color;
        }
        public String profile_background_image_url_https;
        public boolean profile_background_tile;
        public boolean profile_use_background_image;

        // badges
        public boolean verified;
        public String withheld_in_countries;
        public String withheld_scope;

        public Tweet status;

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

        @JsonIgnore
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
            return convertAge(created_at, sinceTime);
        }

        public String source;
        // todo: entities for hashtag etc highlighting
    }
}
