package com.passbrook.challenge.interfaces;

import com.google.android.gms.drive.DriveFolder;
import com.passbrook.challenge.utility.UIUpdate;

/**
 * Created by sandeeprana on 26/11/16.
 * License is only applicable to individuals and non-profits
 * and that any for-profit company must
 * purchase a different license, and create
 * a second commercial license of your
 * choosing for companies
 */

public interface OnUpdateUI {
    public void onUpdateUI(UIUpdate uiUpdate);

    public void imageLoadingFinished(String pathToImage, DriveFolder.DriveFolderResult result);
}
