package cm.aptoide.ptdev.webservices.timeline.json;

import com.google.api.client.util.Key;

/**
 * Created by asantos on 06-11-2014.
 */
public class Friend {
    public String getUsername() {
        return username;
    }
    public String getAvatar() {
        return avatar;
    }
    public String getEmail() {
        return email;
    }
    @Key
    String username;
    @Key
    String avatar;
    @Key
    String email;
}
