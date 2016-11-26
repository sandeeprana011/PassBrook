package com.passbrook.challenge;


import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Query;
import com.passbrook.challenge.dialogs.DialogCreateFolder;
import com.passbrook.challenge.interfaces.OnCreateButtonClicked;
import com.passbrook.challenge.interfaces.OnUpdateUI;
import com.passbrook.challenge.utility.AsyncImageGet;
import com.passbrook.challenge.utility.UIUpdate;
import com.passbrook.challenge.utility.Utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnCreateButtonClicked, OnUpdateUI, ResultCallback<DriveFolder.DriveFileResult>, View.OnClickListener {

    private static final String TAG = "MAINACTIVITY";
    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 12;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 43;
    private static final int PICKFILE_REQUEST_CODE = 34;
    TextView textHomeWarning;
    Button buttonOpenDialogCreateFolder, uploadRandomPhoto;

    private String filePath;
    private GoogleApiClient mGoogleApiClient;
    private ListView folderListView;
    private SharedPreferences preferences;
    //    private DriveFolder resultDriveFolder;
    ResultCallback<DriveFolder.DriveFolderResult> folderCreatedCallback = new
            ResultCallback<DriveFolder.DriveFolderResult>() {
                @Override
                public void onResult(DriveFolder.DriveFolderResult result) {
                    Log.e("FOLDERRESULT", "got resultDriveFolder");
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

                    result.getDriveFolder().getDriveId();
                    preferences.edit().putString(Constants.DRIVE_FOLDER_ID, result.getDriveFolder().getDriveId().encodeToString()).apply();

                    uploadRandomImage(result.getDriveFolder());
                }
            };
    private ImageView imageViewThumbnail;
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
    private SignInButton signInGoogle;
    private ConnectionResult connectionResult;
    private FolderListAdapter folderListAdapter;
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
                            .setMimeType("image/*").build();
                    contents.commit(mGoogleApiClient, changeSet);

                    uploadRandomPhoto.setEnabled(true);
                    executeIfWeAreGoodToGoAfterAllRefreshUI();

                }
            };

    private void loadImageFromStream(InputStream inputStream) {
        try {
            imageViewThumbnail.setImageBitmap(BitmapFactory.decodeStream(inputStream));
        } catch (OutOfMemoryError ex) {
            textHomeWarning.setVisibility(View.VISIBLE);
            textHomeWarning.setText("Image is too big to render. Need extra code to handle. Known error.");
            buttonOpenDialogCreateFolder.setVisibility(View.VISIBLE);
            Log.e("Image", "Out of memory");
        }

    }

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textHomeWarning = (TextView) findViewById(R.id.t_warning_home);
        buttonOpenDialogCreateFolder = (Button) findViewById(R.id.b_create_folder);
        imageViewThumbnail = (ImageView) findViewById(R.id.image_thumbnail_folder);
        signInGoogle = (SignInButton) findViewById(R.id.signingoogle);
        folderListView = (ListView) findViewById(R.id.list_folder_content);
        uploadRandomPhoto = (Button) findViewById(R.id.uploadmore);

        folderListAdapter = new FolderListAdapter(MainActivity.this);
        folderListView.setAdapter(folderListAdapter);

//        mResultsAdapter = new ResultsAdapter(this);
//        mResultsListView.setAdapter(mResultsAdapter);

        signInGoogle.setOnClickListener(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
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
        executeIfWeAreGoodToGoAfterAllRefreshUI();
    }

    private void executeIfWeAreGoodToGoAfterAllRefreshUI() {
        buttonOpenDialogCreateFolder.setVisibility(View.VISIBLE);
        Log.e(TAG, "Connected!");
        String folderName = preferences.getString(Constants.FOLDER_NAME, null);
        String fileName = preferences.getString(Constants.FILE_NAME, null);
        String fileId = preferences.getString(Constants.DRIVE_FILE_ID, null);
        String folderId = preferences.getString(Constants.DRIVE_FOLDER_ID, null);

        if (folderName != null && !folderName.equals("") && fileName != null && !fileName.equals("")) {
//            buttonOpenDialogCreateFolder.setVisibility(View.GONE);
//            textHomeWarning.setVisibility(View.GONE);

            final ResultCallback<? super DriveApi.MetadataBufferResult> folderListCallback = new ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                    if (!metadataBufferResult.getStatus().isSuccess()) {
                        textHomeWarning.setText("Problem while retrieving files");
                        return;
                    }
                    folderListAdapter.clear();
                    Log.e("Meta Data Buf Size : ", String.valueOf(metadataBufferResult.getMetadataBuffer().getCount()));
                    folderListAdapter.append(metadataBufferResult.getMetadataBuffer());

                }
            };


            DriveId sFolderId = DriveId.decodeFromString(folderId);

            final DriveFolder folder = sFolderId.asDriveFolder();
//            folder.listChildren(mGoogleApiClient).setResultCallback(folderListCallback);

