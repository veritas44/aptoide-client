package cm.aptoide.ptdev.pushnotification;



import java.util.List;

import cm.aptoide.ptdev.webservices.json.GenericResponseV2;

/**
 * Created by rmateus on 30-09-2014.
 */
public class PushNotificationJson extends GenericResponseV2 {

    
    List<Notification> results;

    public List<Notification> getResults() {
        return results;
    }

    public static class Notification {
        
        Number id;
        
        String title;

        
        String message;

        
        String target_url;
        
        String track_url;

         Images images;

        public String getMessage() {
            return message;
        }

        public Images getImages() {
            return images;
        }

        public Number getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getTarget_url() {
            return target_url;
        }

        public String getTrack_url() {
            return track_url;
        }

        public static class Images {
             String banner_url;
             String icon_url;


            public String getBanner_url() {
                return banner_url;
            }

            public String getIcon_url() {
                return icon_url;
            }
        }
    }
}
