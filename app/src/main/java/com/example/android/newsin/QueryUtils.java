package com.example.android.newsin;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.example.android.newsin.MainActivity.LOG_TAG;

/**
 * Created by Cristina on 06/07/2017.
 * Helper methods related to requesting and receiving news data from The Guardian API.
 */

public final class QueryUtils {

    private static final String EMPTY_STRING = "";
    private static final String DATE_SEPARATOR = "T";

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the The Guardian dataset and return a List of {@link New}.
     */
    public static List<New> fetchNewsData(String requestUrl) {

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request", e);
        }

        // Extract relevant fields from the JSON response and create an List<New> object
        List<New> news = extractFeatureFromJson(jsonResponse);

        // Return the List
        return news;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,
                    Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link New} objects that has been built up from parsing a JSON response.
     */
    private static List<New> extractFeatureFromJson(String newJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(newJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding news to
        List<New> news = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(newJSON);
            if (baseJsonResponse.has("response")) {
                JSONObject response = baseJsonResponse.getJSONObject("response");
                if (response.has("results")) {
                    JSONArray results = response.getJSONArray("results");
                    // Loop through each feature in the array
                    for (int i = 0; i < results.length(); i++) {
                        // Get New JSONObject at position i
                        JSONObject singleNew = results.getJSONObject(i);

                        // Extract "webTitle" for title
                        String title = EMPTY_STRING;
                        if (singleNew.has("webTitle")) {
                            title = singleNew.getString("webTitle");
                        }

                        // Create date from "webPublicationDate" (published date is returned in
                        // format yyyy-mm-ddThh:mm:ssZ)
                        String date = EMPTY_STRING;
                        if (singleNew.has("webPublicationDate")) {
                            String publishedDate = singleNew.getString("webPublicationDate");
                            if (publishedDate.contains(DATE_SEPARATOR)) {
                                String[] parts = publishedDate.split(DATE_SEPARATOR);
                                date = parts[0];
                            } else {
                                date = publishedDate;
                            }
                        }
                        // Extract "sectionName" for subject
                        String subject = EMPTY_STRING;
                        if (singleNew.has("sectionName")) {
                            subject = singleNew.getString("sectionName");
                        }

                        // Extract "webUrl" for newUrl
                        String newUrl = EMPTY_STRING;
                        if (singleNew.has("webUrl")) {
                            newUrl = singleNew.getString("webUrl");
                        }

                        // Extract additional fields
                        String text = EMPTY_STRING;
                        String imageLink = EMPTY_STRING;
                        if (singleNew.has("fields")) {
                            JSONObject fields = singleNew.getJSONObject("fields");
                            // Extract "thumbnail" for imageLink
                            if (fields.has("thumbnail")) {
                                imageLink = fields.getString("thumbnail");
                            }
                            // Extract "bodyText" for text
                            if (fields.has("bodyText")) {
                                text = fields.getString("bodyText");
                            }
                        }

                        // Create {@link New} java object
                        New parsedNew = new New(title, text, date, subject, imageLink, newUrl);
                        // Add {@link New} to list of news
                        news.add(parsedNew);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the JSON results", e);
        }
        // Return the list of news
        return news;
    }
}
