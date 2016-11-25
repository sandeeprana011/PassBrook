package com.passbrook.challenge.utility;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.gms.drive.DriveFolder;
import com.passbrook.challenge.interfaces.OnUpdateUI;

/**
 * Created by sandeeprana on 26/11/16.
 * License is only applicable to individuals and non-profits
 * and that any for-profit company must
 * purchase a different license, and create
 * a second commercial license of your
 * choosing for companies
 *
 * @link{AsyncImageGet} help in getting all images and manipulation some random path
 */

public class AsyncImageGet extends AsyncTask<Context, UIUpdate, String> {


    private OnUpdateUI onUpdateUIListener;
    private DriveFolder.DriveFolderResult result;

    public AsyncImageGet(OnUpdateUI onUpdateUIListener, DriveFolder.DriveFolderResult result) {
        this.onUpdateUIListener = onUpdateUIListener;
        this.result = result;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Context... contexts) {
        publishProgress(new UIUpdate("Creating resolver", 20));

        ContentResolver cr = contexts[0].getContentResolver();
        String[] columns = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.TITLE,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.MIME_TYPE,
                MediaStore.Images.ImageColumns.SIZE};
        publishProgress(new UIUpdate("Starting Query", 40));
        Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns, null, null, "RANDOM()");
        /**                             ^
         * Random function here made paths random so can pick first index path
         */

        publishProgress(new UIUpdate("Query Completed", 80));
        int image_path_index = cur.getColumnIndex(MediaStore.Images.Media.DATA);

        publishProgress(new UIUpdate("Getting Random Imgae", 100));
        if (!cur.moveToFirst()) {
            cur.close();
            return null;
        }

        String pathToImage = cur.getString(image_path_index);
        Log.e("IMAGEPATH", pathToImage);

        cur.close();

        return pathToImage;
    }

    @Override
    protected void onProgressUpdate(UIUpdate... values) {
        super.onProgressUpdate(values);
        if (onUpdateUIListener != null && values.length > 0) {
            this.onUpdateUIListener.onUpdateUI(values[0]);
        }

    }

    @Override
    protected void onPostExecute(String pathToImage) {
        super.onPostExecute(pathToImage);
        if (onUpdateUIListener != null) {
            this.onUpdateUIListener.imageLoadingFinished(pathToImage, result);
        }
    }
}

