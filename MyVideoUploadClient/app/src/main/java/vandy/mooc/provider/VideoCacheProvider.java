package vandy.mooc.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class VideoCacheProvider extends ContentProvider {

    private VideoCacheDatabaseHelper mOpenHelper;
    /**
     * The URI Matcher used by this content provider.
     */
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final int VIDEOS = 1000;
    private static final int VIDEO = 1001;

    //public VideoCacheProvider() {
    //}

    private static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code
        // to return when a match is found.  The code passed into the
        // constructor represents the code to return for the rootURI.
        // It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        // For each type of URI that is added, a corresponding code is
        // created.
        matcher.addURI(VideoCacheContract.CONTENT_AUTHORITY,
                VideoCacheContract.PATH_VIDEO_CACHE, VIDEOS);

        matcher.addURI(VideoCacheContract.CONTENT_AUTHORITY,
                VideoCacheContract.PATH_VIDEO_CACHE + "/#", VIDEO);

        return matcher;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case VIDEOS:
                return VideoCacheContract.VideoCacheEntry.CONTENT_ITEMS_TYPE;
            case VIDEO:
                return VideoCacheContract.VideoCacheEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: "
                        + uri);
        }
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new VideoCacheDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;

        switch (sUriMatcher.match(uri)) {
            case VIDEOS:
                long id =  db.insert(VideoCacheContract.VideoCacheEntry.TABLE_NAME, null, values);

                // Check if a new row is inserted or not.
                if (id > 0)
                    returnUri = VideoCacheContract.VideoCacheEntry.buildVideoCacheUri(id);
                else
                    throw new android.database.SQLException
                            ("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: "
                        + uri);
        }

        // Notifies registered observers that a row was inserted.
        getContext().getContentResolver().notifyChange(uri,
                null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri,
                          ContentValues[] contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case VIDEOS:
                // Begins a transaction in EXCLUSIVE mode.
                db.beginTransaction();
                int returnCount = 0;

                try {
                    // contentValues into the SQLite database.
                    for(ContentValues contentValue : contentValues){
                        db.insert(VideoCacheContract.VideoCacheEntry.TABLE_NAME, null, contentValue);
                    }
                    // Marks the current transaction as successful.
                    db.setTransactionSuccessful();
                } finally {
                    // End a transaction.
                    db.endTransaction();
                }

                // Notifies registered observers that rows were updated.
                getContext().getContentResolver().notifyChange(uri,
                        null);
                return returnCount;
            default:
                return super.bulkInsert(uri,
                        contentValues);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor retCursor;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Match the id returned by UriMatcher to query appropriate
        // rows.
        switch (sUriMatcher.match(uri)) {
            case VIDEOS:
                retCursor = db.query(VideoCacheContract.VideoCacheEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case VIDEO:
                // Selection clause that matches row id with id passed
                // from Uri.
                final String rowId =
                        ""
                                + VideoCacheContract.VideoCacheEntry._ID
                                + " = '"
                                + ContentUris.parseId(uri)
                                + "'";

                retCursor = db.query(VideoCacheContract.VideoCacheEntry.TABLE_NAME, projection, rowId, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: "
                        + uri);
        }

        // Register to watch a content URI for changes.
        retCursor.setNotificationUri(getContext().getContentResolver(),
                uri);
        return retCursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        final SQLiteDatabase db =
                mOpenHelper.getWritableDatabase();

        int rowsUpdated;

        // Try to match against the path in a uri.  It returns the
        // code for the matched node (added using addURI), or -1 if
        // there is no matched node.  If a match occurs update the
        // appropriate rows.
        switch (sUriMatcher.match(uri)) {
            case VIDEOS:
                rowsUpdated = db.update(VideoCacheContract.VideoCacheEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: "
                        + uri);
        }

        // Notifies registered observers that rows were updated.
        if (rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri,
                    null);
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri,
                      String selection,
                      String[] selectionArgs) {
        // Create and/or open a database that will be used for reading
        // and writing. Once opened successfully, the database is
        // cached, so you can call this method every time you need to
        // write to the database.
        final SQLiteDatabase db =
                mOpenHelper.getWritableDatabase();

        // Keeps track of the number of rows deleted.
        int rowsDeleted = 0;

        // Try to match against the path in a url.  It returns the
        // code for the matched node (added using addURI) or -1 if
        // there is no matched node.  If a match is found delete the
        // appropriate rows.
        switch (sUriMatcher.match(uri)) {
            case VIDEOS:
                rowsDeleted = db.delete(VideoCacheContract.VideoCacheEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: "
                        + uri);
        }

        // Notifies registered observers that rows were deleted.
        if (selection == null || rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri,
                    null);
        return rowsDeleted;
    }

}
