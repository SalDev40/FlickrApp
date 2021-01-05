package saldev40.android.flickrbrowser;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements GetFlickrJsonData.OnDataAvailable {

    private static final String TAG = "MainActivity";
    private GetFlickrJsonData getFlickrJsonData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFlickrJsonData = new GetFlickrJsonData(
                "https://www.flickr.com/services/feeds/photos_public.gne",
                "en-us",
                true,
                this
        );
//        getFlickrJsonData.executeOnSameThread("android, nougat");

        //will run ASYNC
        getFlickrJsonData.execute("android, nougat");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     *
     * @param data = list of photos
     * @param status = status of download
     * - called  by OnDownloadComplete() from GetFlickrJsonData
     */
    @Override
    public void onDataAvailable(List<Photo> data,
                                DownloadStatus status) {

        if (status == DownloadStatus.OK) {
            Log.d(TAG, "onDataAvailable: statusOK" + data.get(0).toString());
        } else {
            Log.e(TAG, "onDataAvailable: error downloading" + status);
        }
    }
}