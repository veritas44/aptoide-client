package cm.aptoide.ptdev.pushnotification;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.flurry.android.FlurryAgent;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Executors;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.services.TimelineActivitySyncService;
import cm.aptoide.ptdev.services.TimelinePostsSyncService;
import cm.aptoide.ptdev.services.UpdatesService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.IconSizes;
import cm.aptoide.ptdev.webservices.OauthErrorHandler;
import retrofit.RestAdapter;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by asantos on 01-09-2014.
 */
public class PushNotificationReceiver extends BroadcastReceiver {
    private static final String PUSH_NOTIFICATION_TITLE = "title";
    private static final String PUSH_NOTIFICATION_MSG = "MSG";
    private static final String PUSH_NOTIFICATION_EXTERNAL_URL = "url";
    private static final String PUSH_NOTIFICATION_IMG_URL = "img";
    public static final long PUSH_NOTIFICATION_TIME_INTERVAL = AlarmManager.INTERVAL_DAY; //AlarmManager.INTERVAL_FIFTEEN_MINUTES / 30;



    // same as Manifest
    public final String PUSH_NOTIFICATION_Action_TRACK_URL = Aptoide.getConfiguration().getTrackUrl();

    public final String PUSH_NOTIFICATION_Action = Aptoide.getConfiguration().getAction();

    public final String PUSH_NOTIFICATION_Action_FIRST_TIME = Aptoide.getConfiguration().getActionFirstTime();


    private static final String PUSH_NOTIFICATION_TRACK_URL = "trackUrl";
    public static final String SPREF_PNOTIFICATION_ID = "lastPNotificationId";

    public class MyImageLoadingListener implements ImageLoadingListener {

        private final Context context;
        private final Bundle extra;

        public MyImageLoadingListener(Context context, Bundle extra) {

            this.context = context;
            this.extra = extra;
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {
//            Log.i("PushNotificationReceiver", "onLoadingStarted");
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
//            Log.i("PushNotificationReceiver", "onLoadingFailed");
            loadNotification(extra, context);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//            Log.i("PushNotificationReceiver", "onLoadingComplete");
            loadNotification(extra, context, loadedImage);
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
//            Log.i("PushNotificationReceiver", "onLoadingCancelled");
        }
    }


    public interface Notifications{

        @POST("/3/getPushNotifications")
        @FormUrlEncoded
        PushNotificationJson getPushNotifications(@FieldMap HashMap<String, String> parameters);

    }


    public void createPendingIntent(AlarmManager am, Context context, String action, long time, int broadcastid){
        Intent i = new Intent(context, PushNotificationReceiver.class);
        i.setAction(action);
        PendingIntent pi = PendingIntent.getBroadcast(context, broadcastid, i, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, time , pi);
    }


    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(Intent.ACTION_BOOT_COMPLETED) ) {
                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                createPendingIntent(am, context, PUSH_NOTIFICATION_Action, PUSH_NOTIFICATION_TIME_INTERVAL, 982764);
                createPendingIntent(am, context, "updates_service", AlarmManager.INTERVAL_HALF_DAY / 2, 982765);

                createPendingIntent(am, context, "timelinepostsservice",  AlarmManager.INTERVAL_HALF_DAY, 982766);
                createPendingIntent(am, context, "timelineactivityservice",  AlarmManager.INTERVAL_HALF_DAY, 982767);


//                Log.i("PushNotificationReceiver", "Alarm Registed Received");

            }else if(action.equals("timelineactivityservice")) {
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        TimelineActivitySyncService syncService = new TimelineActivitySyncService();
                        syncService.sync(context , context.getPackageName());
                    }});

            }else if(action.equals("timelinepostsservice")) {
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        TimelinePostsSyncService syncService = new TimelinePostsSyncService();
                        syncService.sync(context, context.getPackageName());
                    }});
            }else if(action.equals("updates_service")) {

                Intent i = new Intent(context, UpdatesService.class);
                context.startService(i);

            } else if (action.equals(PUSH_NOTIFICATION_Action)) {
//                Log.i("PushNotificationReceiver", "PUSH_NOTIFICATION_Action");

                final Handler handler = new Handler(Looper.getMainLooper());

                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            //GenericUrl url = new GenericUrl(WebserviceOptions.WebServicesLink + "/3/getPushNotifications");

                            HashMap<String, String> parameters = new HashMap<String, String>();

                            String oemid = Aptoide.getConfiguration().getExtraId();
                            if (!TextUtils.isEmpty(oemid)) {
                                parameters.put("oem_id", oemid);
                            }
                            parameters.put("mode", "json");
                            parameters.put("limit", "1");
                            parameters.put("lang", AptoideUtils.getMyCountry(Aptoide.getContext()));

                            if(Aptoide.DEBUG_MODE){
                                SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext());
                                parameters.put("notification_type", sPref.getString("notificationtype", null));
                            }


                            int lastId = PreferenceManager.getDefaultSharedPreferences(context).getInt(SPREF_PNOTIFICATION_ID,0);

                            parameters.put("id", String.valueOf(lastId));



                            //HttpContent content = new UrlEncodedContent(parameters);
                            //HttpRequest httpRequest = AndroidHttp.newCompatibleTransport().createRequestFactory().buildPostRequest(url, content);
                            //httpRequest.setParser(new JacksonFactory().createJsonObjectParser());
                            PushNotificationJson response = new RestAdapter.Builder().setConverter(OauthErrorHandler.createConverter()).setEndpoint("https://webservices.aptoide.com/webservices").build().create(Notifications.class).getPushNotifications(parameters);
                            //PushNotificationJson response = httpRequest.execute().parseAs(PushNotificationJson.class);
