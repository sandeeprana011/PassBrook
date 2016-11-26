package com.passbrook.challenge.utility;

import android.webkit.MimeTypeMap;

/**
 * Created by sandeeprana on 27/11/16.
 * License is only applicable to individuals and non-profits
 * and that any for-profit company must
 * purchase a different license, and create
 * a second commercial license of your
 * choosing for companies
 */

public class Utility {

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        } else {
            type = "application/octet-stream";
        }
        return type == null ? "application/octet-stream" : type;
    }
}
