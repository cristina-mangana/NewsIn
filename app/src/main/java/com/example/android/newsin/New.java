package com.example.android.newsin;

/**
 * Created by Cristina on 06/07/2017.
 * This class represents a single new. Each object has information about the new, such as title,
 * text, date, subject and image link.
 */

public class New {

    private String mTitle, mText, mDate, mSubject, mImageLink, mNewUrl;

    /**
     * Create a new {@link New} object
     * @param title is the new title
     * @param text is the new text
     * @param date is the new date
     * @param subject is the new category, i.e. Culture
     * @param imageLink is the url to download the new image
     * @param newUrl is the website URL to read the hole new
     */
    public New(String title, String text, String date, String subject, String imageLink,
               String newUrl) {
        mTitle = title;
        mText = text;
        mDate = date;
        mSubject = subject;
        mImageLink = imageLink;
        mNewUrl = newUrl;
    }

    /**
     * Get the new title
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Get the new text
     */
    public String getText() {
        return mText;
    }

    /**
     * Get the new subject
     */
    public String getSubject() {
        return mSubject;
    }

    /**
     * Get the new date
     */
    public String getDate() {
        return mDate;
    }

    /**
     * Get the url to download the new image
     */
    public String getImageLink() {
        return mImageLink;
    }

    /**
     * Get the url to read the hole new
     */
    public String getNewUrl() {
        return mNewUrl;
    }
}
