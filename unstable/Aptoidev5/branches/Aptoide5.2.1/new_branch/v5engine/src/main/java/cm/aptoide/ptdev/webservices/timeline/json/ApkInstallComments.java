package cm.aptoide.ptdev.webservices.timeline.json;

import com.google.api.client.util.Key;

import java.util.List;

import cm.aptoide.ptdev.fragments.GenericResponse;

/**
 * Created by asantos on 24-09-2014.
 */
public class ApkInstallComments extends GenericResponse {

    public List<ApkInstallComments.Comment> getComment() {
        return Comment;
    }

    @Key
    List<Comment> Comment;

    public static class Comment{
        public List<Entry> getEetry() {
            return entry;
        }

        @Key
        List<Entry> entry;

        public static class Entry {

                public Number getId() {
                    return id;
                }

                public String getUsername() {
                    return username;
                }

                public String getText() {
                    return text;
                }

                public String getTimestamp() {
                    return timestamp;
                }

                public boolean isOwned() {
                    return owned;
                }
                @Key
                Number id;
                @Key
                String username;
                @Key
                String text;
                @Key
                String timestamp;
                @Key
                boolean owned;
            }
    }
}