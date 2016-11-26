package com.passbrook.challenge;


import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.passbrook.challenge.dialogs.DialogCreateFolder;
import com.passbrook.challenge.interfaces.OnCreateButtonClicked;
import com.passbrook.challenge.interfaces.OnUpdateUI;
import com.passbrook.challenge.utility.AsyncImageGet;
import com.passbrook.challenge.utility.UIUpdate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnCreateButtonClicked, OnUpdateUI, ResultCallback<DriveFolder.DriveFileResult> {

    private static final String TAG = "MAINACTIVITY";
    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 12;
    TextView textHomeWarning;
    Button buttonOpenDialogCreateFolder;
    ResultCallback<DriveFolder.DriveFolderResult> folderCreatedCallback = new
            ResultCallback<DriveFolder.DriveFolderResult>() {
                @Override
                public void onResult(DriveFolder.DriveFolderResult result) {
                    Log.e("FOLDERRESULT", "got result");
                    if (!result.getStatus().isSuccess()) {
//                        showMessage("Error while trying to create the folder");
                        Log.e("FOLDERRESULT", "Error : creating folder");
                        Snackbar.make(textHomeWarning.getRootView(), "Error: creating folder" + result.getDriveFolder().getDriveId(), Snackbar.LENGTH_SHORT);
                        buttonOpenDialogCreateFolder.setVisibility(View.VISIBLE);
                        buttonOpenDialogCreateFolder.setEnabled(true);
                        return;
                    }

                    buttonOpenDialogCreateFolder.setVisibility(View.GONE);
                    Snackbar.make(textHomeWarning.getRootView(), "Created a folder: " + result.getDriveFolder().getDriveId(), Snackbar.LENGTH_SHORT);
                    Log.e("FOLDERRESULT", "Folder created!" + result.getDriveFolder());

                    uploadRandomImage(result);
                }
            };
    ResultCallback<DriveApi.DriveContentsResult> fileDownloadedCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        // display an error saying file can't be opened
                        return;
                    }
                    // DriveContents object contains pointers
                    // to the actual byte stream
                    DriveContents contents = result.getDriveContents();

                    loadImageFromStream(contents.getInputStream());


                }
            };
    private String filePath;
    private GoogleApiClient mGoogleApiClient;
    ResultCallback<DriveApi.DriveContentsResult> contentsOpenedCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {

                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        // display an error saying file can't be opened
                        Log.e("Failed", "Success for what");
                        return;
                    }
                    // DriveContents object contains pointers
                    // to the actual byte stream
                    DriveContents contents = result.getDriveContents();

                    Log.e("Success", "Writing to file");
                    File file = new File(filePath);
                    try {
                        ParcelFileDescriptor parcelFileDescriptor = contents.getParcelFileDescriptor();

                        FileInputStream fileInputStream = new FileInputStream(file);

                        FileOutputStream fileOutputStream = new FileOutputStream(parcelFileDescriptor
                                .getFileDescriptor());

                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = fileInputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, len);
                        }
                        // Append to the file.

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(file.getName())
                            .setMimeType("image/jpeg").build();
                    contents.commit(mGoogleApiClient, changeSet);


                }
            };
    private SharedPreferences preferences;

    private void loadImageFromStream(InputStream inputStream) {

    }

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textHomeWarning = (TextView) findViewById(R.id.t_warning_home);
        buttonOpenDialogCreateFolder = (Button) findViewById(R.id.b_create_folder);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        String folderName = preferences.getString(Constants.FOLDER_NAME, null);
        String fileName = preferences.getString(Constants.FILE_NAME, null);
        String fileId = preferences.getString(Constants.DRIVE_FILE_ID, null);
        if (folderName != null && !folderName.equals("") && fileName != null && !fileName.equals("")) {
//            buttonOpenDialogCreateFolder.setVisibility(View.GONE);
//            textHomeWarning.setVisibility(View.GONE);
            DriveId sFileId = DriveId.decodeFromString(fileId);
            DriveFile file = Drive.DriveApi.getFile(mGoogleApiClient, sFileId);

            file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                    .setResultCallback(fileDownloadedCallback);


        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        Snackbar.make(null, R.string.error_connection_failed, Snackbar.LENGTH_LONG).show();
        Log.e(TAG, "Connected!");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "Connection suspended!");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        //If not logged in
        textHomeWarning.setVisibility(View.VISIBLE);

        if (connectionResult.hasResolution()) {
            try {
                textHomeWarning.setVisibility(View.GONE);
                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
                Log.e(TAG, "Connection result resolution");
            } catch (IntentSender.SendIntentException e) {
                Snackbar.make(getCurrentFocus().getRootView(), R.string.error_connection_failed, Snackbar.LENGTH_LONG).show();
                Log.e(TAG, "OnIntent Sender Error");
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
            Log.e(TAG, "Else Error");
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case RESOLVE_CONNECTION_REQUEST_CODE:
                Log.e(TAG, "OnActivity Result");
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                } else if (resultCode == RESULT_CANCELED) {
                    textHomeWarning.setVisibility(View.VISIBLE);
                    buttonOpenDialogCreateFolder.setVisibility(View.GONE);
                }
                break;
        }
    }

    public void openDialogCreateAndUpload(View view) {
        //View Disabled after first Click
        if (view != null) view.setEnabled(false);

        /**
         * Code to open the dialog window
         */
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogCreateFolder newFragment = new DialogCreateFolder();
        newFragment.setListener(this);
        newFragment.show(ft, "dialog");
    }

    @Override
    public void onCreateButtonClicked(String folderName) {
        Log.e("FOLDER", folderName + "");

        preferences.edit().putString(Constants.FOLDER_NAME, folderName).apply();

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(folderName).build();
        Drive.DriveApi.getRootFolder(mGoogleApiClient).createFolder(mGoogleApiClient, changeSet)
                .setResultCallback(folderCreatedCallback);
    }

    private void uploadRandomImage(DriveFolder.DriveFolderResult result) {
        textHomeWarning.setVisibility(View.VISIBLE);
        AsyncImageGet asyncImageGet = new AsyncImageGet(this, result);
        asyncImageGet.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
    }

    @Override
    public void onUpdateUI(UIUpdate uiUpdate) {
        if (uiUpdate != null) {
            Log.e("UIUPDATE", uiUpdate.getMessage() + String.valueOf(uiUpdate.getProgress()) + "%");
            textHomeWarning.setText(uiUpdate.getMessage() + "\n" + String.valueOf(uiUpdate.getProgress()) + "%");
        }

    }

    @Override
    public void imageLoadingFinished(String pathToImage, DriveFolder.DriveFolderResult result) {
        textHomeWarning.setText("Image Uploaded Successfully!");

        this.filePath = pathToImage;

        File file = new File(pathToImage);

        //saving file name to get after storing the image
        preferences.edit().putString(Constants.FILE_NAME, file.getName()).apply();

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(file.getName())
                .setMimeType("image/jpeg").build();
        // Create a file in the root folder
        result.getDriveFolder()
                .createFile(mGoogleApiClient, changeSet, null)
                .setResultCallback(this);

        Log.e("PATHTOIMAGE", pathToImage + "");
    }

    @Override
    public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {

        Log.e("ONRESULT", "OVERclocked");

        driveFileResult.getDriveFile().open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, new DriveFile.DownloadProgressListener() {
            @Override
            public void onProgress(long l, long l1) {
                Log.e("PROGRESS", String.format("First l : %d , Second l1 : %d", l, l1));
            }
        })
                .setResultCallback(contentsOpenedCallback);

        preferences.edit().putString(Constants.DRIVE_FILE_ID, driveFileResult.getDriveFile().getDriveId().encodeToString()).apply();
    }

    public void queryFile() {

        String fileName = preferences.getString(Constants.FILE_NAME, null);
        String folderName = preferences.getString(Constants.FOLDER_NAME, null);

    }

}
