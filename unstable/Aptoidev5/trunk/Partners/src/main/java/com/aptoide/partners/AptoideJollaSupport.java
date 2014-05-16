package com.aptoide.partners;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.io.File;
import java.io.FileFilter;

import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.Aptoide;

/**
 * AptoideJollaSupport, provides support for the missing intents generated by the browser in the Jolla environment
 *
 * @author dsilveira
 * @since 2014.02.05
 * @version 2
 */
public class AptoideJollaSupport extends Activity {

    public static final String MYAPP_DOWNLOAD_DIR = "/home/nemo/Downloads/.aptoide";
    public static final String MYAPP_FILE_EXTENSION = ".myapp";
    public static final String MYAPP_INTENT_TYPE = "application/vnd.cm.aptoide.pt";
    public static final String DUMMY_URL_BASE = "http://imgs.aptoide.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String myappFileName = findDownloadedMyappFileName();
        if(!myappFileName.equals("")) {
            broadcastMyappIntent(myappFileName);
        }
        finish();
    }

    private String findDownloadedMyappFileName() {
        String mostRecentMyappFileName;

        File downloadDir = new File(MYAPP_DOWNLOAD_DIR);

        File[] myappFilesList = downloadDir.listFiles(
            new FileFilter() {
                public boolean accept(File file) {
                    return (file.isFile() && file.getName().endsWith(MYAPP_FILE_EXTENSION));
                }
            });

        long mostRecentModifiedTime = Long.MIN_VALUE;
        File mostRecentMyappFile = null;
        for (File myappFile : myappFilesList) {
            if (myappFile.lastModified() > mostRecentModifiedTime) {
                mostRecentMyappFile = myappFile;
                mostRecentModifiedTime = myappFile.lastModified();
            }
        }
        try {
            mostRecentMyappFileName = mostRecentMyappFile.getName();
        } catch (Exception e) {
            e.printStackTrace();
            mostRecentMyappFileName = "";
        }

        try {
            mostRecentMyappFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mostRecentMyappFileName;
    }

    private Class appViewClass = Aptoide.getConfiguration().getAppViewActivityClass();

    private void broadcastMyappIntent(String myappFileName) {
        String uri = DUMMY_URL_BASE + myappFileName;
        String[] strings = uri.split("-");
        long id = Long.parseLong(strings[strings.length-1].split("\\.myapp")[0]);

        Intent appViewIntent = new Intent(this, appViewClass);
        appViewIntent.putExtra("fromMyapp", true);
        appViewIntent.putExtra("id", id);

        startActivity(appViewIntent);

//        Intent myappIntent = new Intent(Intent.ACTION_VIEW);
//        myappIntent.setDataAndType(Uri.parse(DUMMY_URL_BASE + myappFileName), MYAPP_INTENT_TYPE);
//        startActivity(myappIntent);
    }
}