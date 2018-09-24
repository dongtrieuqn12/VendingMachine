package com.felhr.serialportexample.Activity;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Ho Dong Trieu on 08/29/2018
 */
public class WorkerActivity extends AsyncTask<WorkerActivity.WorkerParams, Void, String> {

    static class WorkerParams {
        final MainActivity activity;
        String result;

        WorkerParams(MainActivity activity, String result) {
            this.result = result;
            this.activity = activity;
        }
    }

    StringBuilder stringBuilder;
    HttpURLConnection connection = null;
    String targetURL;
    String urlParameters;

    public WorkerActivity(String urlParameters) {
        this.urlParameters = urlParameters;
    }

    @Override
    protected String doInBackground(WorkerParams... workerParams) {
        try {
            URL url = new URL("https://id.kiotviet.vn/connect/token");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            //get response
            InputStream inputStream = connection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return String.valueOf(stringBuilder);
    }
}
