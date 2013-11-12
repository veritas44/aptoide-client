package cm.aptoide.ptdev.model;

import android.content.ContentValues;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.database.StatementHelper;
import cm.aptoide.ptdev.database.schema.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 14-10-2013
 * Time: 17:49
 * To change this template use File | Settings | File Templates.
 */
public class ApkInfoXML extends Apk {

    private long repoId;

    @Override
    public List<String> getStatements() {

        ArrayList<String> statements = new ArrayList<String>(10);
        ArrayList<String> values = new ArrayList<String>();

        values.add(Schema.Apk.COLUMN_APKID);
        values.add(Schema.Apk.COLUMN_NAME);
        values.add(Schema.Apk.COLUMN_VERCODE);
        values.add(Schema.Apk.COLUMN_REPO_ID);

        statements.add(0, StatementHelper.getInsertStatment(Schema.Apk.getName(), values));

        values.add(Schema.Category.COLUMN_NAME);
        values.add(Schema.Category.COLUMN_REPO_ID);
        values.add(Schema.Category.COLUMN_ID_PARENT);

        statements.add(1, StatementHelper.getInsertStatment(Schema.Category.getName(), values));

        values.add(Schema.Category_Apk.COLUMN_APK_ID);
        values.add(Schema.Category_Apk.COLUMN_CATEGORY_ID);

        statements.add(2, StatementHelper.getInsertStatment(Schema.Category_Apk.getName(), values));


        statements.add(3, "select id_apk from apk where id_repo = ? and package_name = ? and version_code = ?");
        statements.add(4, "select id_category from category where name = ? and id_repo = ? and id_category_parent = ?");


        return statements;
    }

    @Override
    public void databaseInsert(List<SQLiteStatement> sqLiteStatements, HashMap<String, Long> categoriesIds) {

        long apkid;
        long category1id;
        long category2id;

        try {

            StatementHelper.bindAllArgsAsStrings(sqLiteStatements.get(0), new String[]{getPackageName(), getName(), String.valueOf(getVersionCode()), String.valueOf(getRepoId())});
            apkid = sqLiteStatements.get(0).executeInsert();

        } catch (SQLiteException e) {
            e.printStackTrace();

            Log.d("RepoParser-ApkInfo-Insert", "Conflict: " + e.getMessage() + " on " + getPackageName() + " " + getRepoId() + " " + getVersionCode());
            StatementHelper.bindAllArgsAsStrings(sqLiteStatements.get(3), new String[]{ String.valueOf(getRepoId()), getPackageName(), String.valueOf(getVersionCode()) });
            apkid = sqLiteStatements.get(3).simpleQueryForLong();

        }

        if (Aptoide.getDb().yieldIfContendedSafely(1000)) {
            Log.d("RepoParser", "yelded");
        }

        if (categoriesIds.containsKey(getCategory1())) {
            category1id = categoriesIds.get(getCategory1());
        } else {

            StatementHelper.bindAllArgsAsStrings(sqLiteStatements.get(1), new String[]{getCategory1(), String.valueOf(getRepoId()), "0"});

            try {
                category1id = sqLiteStatements.get(1).executeInsert();

                if (Aptoide.getDb().yieldIfContendedSafely(1000)) {
                    Log.d("RepoParser", "yelded");
                }

            } catch (SQLiteException e) {
                e.printStackTrace();
                StatementHelper.bindAllArgsAsStrings(sqLiteStatements.get(4), new String[]{getCategory1(), String.valueOf(getRepoId()), "0"});
                category1id = sqLiteStatements.get(4).simpleQueryForLong();
            }
            categoriesIds.put(getCategory1(), category1id);

        }

        if (categoriesIds.containsKey(getCategory2())) {
            category2id = categoriesIds.get(getCategory2());
        } else {

            StatementHelper.bindAllArgsAsStrings(sqLiteStatements.get(1), new String[]{getCategory2(), String.valueOf(getRepoId()), String.valueOf(category1id)});

            try {
                category2id = sqLiteStatements.get(1).executeInsert();

                if (Aptoide.getDb().yieldIfContendedSafely(1000)) {
                    Log.d("RepoParser", "yelded");
                }

            } catch (SQLiteException e) {
                e.printStackTrace();
                StatementHelper.bindAllArgsAsStrings(sqLiteStatements.get(4), new String[]{getCategory2(), String.valueOf(getRepoId()), String.valueOf(category1id)});
                category2id = sqLiteStatements.get(4).simpleQueryForLong();
            }
            categoriesIds.put(getCategory2(), category2id);

        }

        try{
            StatementHelper.bindAllArgsAsStrings(sqLiteStatements.get(2), new String[]{ String.valueOf(apkid), String.valueOf(category2id)});
            sqLiteStatements.get(2).executeInsert();
        } catch (SQLiteException e){
            e.printStackTrace();
        }

        if (Aptoide.getDb().yieldIfContendedSafely(1000)) {
            Log.d("RepoParser", "yelded");
        }


    }

    @Override
    public void databaseDelete(Database db) {

    }

    public long getRepoId() {
        return repoId;
    }

    public void setRepoId(long repoId) {
        this.repoId = repoId;
    }
}
