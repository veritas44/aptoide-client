package cm.aptoide.ptdev.model;

/**
 * Created by tdeus on 2/12/14.
 */
public class Video implements MediaObject {

    private String thumb;
    private String url;
    private String type;

    public Video(String thumb, String url){
        this.thumb=thumb;
        this.url=url;
    }

    public String getVideoUrl() { return url; }

    public String getImageUrl() {
        return thumb;
    }


}
