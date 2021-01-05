package saldev40.android.flickrbrowser;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetFlickrJsonData
        extends AsyncTask<String, Void, List<Photo>>
        implements GetRawData.OnDownloadComplete {
    private static final String TAG = "GetFlickrJsonData";

    public interface OnDataAvailable {
        void onDataAvailable(List<Photo> data, DownloadStatus status);
    }

    public List<Photo> photoList;
    private String baseUrl;
    private String language;
    private boolean matchAll;
    private final OnDataAvailable callback;
    private boolean runningOnSameThread = false;

    public GetFlickrJsonData(String baseUrl,
                             String language,
                             boolean matchAll,
                             OnDataAvailable callback) {
        this.baseUrl = baseUrl;
        this.language = language;
        this.matchAll = matchAll;

        this.callback = callback;
        photoList = new ArrayList<>();
    }

    /**
     * @param data   - Raw JSON data downloaded async in a string fromat
     * @param status - download status for given URL
     *               <p>
     *               Is called by onPostExecute() in GetRawData once the data is done downloading
     */
    @Override
    public void onDownloadComplete(String data, DownloadStatus status) {
        if (status == DownloadStatus.OK) {
            Log.d(TAG, "onDownloadComplete: data: " + data);
            try {
                JSONObject jsonObject = new JSONObject(data);
                JSONArray itemsArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject jsonPhoto = itemsArray.getJSONObject(i);
                    JSONObject jsonMedia = jsonPhoto.getJSONObject("media");
                    String photoUrl = jsonMedia.getString("m");
                    // bigger image link
                    String link = photoUrl.replace("_m.", "_b.");

                    photoList.add(new Photo(
                            jsonPhoto.getString("title"),
                            jsonPhoto.getString("author"),
                            jsonPhoto.getString("author_id"),
                            link, jsonPhoto.getString("tags"),
                            photoUrl));
                }


            } catch (JSONException e) {
                Log.e(TAG, "JSON error" + e.getMessage());
                e.printStackTrace();
                status = DownloadStatus.FAILED_OR_EMPTY;
            }
        } else {
            status = DownloadStatus.FAILED_OR_EMPTY;
        }

        //only if we are running the data parsing on same thread
        // as main activity
        if (runningOnSameThread && callback != null) {
            callback.onDataAvailable(photoList, status);
        }

    }

    /**
     * @param searchCriteria - given tags to search by in URI
     *
     *     Function will run on same  thread as Main Activity and therefore
     *     not parse in background, can be bad for performance
     */

    public void executeOnSameThread(String searchCriteria) {
        String destinationUri = createUri(searchCriteria, language, matchAll);
        runningOnSameThread = true;
        Log.d(TAG, "executeOnSameThread: " + destinationUri);
        GetRawData getRawData = new GetRawData(this);
        getRawData.execute(destinationUri);
    }


    private String createUri(String searchCriteria, String language, boolean matchAll) {
        return Uri.parse(baseUrl).buildUpon()
                .appendQueryParameter("tags", searchCriteria)
                .appendQueryParameter("tagmode", matchAll ? "ALL" : "ANY")
                .appendQueryParameter("lang", language)
                .appendQueryParameter("format", "json")
                .appendQueryParameter("nojsoncallback", "1")
                .build().toString();
    }


    /**
     *
     * @param = url to download and run asynchronously
     * @return = downloaded and parsed photos
     *  Will allow us to parse the JSON downloaded in the background thread ASYNC
     *  - we must now run GetRawData on the same backgroundThread as this one
     *  and not a seperate background thread
     */
    @Override
    protected List<Photo> doInBackground(String... params) {
        String destinationUri = createUri(params[0], language, matchAll);
        GetRawData getRawData = new GetRawData(this);
        getRawData.runOnSameThread(destinationUri);
//         wont work, wont allow to run this and GetRawData in background thread
//        getRawData.execute(destinationUri);
        return photoList;
    }

    @Override
    protected void onPostExecute(List<Photo> photos) {
        // only if we are running JSON parsing in the background
        if (!runningOnSameThread &&  callback != null) {
            callback.onDataAvailable(photoList, DownloadStatus.OK);
        }
    }
}
