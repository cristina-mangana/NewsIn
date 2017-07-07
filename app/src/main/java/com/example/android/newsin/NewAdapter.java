package com.example.android.newsin;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.example.android.newsin.MainActivity.LOG_TAG;

/**
 * Created by Cristina on 06/07/2017.
 * {@link NewAdapter} is an {@link ArrayAdapter} that can provide the layout for each list
 * based on a data source, which is a list of {@link New} objects.
 */

public class NewAdapter extends ArrayAdapter<New> {

    /**
     * Custom constructor.
     *
     * @param context The current context. Used to inflate the layout file.
     * @param news  A List of New objects to display in a list
     */
    public NewAdapter(Activity context, List<New> news) {
        super(context, 0, news);
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position    The position in the list of data that should be displayed in the
     *                    list item view.
     * @param convertView The recycled view to populate.
     * @param parent      The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        NewViewHolder holder; // to reference the child views for later actions
        // Check if the existing view is being reused, otherwise inflate the view
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item,
                    parent, false);
            // cache view fields into the holder
            holder = new NewViewHolder(listItemView);
            // associate the holder with the view for later lookup
            listItemView.setTag(holder);
        } else {
            // view already exists, get the holder instance from the view
            holder = (NewViewHolder) listItemView.getTag();
        }

        // Get the {@link New} object located at this position in the list
        New currentNew = getItem(position);

        // Get the imageLink to download the image
        String imageLink = currentNew.getImageLink();
        // Download the image and attach it to the ImageView. If imageLink is null, no image is
        // assign to that new or is not recognized as such. Then, a default image is assigned.
        if (imageLink != null && imageLink.length() > 0) {
            Picasso.with(getContext()).load(imageLink).into(holder.newImageView);
        } else {
            Picasso.with(getContext()).load(R.drawable.default_image).into(holder.newImageView);
        }

        // Get the new date and set as text for the TextView
        // Format the date string (i.e. "Mar 3, 1984")
        String formattedDate = formatDate(currentNew.getDate());
        holder.textViewDate.setText(formattedDate);

        // Get the new subject and set as text for the TextView
        String subject = currentNew.getSubject();
        if (subject.length() > 18) {
            subject = subject.substring(0, 16) + "...";
        }
        holder.textViewSubject.setText(subject);

        // Get the new title and set as text for the TextView
        holder.textViewTitle.setText(currentNew.getTitle());

        // Get the new text and set as text for the TextView
        holder.textViewText.setText(currentNew.getText());

        // Return the whole list item layout so that it can be shown in the ListView
        return listItemView;
    }

    /**
     * Provide a reference to the views for each data item
     */
    public static class NewViewHolder {

        ImageView newImageView;
        TextView textViewDate;
        TextView textViewSubject;
        TextView textViewTitle;
        TextView textViewText;

        public NewViewHolder(View itemView) {

            newImageView = (ImageView) itemView.findViewById(R.id.new_image);
            textViewDate = (TextView) itemView.findViewById(R.id.new_date);
            textViewSubject = (TextView) itemView.findViewById(R.id.new_subject);
            textViewTitle = (TextView) itemView.findViewById(R.id.new_title);
            textViewText = (TextView) itemView.findViewById(R.id.new_text);
        }
    }

    /**
     * Return the formatted date string (i.e. "23 Apr, 2017") from a String.
     * https://stackoverflow.com/questions/31060010/convert-string-to-a-formatted-date-in-android
     */
    private String formatDate(String date) {
        //Convert the string date to a date object
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(date);
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Error parsing date");
        }

        //Format date
        SimpleDateFormat df = new SimpleDateFormat("dd LLL, yyyy");
        return df.format(convertedDate);
    }
}
