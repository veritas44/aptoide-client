package cm.aptoide.pt.webservices;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import cm.aptoide.pt.ApplicationAptoide;
import cm.aptoide.pt.Category;
import cm.aptoide.pt.Database;
import cm.aptoide.pt.ExtrasContentProvider;
import cm.aptoide.pt.configuration.AptoideConfiguration;
import cm.aptoide.pt.util.NetworkUtils;
import cm.aptoide.pt.util.Utils;
import cm.aptoide.pt.views.ViewApk;
import cm.aptoide.pt.webservices.comments.Comment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 11-03-2013
 * Time: 14:14
 * To change this template use File | Settings | File Templates.
 */
public class WebserviceGetApkInfo {

    private final Context context;
    JSONObject response;


    //For reference: getApkInfo/<repo>/<apkid>/<apkversion>/options=(<options>)/<mode>;
    String defaultWebservice = AptoideConfiguration.getInstance().getWebServicesUri();
    private ArrayList<Comment> comments;
    private boolean seeAll = false;
    private boolean screenshotChanged;



    public WebserviceGetApkInfo(Context context, String webservice, ViewApk apk, Category category, String token, boolean cacheData) throws IOException, JSONException {
        this.context = context;

        StringBuilder url = new StringBuilder();

        if(webservice==null){
            url.append(defaultWebservice);
        }else{
            url.append(webservice);
        }

        ArrayList<WebserviceOptions> options = new ArrayList<WebserviceOptions>();

        if(token!=null)options.add(new WebserviceOptions("token", token));
        options.add(new WebserviceOptions("cmtlimit", "6"));
        options.add(new WebserviceOptions("vercode", String.valueOf(apk.getVercode())));

        options.add(new WebserviceOptions("payinfo", "true"));
        options.add(new WebserviceOptions("q", Utils.filters(context)));
        options.add(new WebserviceOptions("lang", Utils.getMyCountryCode(ApplicationAptoide.getContext())));




        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for(WebserviceOptions option: options){
            sb.append(option);
            sb.append(";");
        }
        sb.append(")");

        url.append("webservices/getApkInfo/").append(apk.getRepoName()).append("/")
                .append(apk.getApkid()).append("/")
                .append(URLEncoder.encode(apk.getVername(),"UTF-8")).append("/")
                .append("options=")
                .append(sb.toString()).append("/")
                .append("json");

        NetworkUtils utils = new NetworkUtils();
        String line;

        BufferedReader br = new BufferedReader(new InputStreamReader(utils.getInputStream(url.toString(), null, null, ApplicationAptoide.getContext())));
        sb = new StringBuilder();
        while ((line = br.readLine()) != null){
            sb.append(line).append('\n');
        }

        br.close();

        Log.e("REQUEST",url.toString());
        Log.e("RESPONSE",sb.toString());
        response = new JSONObject(sb.toString());

        if (cacheData) {
            try {
                comments = getComments();
                Database database = Database.getInstance();

                Log.d("WebserviceGetApkInfo", comments.size() + "");


                if (category.equals(Category.INFOXML)) {
                    JSONArray screenshots = getScreenshots();


                    ArrayList<String> loadedScreenshots = apk.getScreenshots();


                    for (int i = 0; i != screenshots.length(); i++) {
                        if (!loadedScreenshots.contains(screenshots.getString(i))) {
                            apk.getScreenshots().clear();
                            for (int j = 0; j != screenshots.length(); j++) {
                                apk.getScreenshots().add(screenshots.getString(j));
                            }
                            Database.getInstance().insertScreenshots(apk, category);
                            screenshotChanged = true;
                            break;
                        }
                    }

                    Log.d("WebserviceGetApkInfo", loadedScreenshots.toString());
                    Log.d("WebserviceGetApkInfo", screenshots.toString());


                }

                ContentValues values = new ContentValues();
                values.put("apkid", apk.getApkid());
                values.put("comment", getDescription());
                context.getContentResolver().insert(ExtrasContentProvider.CONTENT_URI, values);

                database.deleteCommentsCache(apk.getId(), category);
                for (Comment comment : comments) {
                    database.insertComment(apk.getId(), comment, category);
                }
                database.insertLikes(getLikes(), category, apk.getId());
                database.insertMalwareInfo(getMalwareInfo(), category, apk.getId());
                if (hasOBB()) {
                    JSONObject mainObb = getMainOBB();
                    database.insertMainObbInfo(mainObb.getString("path"), mainObb.getString("md5sum"), mainObb.getString("filesize"), mainObb.getString("filename"), apk.getId(), category);

                    if (hasPatchOBB()) {
                        JSONObject patchObb = getPatchOBB();
                        database.insertPatchObbInfo(patchObb.getString("path"), patchObb.getString("md5sum"), patchObb.getString("filesize"), patchObb.getString("filename"), apk.getId(), category);
                    }

                }

                database.insertDownloadPath(getApkDownloadPath(), category, apk.getId());
                database.insertApkMd5(getApkMd5(), category, apk.getId());
                database.insertApkSize(getApkSize(), category, apk.getId());


            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


    }

    public boolean isSeeAll(){
        return seeAll;

    }

    public MalwareStatus getMalwareInfo() throws JSONException {
        JSONObject malwareResponse = response.getJSONObject("malware");
        return new MalwareStatus(malwareResponse.getString("status"), malwareResponse.getString("reason"));
    }

    public String getApkMd5() throws JSONException {
        return response.getJSONObject("apk").getString("md5sum");
    }

    public String getApkDownloadPath() throws JSONException {
        return response.getJSONObject("apk").getString("path");
    }

    public String getApkSize() throws JSONException {
        return response.getJSONObject("apk").getString("size");
    }

    public String getName() throws JSONException {
        JSONObject malwareResponse = response.getJSONObject("meta");
        return malwareResponse.getString("title");
    }

    public String getDescription() throws JSONException {
        JSONObject malwareResponse = response.getJSONObject("meta");
        return malwareResponse.getString("description");
    }

    public JSONArray getApkPermissions() throws JSONException {

        JSONArray permissionArray = response.getJSONArray("permissions");

        return permissionArray;

    }

    public String getLatestVersionURL() throws JSONException {
        return response.getString("latest");

    }

    public boolean hasLatestVersion() {
        return response.has("latest");
    }

    public boolean hasOBB() {
        return response.has("obb");
    }


    public ArrayList<Comment> getComments() throws JSONException, ParseException {

        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        JSONArray array = response.getJSONArray("comments");

        if(comments!=null){
            return comments;
        }
        comments = new ArrayList<Comment>();
        for(int i = 0;i!=array.length();i++){

            Comment comment = new Comment();
            comment.id = array.getJSONObject(i).getInt("id");
            comment.subject = array.getJSONObject(i).getString("subject");
            comment.timeStamp =  dateFormater.parse(array.getJSONObject(i).getString("timestamp")).getTime();
            comment.username = array.getJSONObject(i).getString("username");
            comment.text = array.getJSONObject(i).getString("text");
            comments.add(comment);
            if(i>4){
                seeAll = true;
            }
        }

        return comments;
    }



    public JSONArray getScreenshots() throws JSONException {
        return response.getJSONArray("sshots");
    }

    public JSONObject getMainOBB() throws JSONException {
        return response.getJSONObject("obb").getJSONObject("main");
    }

    public JSONObject getPatchOBB() throws JSONException {
        return response.getJSONObject("obb").getJSONObject("patch");
    }

    public boolean hasPatchOBB() throws JSONException {
        return response.getJSONObject("obb").has("patch");
    }

    public TasteModel getLikes() throws JSONException {
        TasteModel model = new TasteModel();
        JSONObject likevotes = response.getJSONObject("likevotes");

        model.likes = likevotes.getString("likes");
        model.dislikes =  likevotes.getString("dislikes");
        if(likevotes.has("uservote")){
            model.uservote = likevotes.getString("uservote");
        }

        return model;
    }

    public boolean isScreenshotChanged() {
        return screenshotChanged;
    }

    public void setScreenshotChanged(boolean screenshotChanged) {
        this.screenshotChanged = screenshotChanged;
    }

    public JSONObject getPayment() throws JSONException {
        return response.getJSONObject("payment");
    }

    public boolean hasPermissions() {
        return response.has("permissions");  //To change body of created methods use File | Settings | File Templates.
    }

    public class WebserviceOptions {
        String key;
        String value;


        private WebserviceOptions(String key,String value) {
            this.value = value;
            this.key = key;
        }

        /**
         * Returns a string containing a concise, human-readable description of this
         * object. Subclasses are encouraged to override this method and provide an
         * implementation that takes into account the object's type and data. The
         * default implementation is equivalent to the following expression:
         * <pre>
         *   getClass().getName() + '@' + Integer.toHexString(hashCode())</pre>
         * <p>See <a href="{@docRoot}reference/java/lang/Object.html#writing_toString">Writing a useful
         * {@code toString} method</a>
         * if you intend implementing your own {@code toString} method.
         *
         * @return a printable representation of this object.
         */
        @Override
        public String toString() {
            return key+"="+value;    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void finalize() throws Throwable {

            Log.d("TAG", "Garbage Collecting WebserviceResponse");
            super.finalize();
        }
    }

}
