package com.felhr.serialportexample.API;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.felhr.serialportexample.Others.Constants;
import com.felhr.serialportexample.Activity.MainActivity;

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
 * Created by Ho Dong Trieu on 09/17/2018
 */
public class SaleTransaction extends AsyncTask<Void, Void, String> {

    String param,accesstoken;
    private MainActivity activity;

    public SaleTransaction(String param, String accesstoken, MainActivity activity){
        this.param = param;
        this.accesstoken = accesstoken;
        this.activity = activity;
    }

    @Override
    protected String doInBackground(Void... voids) {
        StringBuilder stringBuilder;
        try {
            URL url = new URL(Constants.URL_MCP);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Authorization",accesstoken);
            httpURLConnection.setRequestProperty("Content-Type","application/json");

            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoOutput(true);

            DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
            dataOutputStream.writeBytes(param);
            dataOutputStream.flush();
            dataOutputStream.close();

            InputStream inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine())!= null){
                stringBuilder.append(line).append("\n");
            }

        } catch (IOException e) {
            return "";
        }

        return stringBuilder.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        if(s.equals("")){
            activity.databaseHelperTransaction.AddTransaction(activity.transactionModel);
        }else {
            try {
                JSONObject jsonObject = new JSONObject(s);
                String responseCode = jsonObject.getString("responseCode");
                switch (responseCode){
                    case "00":
                        activity.transactionModel.setStatus(Constants.IS_SYNC);
                        activity.databaseHelperTransaction.AddTransaction(activity.transactionModel);
                        Toast.makeText(activity,"giao dịch thành công",Toast.LENGTH_SHORT).show();
                        break;
                    case "96":
                        activity.databaseHelperTransaction.AddTransaction(activity.transactionModel);
                        Toast.makeText(activity,"giao dịch thất bại",Toast.LENGTH_SHORT).show();
                        break;
                    case "05":
                        activity.databaseHelperTransaction.AddTransaction(activity.transactionModel);
                        Toast.makeText(activity,"giao dịch thất bại",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        activity.databaseHelperTransaction.AddTransaction(activity.transactionModel);
                        Toast.makeText(activity,"giao dịch thất bại",Toast.LENGTH_SHORT).show();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
