package com.felhr.serialportexample.API;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import com.felhr.serialportexample.Activity.MainActivity;
import com.felhr.serialportexample.Others.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Ho Dong Trieu on 09/21/2018
 */

public class GetAccessToken extends AsyncTask<String,Void,String> {
    private StringBuilder stringBuilder;
    private HttpURLConnection connection = null;
    String targetURL;
    private String urlParameters;

    public GetAccessToken(String urlParameters){
        this.urlParameters = urlParameters;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            URL url = new URL("https://id.kiotviet.vn/connect/token");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            //send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush ();
            wr.close ();
            //get response
            InputStream inputStream = connection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine())!= null){
                stringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return String.valueOf(stringBuilder);
    }

    @Override
    protected void onPostExecute(String s) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(s);
            MainActivity.accessTokenKiot = jsonObject.getString("access_token");
            Log.d(Constants.TAG,MainActivity.accessTokenKiot);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
