package com.passbrook.challenge;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.widget.DataBufferAdapter;

/**
 * Created by sandeeprana on 26/11/16.
 * License is only applicable to individuals and non-profits
 * and that any for-profit company must
 * purchase a different license, and create
 * a second commercial license of your
 * choosing for companies
 */


/**
 * A DataBufferAdapter to display the results of file listing/querying requests.
 */
public class FolderListAdapter extends DataBufferAdapter<Metadata> {

    public FolderListAdapter(Context context) {
        super(context, R.layout.layoutsimplewithimage);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getContext(),
                    R.layout.layoutsimplewithimage, null);
        }
        Metadata metadata = getItem(position);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageview_icon);
        String mime = metadata.getMimeType();
        if (mime.equals("image/jpeg") || metadata.getTitle().endsWith(".jpeg")) {
            imageView.setImageResource(R.drawable.jpg);
        } else if (mime.equals("image/jpg") || metadata.getTitle().endsWith(".jpg")) {
            imageView.setImageResource(R.drawable.jpg);
        } else if (mime.equals("image/doc") || metadata.getTitle().endsWith(".doc")) {
            imageView.setImageResource(R.drawable.doc);
        } else if (mime.equals("image/png") || metadata.getTitle().endsWith(".png")) {
            imageView.setImageResource(R.drawable.png);
        } else if (mime.equals("image/pdf") || metadata.getTitle().endsWith(".pdf")) {
            imageView.setImageResource(R.drawable.pdf);
        } else {
            imageView.setImageResource(R.drawable.unknown);
        }

        Log.e("POSITION", String.valueOf(position));
        TextView titleTextView =
                (TextView) convertView.findViewById(R.id.textview_title);
        titleTextView.setText(metadata.getTitle());

        return convertView;
    }


}