//            Log.e("FOLDERID", sFolderId.encodeToString());
            Drive.DriveApi.requestSync(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    Log.e("STATUS", status.toString());

                    Query query = new Query.Builder()
//                    .addFilter(Filters.eq(SearchableField.MIME_TYPE, "image/*"))
                            .build();
                    folder.queryChildren(mGoogleApiClient, query).setResultCallback(folderListCallback);

                    uploadRandomPhoto.setEnabled(true);
                    uploadRandomPhoto.setVisibility(View.VISIBLE);

                    uploadRandomPhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            view.setEnabled(false);
                            uploadRandomImage(folder);
                        }
                    });

//                    Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(folderListCallback);

                }
            });
//
//            DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient, sFolderId);

//            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, DriveScopes.DRIVE);
//            credential.setSelectedAccountName(accountName);
//            Drive service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).build();

            //            folder.queryChildren(mGoogleApiClient,query).setResultCallback(folderListCallback);

//            folder.listChildren(mGoogleApiClient).setResultCallback(folderListCallback);


//            DriveId sFileId = DriveId.decodeFromString(fileId);
//            Log.e("FILEID", sFileId.encodeToString());
//            DriveFile file = Drive.DriveApi.getFile(mGoogleApiClient, sFileId);
//
//            file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
//                    .setResultCallback(fileDownloadedCallback);
            buttonOpenDialogCreateFolder.setVisibility(View.GONE);

        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "Connection suspended!");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        //If not logged in
        textHomeWarning.setVisibility(View.VISIBLE);
        buttonOpenDialogCreateFolder.setVisibility(View.GONE);

        this.connectionResult = connectionResult;
        if (connectionResult.hasResolution()) {
            signInGoogle.setEnabled(true);
            signInGoogle.setVisibility(View.VISIBLE);
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
            case PICKFILE_REQUEST_CODE:
                if (data == null) return;
                String fPathResulted = data.getDataString();
                if (fPathResulted != null) {
                    Log.e("FILE PATH", fPathResulted);

//                this.resultDriveFolder = getDriverFolder();
                    this.imageLoadingFinished(fPathResulted);

                }
                break;
        }
    }

    private DriveFolder getDriverFolder() {
        String folderId = preferences.getString(Constants.DRIVE_FOLDER_ID, null);
        DriveId driveId = DriveId.decodeFromString(folderId);
        DriveFolder folder = driveId.asDriveFolder();
        return folder;
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

    private void uploadRandomImage(DriveFolder driveFolder) {

//        this.resultDriveFolder = this.getDriverFolder();
        checkCompatibilityRunCursorAndUploadRandomImage();


    }

    @Override
    public void onUpdateUI(UIUpdate uiUpdate) {
        if (uiUpdate != null) {
            Log.e("UIUPDATE", uiUpdate.getMessage() + String.valueOf(uiUpdate.getProgress()) + "%");
            textHomeWarning.setText(uiUpdate.getMessage() + "\n" + String.valueOf(uiUpdate.getProgress()) + "%");
        }

    }

    @Override
    public void imageLoadingFinished(String pathToImage) {
        textHomeWarning.setText("Image Uploaded Successfully!");

        this.filePath = pathToImage;

        File file = new File(pathToImage);


        //saving file name to get after storing the image
        preferences.edit().putString(Constants.FILE_NAME, file.getName()).apply();

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(file.getName())
                .setMimeType(Utility.getMimeType(pathToImage)).build();
        Log.e("MIMETYPE", Utility.getMimeType(pathToImage));
        // Create a file in the root folder
        this.getDriverFolder().createFile(mGoogleApiClient, changeSet, null)
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

//    public void queryFile() {
//
//        String fileName = preferences.getString(Constants.FILE_NAME, null);
//        String folderName = preferences.getString(Constants.FOLDER_NAME, null);
//
//    }

    public void checkCompatibilityRunCursorAndUploadRandomImage() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Log.e("This block", "1");
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

            } else {

                // No explanation needed, we can request the permission.
                Log.e("This block", "2");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // resultDriveFolder of the request.
            }
        } else {
            Log.e("Some", "Permission granted");
            textHomeWarning.setVisibility(View.VISIBLE);
            AsyncImageGet asyncImageGet = new AsyncImageGet(this);
            asyncImageGet.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the resultDriveFolder arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    textHomeWarning.setVisibility(View.VISIBLE);
                    AsyncImageGet asyncImageGet = new AsyncImageGet(this);
                    asyncImageGet.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);

                } else {
                    textHomeWarning.setVisibility(View.VISIBLE);
                    textHomeWarning.setText("Permission required!");
                    buttonOpenDialogCreateFolder.setVisibility(View.GONE);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onClick(View view) {
        view.setEnabled(false);
        if (this.connectionResult == null) {
            return;
        }
        try {
            textHomeWarning.setVisibility(View.GONE);
            this.connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
            Log.e(TAG, "Connection resultDriveFolder resolution");
        } catch (IntentSender.SendIntentException e) {
            Snackbar.make(getCurrentFocus().getRootView(), R.string.error_connection_failed, Snackbar.LENGTH_LONG).show();
            Log.e(TAG, "OnIntent Sender Error");
        }

    }

    public void activityBuilder(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICKFILE_REQUEST_CODE);
    }
}
