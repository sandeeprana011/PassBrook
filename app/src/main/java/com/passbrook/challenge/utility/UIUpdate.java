package com.passbrook.challenge.utility;

/**
 * Created by sandeeprana on 26/11/16.
 * License is only applicable to individuals and non-profits
 * and that any for-profit company must
 * purchase a different license, and create
 * a second commercial license of your
 * choosing for companies
 */
public class UIUpdate {
    private String message;
    private int progress;

    public UIUpdate(String message, int progress) {
        this.message = message;
        this.progress = progress;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
