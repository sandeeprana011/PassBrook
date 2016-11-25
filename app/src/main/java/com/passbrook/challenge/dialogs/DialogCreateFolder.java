package com.passbrook.challenge.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.passbrook.challenge.R;

/**
 * Created by sandeeprana on 26/11/16.
 * License is only applicable to individuals and non-profits
 * and that any for-profit company must
 * purchase a different license, and create
 * a second commercial license of your
 * choosing for companies
 */

public class DialogCreateFolder extends DialogFragment {
    private OnCreateButtonClicked onCreateButtonClicked;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.dialog_create_folder, null);
        final EditText editTextCreateFolder = (EditText) view.findViewById(R.id.e_createfolder_dialog);

        builder.setView(view);

        builder.setMessage("Create Folder")
                .setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//                        Log.e("DIALOGCREATEFOLDER", editTextCreateFolder.getText().toString());
                        if (onCreateButtonClicked != null) {
                            onCreateButtonClicked.onCreateButtonClicked(editTextCreateFolder.getText().toString());
                        }
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        
                    }
                });

        return builder.create();
    }

    void setListener(OnCreateButtonClicked onCreateButtonClicked) {
        this.onCreateButtonClicked = onCreateButtonClicked;
    }
}