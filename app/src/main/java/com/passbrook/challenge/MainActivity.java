package com.passbrook.challenge;


import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
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
import com.google.android.gms.drive.Drive;
import com.passbrook.challenge.dialogs.DialogCreateFolder;
import com.passbrook.challenge.dialogs.OnCreateButtonClicked;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnCreateButtonClicked {

    private static final String TAG = "MAINACTIVITY";
    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 12;
    TextView textHomeWarning;
    Button buttonOpenDialogCreateFolder;
    private GoogleApiClient mGoogleApiClient;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textHomeWarning = (TextView) findViewById(R.id.t_warning_home);
        buttonOpenDialogCreateFolder = (Button) findViewById(R.id.b_create_folder);

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
    }
}
