package cm.aptoide.ptdev.model;

import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.database.StatementHelper;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.Filters;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 06-11-2013
 * Time: 14:38
 * To change this template use File | Settings | File Templates.
 */
public class ApkTopXML extends Apk {

    public ApkTopXML() {
        super();
    }

    public ApkTopXML(ApkTopXML apkTopXML) {
        super(apkTopXML);
    }

    @Override
    public List<String> getStatements() {

        ArrayList<String> statements = new ArrayList<String>(10);
        ArrayList<String> values = getValues();


        statements.add(0, StatementHelper.getInsertStatment(Schema.Apk.getName(), values));

        values.add(Schema.Category.COLUMN_NAME);
        values.add(Schema.Category.COLUMN_REPO_ID);
        values.add(Schema.Category.COLUMN_ID_PARENT);

        statements.add(1, StatementHelper.getInsertStatment(Schema.Category.getName(), values));

        values.add(Schema.Category_Apk.COLUMN_APK_ID);
        values.add(Schema.Category_Apk.COLUMN_CATEGORY_ID);
        values.add(Schema.Category_Apk.COLUMN_REPO_ID);

        statements.add(2, StatementHelper.getInsertStatment(Schema.Category_Apk.getName(), values));


        statements.add(3, "select id_apk from apk where id_repo = ? and package_name = ? and version_code = ?");


        return statements;
    }

    @Override
    public void databaseInsert(List<SQLiteStatement> sqLiteStatements, HashMap<Integer, Integer> categoriesIds) {

        long apkid;
        try {

            StatementHelper.bindAllArgsAsStrings(sqLiteStatements.get(0),
                    new String[]{getPackageName(),
                            getName(),
                            String.valueOf(getVersionCode()),
                            String.valueOf(getVersionName()),
                            String.valueOf(getRepoId()),
                            String.valueOf(getDate().getTime()),
                            String.valueOf(getDownloads()),
                            String.valueOf(getRating()),
                            String.valueOf(getAge().equals(Filters.Age.Mature)?1:0),
                            String.valueOf(getMinSdk()),
                            String.valueOf(getMinScreen()),
                            getMinGlEs(),
                            getIconPath(),
                            String.valueOf(AptoideUtils.isCompatible(this)?1:0),
                            getSignature(),
                            getPath(),
                            getMd5h(),
                            String.valueOf(getPrice())

                    });
            apkid = sqLiteStatements.get(0).executeInsert();

        } catch (SQLiteException e) {
            //e.printStackTrace();

            //Log.d("RepoParser-ApkInfo-Insert", "Conflict: " + e.getMessage() + " on " + getPackageName() + " " + getRepoId() + " " + getVersionCode());
            StatementHelper.bindAllArgsAsStrings(sqLiteStatements.get(3), new String[]{ String.valueOf(getRepoId()), getPackageName(), String.valueOf(getVersionCode()) });
            apkid = sqLiteStatements.get(3).simpleQueryForLong();
        }

        if (Aptoide.getDb().yieldIfContendedSafely(1000)) {
            Log.d("RepoParser", "yelded");
        }

//        if (categoriesIds.containsKey(getCategory1())) {
//            category1id = categoriesIds.get(getCategory1());
//        } else {
//
//            StatementHelper.bindAllArgsAsStrings(sqLiteStatements.get(1), new String[]{getCategory1(), String.valueOf(getRepoId()), "0"});
//
//            try {
//                category1id = sqLiteStatements.get(1).executeInsert();
//                if (Aptoide.getDb().yieldIfContendedSafely(1000)) {
//                    Log.d("RepoParser", "yelded");
//                }
//            } catch (SQLiteException e) {
//                e.printStackTrace();
//                StatementHelper.bindAllArgsAsStrings(sqLiteStatements.get(4), new String[]{getCategory1(), String.valueOf(getRepoId()), "0"});
//                category1id = sqLiteStatements.get(4).simpleQueryForLong();
//            }
//            categoriesIds.put(getCategory1(), category1id);
//
//        }
//
        for (Integer catid : getCategoryId()) {

            try {
                StatementHelper.bindAllArgsAsStrings(sqLiteStatements.get(2), new String[]{String.valueOf(apkid), String.valueOf(catid), String.valueOf(getRepoId()),});
                sqLiteStatements.get(2).executeInsert();

            } catch (SQLiteException e) {
                e.printStackTrace();
            }

            if (Aptoide.getDb().yieldIfContendedSafely(1000)) {
                Log.d("RepoParser", "yelded");
            }
        }

    }

    @Override
    public void addApkToChildren() {
        getChildren().add(new ApkTopXML(this));
    }

    @Override
    public void databaseDelete(Database db) {

    }
}
