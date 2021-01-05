package saldev40.android.flickrbrowser;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.ClosedByInterruptException;

enum DownloadStatus {IDLE, PROCESSING, NOT_INITIALIZED, FAILED_OR_EMPTY, OK};

public class GetRawData extends AsyncTask<String, Void, String> {

    interface OnDownloadComplete {
        void onDownloadComplete(String data, DownloadStatus status);
    }
    private static final String TAG = "GetRawData";
    private DownloadStatus downloadStatus;
    private final OnDownloadComplete callback;

    public GetRawData(OnDownloadComplete callback) {
        this.downloadStatus = DownloadStatus.IDLE;
        this.callback = callback;
    }

    /**
     *
     * @param s = called when GetFlickrJson wants to also parse JSON
     *          in background thread, it wont allow this  class to run
     *          again on a seperate backgroud thread since GetFLickrJson
     *          will be already on a background Thread, so we call these functions
     *          manually and return the data to GetFlickrJson which will
     *          Async parse the data, if we dont call execute() it wont create
     *          background thread so we are using these functions like a regular class
     */

    public void runOnSameThread(String s) {
//        onPostExecute(doInBackground(s));
        if(callback != null){
            callback.onDownloadComplete(doInBackground(s), downloadStatus);
        }
    }


    @Override
    protected void onPostExecute(String s) {
        if (callback != null) {
            callback.onDownloadComplete(s, downloadStatus);
        }
    }

    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader = null;
        if (strings == null) {
            downloadStatus = DownloadStatus.NOT_INITIALIZED;
            return null;
        }
        try {
            downloadStatus = DownloadStatus.PROCESSING;
            URL url = new URL(strings[0]);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            StringBuilder result = new StringBuilder();
            bufferedReader
                    = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String line;
            while (null != (line = bufferedReader.readLine())) {
                result.append(line).append("\n");
            }
            downloadStatus = DownloadStatus.OK;
            return result.toString();
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: " + e.getMessage());
        } finally {
            //if error is thrown, make sure to close connection and reader
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException closing bufferedReader: " + e.getMessage());
                }
            }
        }

        downloadStatus = DownloadStatus.FAILED_OR_EMPTY;
        return null;
    }

}
