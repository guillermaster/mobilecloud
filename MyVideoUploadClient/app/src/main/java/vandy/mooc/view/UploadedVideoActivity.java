package vandy.mooc.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import vandy.mooc.R;
import vandy.mooc.common.GenericActivity;
import vandy.mooc.presenter.VideoOps;
import vandy.mooc.utils.Constants;
import vandy.mooc.view.ui.FloatingActionButton;
import vandy.mooc.view.ui.VideoAdapter;

public class UploadedVideoActivity extends GenericActivity<VideoOps.View, VideoOps>
                    implements VideoOps.View{

    private ImageView mVideoPreviewImage;

    private TextView mVideoTitle;

    private FloatingActionButton mDownloadVideoButton;

    private long videoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_uploaded_video);

        // get reference to UI elements
        mVideoPreviewImage = (ImageView) findViewById(R.id.videoPreview);
        mVideoTitle = (TextView) findViewById(R.id.videoTitle);
        mDownloadVideoButton = (FloatingActionButton) findViewById(R.id.fabDownloadVideoButton);

        videoId = getIntent().getLongExtra(Constants.EXTRA_VIDEO_ID, 0);

        super.onCreate(savedInstanceState, VideoOps.class, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_uploaded_video, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setAdapter(VideoAdapter videoAdapter){

    }
}