//                            Log.i("PushNotificationReceiver", "getResults() is " + response.getResults().size());

                            for (final PushNotificationJson.Notification notification : response.getResults()) {
                                final Bundle extra = intent.getExtras();

                                extra.putString(PUSH_NOTIFICATION_EXTERNAL_URL, notification.getTarget_url());
                                extra.putString(PUSH_NOTIFICATION_MSG, notification.getMessage());
                                extra.putString(PUSH_NOTIFICATION_TRACK_URL, notification.getTrack_url());
                                extra.putString(PUSH_NOTIFICATION_TITLE, notification.getTitle());

//                                Log.i("PushNotificationReceiver", "Loading image " + notification.getTitle());

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        try{

                                            String bannerUrl = notification.getImages()!=null?getSize(notification.getImages().getBanner_url()):null;

                                            ImageLoader.getInstance().loadImage(bannerUrl, new MyImageLoadingListener(context, extra));
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }


                                    }
                                });



                            }

                            if(!response.getResults().isEmpty()){
                                lastId = response.getResults().get(0).getId().intValue();
                            }





                            PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(SPREF_PNOTIFICATION_ID, lastId).commit();


                        } catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                });

            }else if(action.equals(PUSH_NOTIFICATION_Action_TRACK_URL)){
                String trackUrl = intent.getStringExtra(PUSH_NOTIFICATION_TRACK_URL);
                String externalUrl = intent.getStringExtra(PUSH_NOTIFICATION_EXTERNAL_URL);


                try {

                    new OkHttpClient().newCall(new Request.Builder().url(trackUrl).build()).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {

                        }

                        @Override
                        public void onResponse(Response response) throws IOException {

                        }
                    });

                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(externalUrl));
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private String getSize(String banner_url) {
            String[] splittedUrl = banner_url.split("\\.(?=[^\\.]+$)");
            banner_url = splittedUrl[0] + "_" + IconSizes.generateSizeStringNotification(Aptoide.getContext()) + "."+ splittedUrl[1];
        return banner_url;
    }



    private void loadNotification(Bundle extra, Context context) {
        loadNotification(extra, context, null);
    }

    private void loadNotification(Bundle extra, Context context, Bitmap o) {
//        Log.i("PushNotificationReceiver", o == null ? "Image was null" : "Image was good");
//        Log.i("PushNotificationReceiver", "Title: " + extra.getCharSequence(PUSH_NOTIFICATION_TITLE));
//        Log.i("PushNotificationReceiver", "Msg: " + extra.getCharSequence(PUSH_NOTIFICATION_MSG));
//        Log.i("PushNotificationReceiver", "URL: " + extra.getCharSequence(PUSH_NOTIFICATION_EXTERNAL_URL));

        Intent resultIntent = new Intent(PUSH_NOTIFICATION_Action_TRACK_URL);


        resultIntent.putExtra(PUSH_NOTIFICATION_TRACK_URL, extra.getString(PUSH_NOTIFICATION_TRACK_URL));
        resultIntent.putExtra(PUSH_NOTIFICATION_EXTERNAL_URL, extra.getString(PUSH_NOTIFICATION_EXTERNAL_URL));

        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(context, new Random().nextInt(), resultIntent, 0);

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(getDrawableResource())
                .setContentIntent(resultPendingIntent)
                .setOngoing(false)
                .setContentTitle(extra.getCharSequence(PUSH_NOTIFICATION_TITLE))
                .setContentText(extra.getCharSequence(PUSH_NOTIFICATION_MSG)).build();

        if (Build.VERSION.SDK_INT >= 16) {
//            Log.d("PushNotificationReceiver", "is 16 or more, BIG!!!");
            RemoteViews expandedView = new RemoteViews(context.getPackageName(),
                    cm.aptoide.ptdev.R.layout.pushnotificationlayout);
            expandedView.setBitmap(cm.aptoide.ptdev.R.id.PushNotificationImageView, "setImageBitmap", o);
            expandedView.setImageViewBitmap(cm.aptoide.ptdev.R.id.icon, BitmapFactory.decodeResource(context.getResources(),getDrawableResource()));
            expandedView.setTextViewText(cm.aptoide.ptdev.R.id.text1, extra.getCharSequence(PUSH_NOTIFICATION_TITLE));
            expandedView.setTextViewText(cm.aptoide.ptdev.R.id.description, extra.getCharSequence(PUSH_NOTIFICATION_MSG));
            notification.bigContentView = expandedView;
        }


//        Log.i("PushNotificationReceiver", "notification built");
        FlurryAgent.logEvent("Push_Notification_Loaded");
        final NotificationManager managerNotification = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        managerNotification.notify(86456, notification);

    }

    public int getDrawableResource(){
        return R.drawable.icon_brand_aptoide;
    }

}
