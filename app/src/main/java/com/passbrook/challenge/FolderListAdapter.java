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

        switch (metadata.getMimeType()) {
            case "image/jpeg":
                imageView.setImageResource(R.drawable.jpg);
                break;
            case "image/jpg":
                imageView.setImageResource(R.drawable.jpg);
                break;
            case "image/doc*":
                imageView.setImageResource(R.drawable.doc);
                break;
            case "image/png":
                imageView.setImageResource(R.drawable.png);
                break;
            case "image/pdf":
                imageView.setImageResource(R.drawable.pdf);
                break;
            default:
                imageView.setImageResource(R.drawable.unknown);
                break;
        }

        Log.e("POSITION", String.valueOf(position));
        TextView titleTextView =
                (TextView) convertView.findViewById(R.id.textview_title);
        titleTextView.setText(metadata.getTitle());

        return convertView;
    }


}
