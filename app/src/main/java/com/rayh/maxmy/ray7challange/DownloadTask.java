package com.rayh.maxmy.ray7challange;


import android.os.AsyncTask;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Maxmya on 19/08/2016.
 */
public class DownloadTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... urls) {

        String result = "";
        URL url;
        HttpURLConnection urlConnection = null;


        try {

            url = new URL(urls[0]);

            urlConnection = (HttpURLConnection) url.openConnection();

            InputStream in = urlConnection.getInputStream();

            InputStreamReader reader = new InputStreamReader(in);

            int data = reader.read();

            while (data != -1) {

                char currentChar = (char) data;

                result += currentChar;

                data = reader.read();
            }

            return result;

        } catch (Exception e) {

            e.printStackTrace();

            return "Error";

        }


    }
}
