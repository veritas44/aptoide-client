package cm.aptoide.ptdev.pushnotification;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import java.util.Random;

import cm.aptoide.ptdev.R;

/**
 * Created by asantos on 01-09-2014.
 */
public class PushNotificationReceiver extends BroadcastReceiver {
    private static final String PUSH_NOTIFICATION_TITLE = "title";
    private static final String PUSH_NOTIFICATION_MSG = "MSG";
    private static final String PUSH_NOTIFICATION_EXTERNAL_URL = "url";
    private static final String PUSH_NOTIFICATION_IMG_URL = "img";
    private static final long PUSH_NOTIFICATION_TIME_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES / 30;//

    // same as Manifest
    public static final String PUSH_NOTIFICATION_Action = "cm.aptoide.pt.PushNotification";
    public static final String PUSH_NOTIFICATION_Action_FIRST_TIME = "cm.aptoide.pt.PushNotificationFirstTime";

    public class MyImageLoadingListener implements ImageLoadingListener {

        private final Context context;
        private final Bundle extra;

        public MyImageLoadingListener(Context context, Bundle extra) {

            this.context = context;
            this.extra = extra;
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            Log.i("PushNotificationReceiver", "onLoadingStarted");
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            Log.i("PushNotificationReceiver", "onLoadingFailed");
            loadNotification(extra, context);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            Log.i("PushNotificationReceiver", "onLoadingComplete");
            loadNotification(extra, context, loadedImage);
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            Log.i("PushNotificationReceiver", "onLoadingCancelled");

        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(Intent.ACTION_BOOT_COMPLETED)
                    || action.equals(PUSH_NOTIFICATION_Action_FIRST_TIME)) {
                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent i = new Intent(context, PushNotificationReceiver.class);

                //TO Be Removed
                i = fillintentStuffFortest(i);

                i.setAction(PUSH_NOTIFICATION_Action);
                PendingIntent pi = PendingIntent.getBroadcast(context, 982764, i, PendingIntent.FLAG_UPDATE_CURRENT);
                am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), PUSH_NOTIFICATION_TIME_INTERVAL, pi);
                Log.i("PushNotificationReceiver", "Alarm Registed Received");
            } else if (action.equals(PUSH_NOTIFICATION_Action)) {
                Log.i("PushNotificationReceiver", "PUSH_NOTIFICATION_Action");
                Bundle extra = intent.getExtras();
                ImageLoader.getInstance().loadImage(
                        extra.getString(PUSH_NOTIFICATION_IMG_URL),
                        new MyImageLoadingListener(context, extra));

            }
        }

    }

    private Intent fillintentStuffFortest(Intent i) {
        return i
                .putExtra(PUSH_NOTIFICATION_TITLE, "TwinTAB Android Tablet")
                .putExtra(PUSH_NOTIFICATION_MSG, "Calling Tablet! Reveals an immersive device. Know more information here!")
                .putExtra(PUSH_NOTIFICATION_IMG_URL, "http://dl.dropboxusercontent.com/u/4804935/twinmos.jpg")
                .putExtra(PUSH_NOTIFICATION_EXTERNAL_URL, "http://www.twinmos.com/");
    }

    private void loadNotification(Bundle extra, Context context) {
        loadNotification(extra, context, null);
    }

    private void loadNotification(Bundle extra, Context context, Bitmap o) {
        Log.i("PushNotificationReceiver", o == null ? "Image was null" : "Image was good");
        Log.i("PushNotificationReceiver", "Title: " + extra.getCharSequence(PUSH_NOTIFICATION_TITLE));
        Log.i("PushNotificationReceiver", "Msg: " + extra.getCharSequence(PUSH_NOTIFICATION_MSG));
        Log.i("PushNotificationReceiver", "URL: " + extra.getCharSequence(PUSH_NOTIFICATION_EXTERNAL_URL));

        Intent resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(extra.getString(PUSH_NOTIFICATION_EXTERNAL_URL)));
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 86456, resultIntent, PendingIntent.FLAG_ONE_SHOT);

