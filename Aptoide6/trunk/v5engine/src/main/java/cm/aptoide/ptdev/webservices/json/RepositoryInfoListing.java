package cm.aptoide.ptdev.webservices.json;



/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 19-11-2013
 * Time: 13:18
 * To change this template use File | Settings | File Templates.
 */
public class RepositoryInfoListing{

    public String avatar_hd;

    public String avatar;
     public String description;
     public String downloads;
     public String items;
     public String name;
     public String theme;
     public String view;

    public String getAvatar_hd(){
        return this.avatar_hd;
    }
    public void setAvatar_hd(String avatar_hd){
        this.avatar_hd = avatar_hd;
    }
    public String getDescription(){
        return this.description;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public String getDownloads(){
        return this.downloads;
    }
    public void setDownloads(String downloads){
        this.downloads = downloads;
    }
    public String getItems(){
        return this.items;
    }
    public void setItems(String items){
        this.items = items;
    }
    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getTheme(){
        return this.theme;
    }
    public void setTheme(String theme){
        this.theme = theme;
    }
    public String getView(){
        return this.view;
    }
    public void setView(String view){
        this.view = view;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
