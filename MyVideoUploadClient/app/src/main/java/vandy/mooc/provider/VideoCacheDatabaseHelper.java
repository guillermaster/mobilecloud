package vandy.mooc.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

/**
 * Created by gpincay on 7/15/2015.
 */
public class VideoCacheDatabaseHelper extends SQLiteOpenHelper {
    /**
     * If the database schema is changed, the database version must be
     * incremented.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Database name.
     */
    public static final String DATABASE_NAME =  "videocache.db";

    public VideoCacheDatabaseHelper(Context context) {
        super(context,
                context.getCacheDir()
                        + File.separator
                        + DATABASE_NAME,
                null,
                DATABASE_VERSION);
    }

    /**
     * Hook method called when Database is created.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Define an SQL string that creates a table to hold Acronyms.
        // Each Acronym has a list of LongForms in Json.
        final String SQL_CREATE_ACRONYM_TABLE =
                "CREATE TABLE "
                        + VideoCacheContract.VideoCacheEntry.TABLE_NAME + " ("
                        + VideoCacheContract.VideoCacheEntry._ID + " INTEGER PRIMARY KEY, "
                        + VideoCacheContract.VideoCacheEntry.COLUMN_ID_WS + " LONG NOT NULL, "
                        + VideoCacheContract.VideoCacheEntry.COLUMN_TITLE + " TEXT NOT NULL, "
                        + VideoCacheContract.VideoCacheEntry.COLUMN_DURATION + " LONG, "
                        + VideoCacheContract.VideoCacheEntry.COLUMN_DATA_URL + " TEXT NOT NULL, "
                        + VideoCacheContract.VideoCacheEntry.COLUMN_STAR_RATING + " INTEGER NOT NULL "
                        + " );";

        // Create the table.
        db.execSQL(SQL_CREATE_ACRONYM_TABLE);
    }

    /**
     * Hook method called when Database is upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion) {
        // This database is only a cache for online data, so its
        // upgrade policy is to simply to discard the data and start
        // over.  This method only fires if you change the version
        // number for your database.  It does NOT depend on the
        // version number for your application.  If the schema is
        // updated without wiping data, commenting out the next 2
        // lines should be the top priority before modifying this
        // method.
        db.execSQL("DROP TABLE IF EXISTS "
                + VideoCacheContract.VideoCacheEntry.TABLE_NAME);
        onCreate(db);
    }
}
