package cm.aptoide.ptdev.webservices;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import cm.aptoide.ptdev.fragments.GenericResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import java.util.HashMap;

/**
 * Created by rmateus on 30-12-2013.
 */
public class AddLikeRequest extends GoogleHttpClientSpiceRequest<GenericResponse>{


    private Context activity;
    private String token;
    private String repo;
    private String packageName;
    private String apkversion;

    public void setLike(boolean isLike) {
        this.isLike = isLike;
    }

    private boolean isLike;

    public AddLikeRequest(Context activity) {
        super(GenericResponse.class);

        this.activity = activity;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setApkversion(String apkversion) {
        this.apkversion = apkversion;
    }

    String baseUrl = "http://webservices.aptoide.com/webservices/addApkLike";
    @Override
    public GenericResponse loadDataFromNetwork() throws Exception {



        GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String > parameters = new HashMap<String, String>();

        parameters.put("mode", "json");
        parameters.put("token", token);
        parameters.put("repo", repo);
        parameters.put("like", isLike?"like":"dontLike");
        parameters.put("apkid", packageName);
        parameters.put("apkversion", apkversion);

        HttpContent content = new UrlEncodedContent(parameters);

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);

        request.setParser(new JacksonFactory().createJsonObjectParser());

        return request.execute().parseAs( getResultType() );
    }
}
