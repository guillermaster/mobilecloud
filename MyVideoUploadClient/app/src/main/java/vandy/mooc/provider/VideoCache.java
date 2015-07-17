package vandy.mooc.provider;

import android.net.Uri;
import java.util.List;

import vandy.mooc.model.mediator.webdata.Video;

/**
 * Created by gpincay on 7/14/2015.
 */
public interface VideoCache<K, V> {
    /**
     * Gets the @a value from the cache at the designated @a key.
     *
     * @param id
     * @return value
     */
    V get(K id);

    /**
     * Put the video into the cache
     *
     * @param video
     */
    Uri put(V video);

    /**
     * Put more than one video into the cache
     *
     * @param videoList
     */
    int put(List<Video> videoList);

    /**
     * Removes the value associated with an id
     *
     * @param id
     */
    void remove(K id);

    /**
     * Get the size of the cache.
     *
     * @return size
     */
    int size();
}
