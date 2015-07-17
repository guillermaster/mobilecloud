package vandy.mooc.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import java.util.List;

import vandy.mooc.model.mediator.webdata.Video;

/**
 * Created by gpincay on 7/14/2015.
 */
public class ContentProviderVideoCache implements VideoCache<Long, Video> {

    /**
     * Store the context to allow access to application-specific
     * resources and classes.
     */
    private Context mContext;

    public ContentProviderVideoCache(Context context){
        mContext = context;
    }

    /**
     * Gets the Video from the cache with the designated ID.
     *
     * @param id
     * @return Video
     */
    public Video get(Long id){
        // Selection clause to find rows with the given ID.
        final String SELECTION_CRITERIA = VideoCacheContract.VideoCacheEntry._ID + " = ?";
        // Initializes an array to contain selection arguments.
         String[] selectionArgs = { id.toString() };

        try{
            Cursor cursor = mContext.getContentResolver()
                    .query(VideoCacheContract.VideoCacheEntry.CONTENT_URI, null,
                            SELECTION_CRITERIA, selectionArgs, null);
            if(!cursor.moveToFirst()) return null;

            return getVideoFromCursor(cursor);
        }catch(Exception e){
            return null;
        }
    }

    /**
     * Put the video into the cache
     *
     * @param video
     */
    public Uri put(Video video){
        ContentValues contentValues = CreateContentValuesForVideo(video);
        return mContext.getContentResolver().insert(VideoCacheContract.VideoCacheEntry.CONTENT_URI, contentValues);
    }

    /**
     * Put more than one video into the cache
     *
     * @param videoList
     */
    public int put(List<Video> videoList){
        if(videoList.isEmpty()) return -1;

        ContentValues[] cvArray = new ContentValues[videoList.size()];

        for(int i = 0; i < videoList.size(); i++){
            cvArray[i] = CreateContentValuesForVideo(videoList.get(i));
        }

        return mContext.getContentResolver().
                bulkInsert(VideoCacheContract.VideoCacheEntry.CONTENT_URI, cvArray);
    }

    private ContentValues CreateContentValuesForVideo(Video video){
        ContentValues contentValues = new ContentValues();
        contentValues.put(VideoCacheContract.VideoCacheEntry.COLUMN_ID_WS, video.getId());
        contentValues.put(VideoCacheContract.VideoCacheEntry.COLUMN_TITLE, video.getTitle());
        contentValues.put(VideoCacheContract.VideoCacheEntry.COLUMN_DURATION, video.getDuration());
        contentValues.put(VideoCacheContract.VideoCacheEntry.COLUMN_CONTENT_TYPE, video.getContentType());
        contentValues.put(VideoCacheContract.VideoCacheEntry.COLUMN_DATA_URL, video.getContentType());
        contentValues.put(VideoCacheContract.VideoCacheEntry.COLUMN_STAR_RATING, video.getRating());
        return contentValues;
    }

    /**
     * Removes the value associated with an id
     *
     * @param id
     */
    public void remove(Long id){

    }

    /**
     * Get the size of the video cache.
     *
     * @return size
     */
    public int size(){
        try (Cursor cursor =
                     mContext.getContentResolver().query
                             (VideoCacheContract.VideoCacheEntry.CONTENT_URI,
                                     null,
                                     null,
                                     null,
                                     null)) {
            return cursor.getCount();
        }
    }

    private Video getVideoFromCursor(Cursor cursor){
        return new Video(
                cursor.getLong(cursor.getColumnIndex(VideoCacheContract.VideoCacheEntry._ID)),
                cursor.getString(cursor.getColumnIndex(VideoCacheContract.VideoCacheEntry.COLUMN_TITLE)),
                cursor.getLong(cursor.getColumnIndex(VideoCacheContract.VideoCacheEntry.COLUMN_DURATION)),
                cursor.getString(cursor.getColumnIndex(VideoCacheContract.VideoCacheEntry.COLUMN_CONTENT_TYPE)),
                cursor.getString(cursor.getColumnIndex(VideoCacheContract.VideoCacheEntry.COLUMN_DATA_URL)),
                cursor.getInt(cursor.getColumnIndex(VideoCacheContract.VideoCacheEntry.COLUMN_STAR_RATING))
        );
    }
}
