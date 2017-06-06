package com.rayh.maxmy.ray7challange;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Maxmya on 19/08/2016.
 */
public class DownloadTask extends AsyncTask<String, Void, String> {

    private ProgressDialog progressDialog;
    private Context activity;
    private String address = "";
    private PlaceAutocompleteFragment view;


    public DownloadTask(Context activity, PlaceAutocompleteFragment view) {

        this.activity = activity;
        this.view = view;
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Getting Location Address .. ");


    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();

    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        progressDialog.dismiss();
        view.setText(s);
    }


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

            JSONObject jsonObjectLocation =
                    new JSONObject(result).getJSONArray("results").getJSONObject(0);
            address = jsonObjectLocation.getString("formatted_address");

            return address;


        } catch (Exception e) {


            return "Error";

        }


    }
}