//        Intent resultIntent = new Intent(context, AptoidePartner.getConfiguration().getStartActivityClass());
//        Intent resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(PUSH_NOTIFICATION_EXTERNAL_URL));
//        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
//                Intent.FLAG_ACTIVITY_SINGLE_TOP |
//                Intent.FLAG_ACTIVITY_NEW_TASK);

        // This ensures that the back button follows the recommended convention for the back key.
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Adds the back stack for the Intent (but not the Intent itself)
//        stackBuilder.addParentStack(AptoidePartner.getConfiguration().getStartActivityClass());

        // Adds the Intent that starts the Activity to the top of the stack.
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);



         PendingIntent resultPendingIntent = PendingIntent.getActivity(context, new Random().nextInt(), resultIntent, 0);

        // Create remote view and set bigContentView.
        /*RemoteViews expandedView = new RemoteViews(context.getPackageName(),
                R.layout.pushnotificationlayout);
        expandedView.setBitmap(R.id.PushNotificationImageView,"setImageBitmap",o);
        expandedView.setTextViewText(R.id.text1, "Pois!");*/


        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.icon_brand_aptoide)
                .setContentIntent(resultPendingIntent)
                .setOngoing(false)
                .setContentTitle(extra.getCharSequence(PUSH_NOTIFICATION_TITLE))
                .setContentText(extra.getCharSequence(PUSH_NOTIFICATION_MSG)).build();

        if (Build.VERSION.SDK_INT >= 16) {
            Log.d("PushNotificationReceiver", "is 16 or more, BIG!!!");
            RemoteViews expandedView = new RemoteViews(context.getPackageName(),
                    R.layout.pushnotificationlayout);
            expandedView.setBitmap(R.id.PushNotificationImageView, "setImageBitmap", o);
            expandedView.setTextViewText(R.id.text1, extra.getCharSequence(PUSH_NOTIFICATION_TITLE));
            expandedView.setTextViewText(R.id.description, extra.getCharSequence(PUSH_NOTIFICATION_MSG));
            notification.bigContentView = expandedView;
        }




/*
        NotificationCompat.BigPictureStyle notiStyle = new
                NotificationCompat.BigPictureStyle();
        notiStyle.setBigContentTitle(extra.getCharSequence(PUSH_NOTIFICATION_TITLE));
        notiStyle.setSummaryText("Blabla.");
        notiStyle.bigPicture(o);



        Intent resultIntent = new Intent(context, Start.class);

        // This ensures that the back button follows the recommended
// convention for the back key.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

// Adds the back stack for the Intent (but not the Intent itself).
        stackBuilder.addParentStack(Start.class);

// Adds the Intent that starts the Activity to the top of the stack.
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification myNotification = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setAutoCancel(true)
                .setLargeIcon(o)
                .setContentIntent(resultPendingIntent)
                .setContentTitle(extra.getCharSequence(PUSH_NOTIFICATION_TITLE))
                .setContentText("pois!")
                .setStyle(notiStyle).build();
        final NotificationManager managerNotification = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        managerNotification.notify(86456, myNotification);
        Log.i("PushNotificationReceiver","notification built");
*/

       /* Intent onClick = new Intent(Intent.ACTION_VIEW);
        onClick.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent onClickAction = PendingIntent.getActivity(context, 0, onClick, 0);

        final Builder mBuilder = new Builder(context);
        mBuilder.setContentTitle(extra.getCharSequence(PUSH_NOTIFICATION_TITLE));
        mBuilder.setContentText("Blablabla");
        mBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
        mBuilder.setLargeIcon(o);
        mBuilder.setContentIntent(onClickAction);
        mBuilder.setAutoCancel(true);
        */
        Log.i("PushNotificationReceiver", "notification built");
        final NotificationManager managerNotification = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        managerNotification.notify(86456, notification);

    }
}
