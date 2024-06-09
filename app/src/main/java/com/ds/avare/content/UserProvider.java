package com.ds.avare.content;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by zkhan on 3/10/17.
 */

public class UserProvider extends MainProvider {


    public static final int PLANS = 900;
    public static final int PLANS_ID = 901;
    public static final int LISTS = 902;
    public static final int LISTS_ID = 903;
    public static final int WNBS = 904;
    public static final int WNBS_ID = 905;
    public static final int RECENTS = 906;
    public static final int RECENTS_ID = 907;
    public static final int TAGS = 908;
    public static final int TAGS_ID = 909;
    public static final int DRAWS = 910;
    public static final int DRAWS_ID = 911;
    public static final int AIRCRAFTS = 912;
    public static final int AIRCRAFTS_ID = 913;

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/rv-user";

    private static final UriMatcher mURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        mURIMatcher.addURI(UserContract.AUTHORITY, UserContract.BASE_PLAN, PLANS);
        mURIMatcher.addURI(UserContract.AUTHORITY, UserContract.BASE_PLAN + "/#", PLANS_ID);
        mURIMatcher.addURI(UserContract.AUTHORITY, UserContract.BASE_LIST, LISTS);
        mURIMatcher.addURI(UserContract.AUTHORITY, UserContract.BASE_LIST + "/#", LISTS_ID);
        mURIMatcher.addURI(UserContract.AUTHORITY, UserContract.BASE_WNB, WNBS);
        mURIMatcher.addURI(UserContract.AUTHORITY, UserContract.BASE_WNB + "/#", WNBS_ID);
        mURIMatcher.addURI(UserContract.AUTHORITY, UserContract.BASE_RECENT, RECENTS);
        mURIMatcher.addURI(UserContract.AUTHORITY, UserContract.BASE_RECENT + "/#", RECENTS_ID);
        mURIMatcher.addURI(UserContract.AUTHORITY, UserContract.BASE_TAG, TAGS);
        mURIMatcher.addURI(UserContract.AUTHORITY, UserContract.BASE_TAG + "/#", TAGS_ID);
        mURIMatcher.addURI(UserContract.AUTHORITY, UserContract.BASE_DRAW, DRAWS);
        mURIMatcher.addURI(UserContract.AUTHORITY, UserContract.BASE_DRAW + "/#", DRAWS_ID);
        mURIMatcher.addURI(UserContract.AUTHORITY, UserContract.BASE_AIRCRAFT, AIRCRAFTS);
        mURIMatcher.addURI(UserContract.AUTHORITY, UserContract.BASE_AIRCRAFT + "/#", AIRCRAFTS_ID);
    }

    @Override
    public String getType(Uri uri) {
        int uriType = mURIMatcher.match(uri);
        return switch (uriType) {
            case PLANS, LISTS -> CONTENT_TYPE;
            case WNBS -> CONTENT_TYPE;
            case RECENTS -> CONTENT_TYPE;
            case TAGS -> CONTENT_TYPE;
            case DRAWS -> CONTENT_TYPE;
            case AIRCRAFTS -> CONTENT_TYPE;
            default -> null;
        };
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        String table = null;
        int uriType = mURIMatcher.match(uri);
        table = switch (uriType) {
            case PLANS -> UserContract.TABLE_PLAN;
            case LISTS -> UserContract.TABLE_LIST;
            case WNBS -> UserContract.TABLE_WNB;
            case RECENTS -> UserContract.TABLE_RECENT;
            case TAGS -> UserContract.TABLE_TAG;
            case DRAWS -> UserContract.TABLE_DRAW;
            case AIRCRAFTS -> UserContract.TABLE_AIRCRAFT;
            default -> throw new IllegalArgumentException("Unknown URI");
        };

        int rows = 0;
        try {
            SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
            rows = db.delete(table, selection, selectionArgs);
        }
        catch (Exception e) {
            // Something wrong, missing or deleted database from download
            resetDatabase();
        }
        return rows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String table = null;
        int uriType = mURIMatcher.match(uri);
        int rows = 0;
        table = switch (uriType) {
            case PLANS -> UserContract.TABLE_PLAN;
            case LISTS -> UserContract.TABLE_LIST;
            case WNBS -> UserContract.TABLE_WNB;
            case RECENTS -> UserContract.TABLE_RECENT;
            case TAGS -> UserContract.TABLE_TAG;
            case DRAWS -> UserContract.TABLE_DRAW;
            case AIRCRAFTS -> UserContract.TABLE_AIRCRAFT;
            default -> throw new IllegalArgumentException("Unknown URI");
        };

        try {
            SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
            rows = db.update(table, values, selection, selectionArgs);
        }
        catch (Exception e) {
            // Something wrong, missing or deleted database from download
            resetDatabase();
        }
        return rows;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        int uriType = mURIMatcher.match(uri);
        switch (uriType) {
            case PLANS:
                queryBuilder.setTables(UserContract.TABLE_PLAN);
                break;
            case LISTS:
                queryBuilder.setTables(UserContract.TABLE_LIST);
                break;
            case WNBS:
                queryBuilder.setTables(UserContract.TABLE_WNB);
                break;
            case RECENTS:
                queryBuilder.setTables(UserContract.TABLE_RECENT);
                break;
            case TAGS:
                queryBuilder.setTables(UserContract.TABLE_TAG);
                break;
            case DRAWS:
                queryBuilder.setTables(UserContract.TABLE_DRAW);
                break;
            case AIRCRAFTS:
                queryBuilder.setTables(UserContract.TABLE_AIRCRAFT);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }

        try {
            Cursor cursor = queryBuilder.query(mDatabaseHelper.getReadableDatabase(),
                    projection, selection, selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        }
        catch (Exception e) {
            // Something wrong, missing or deleted database from download
            resetDatabase();
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        String table = null;
        int uriType = mURIMatcher.match(uri);
        table = switch (uriType) {
            case PLANS -> UserContract.TABLE_PLAN;
            case LISTS -> UserContract.TABLE_LIST;
            case WNBS -> UserContract.TABLE_WNB;
            case RECENTS -> UserContract.TABLE_RECENT;
            case TAGS -> UserContract.TABLE_TAG;
            case DRAWS -> UserContract.TABLE_DRAW;
            case AIRCRAFTS -> UserContract.TABLE_AIRCRAFT;
            default -> throw new IllegalArgumentException("Unknown URI");
        };

        try {
            SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
            long id = db.insert(table, null, values);
            if (id > 0) {
                return switch (uriType) {
                    case PLANS -> UserContract.buildPlansUri(id);
                    case LISTS -> UserContract.buildListsUri(id);
                    case WNBS -> UserContract.buildWnbsUri(id);
                    case RECENTS -> UserContract.buildRecentsUri(id);
                    case TAGS -> UserContract.buildTagsUri(id);
                    case DRAWS -> UserContract.buildDrawsUri(id);
                    case AIRCRAFTS -> UserContract.buildAircraftUri(id);
                    default -> throw new IllegalArgumentException("Unknown URI");
                };
            }
            else {
                throw new android.database.SQLException("Failed to insert row into: " + uri);
            }
        }
        catch (Exception e) {
            // Something wrong, missing or deleted database from download
            resetDatabase();
        }
        return null;
    }

    @Override
    public boolean onCreate() {
        super.onCreate();
        // user data goes in external folders
        String path = mPref.getUserDataFolder();
        mDatabaseHelper = new UserDatabaseHelper(getContext(), path);
        return true;
    }

    /**
     * Sync database on folder change, deleted database, new database, and other conditions
     */
    public void resetDatabase() {
        if(mDatabaseHelper != null) {
            mDatabaseHelper.close();
        }
        onCreate();
    }
}
