package com.example.ambulanceservice;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import static android.content.Context.MODE_PRIVATE;

public class BackgroundCancelCab extends AsyncTask<String, Void, String> {

    Context context;
    AlertDialog alertDialog;

    BackgroundCancelCab()
    {

    }

    BackgroundCancelCab(Context c){
        context=c;
    }


    @Override
    protected String doInBackground(String... strings) {



        String login_url= context.getResources().getString(R.string.server_url)+"/cancel_book_cab.php";

        String ride_id,cab_id;
        ride_id=strings[0];
        cab_id=strings[1];

        try{
            URL url = new URL(login_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("ride_id", "UTF-8") + "=" + URLEncoder.encode(ride_id, "UTF-8")
                    + "&&" +URLEncoder.encode("cab_id", "UTF-8")+"="+URLEncoder.encode(cab_id, "UTF-8");

            bufferedWriter.write(post_data);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
            String result = "";
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }
            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();
            JSONObject response_data = new JSONObject(result);

            String final_result;

            if (response_data.getString("status").equals("1")) {

                 final_result="Booking canceled";


            }else
            {
                final_result="Unable to cancel ride";
            }

            return final_result;

        }catch(Exception e){
            Log.e("In catch of cancel","not able to cancel cab");

        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.e("Herere", "cab cancelled send");
        if(s.equals("Booking canceled"))
        {
//            ExampleBottomSheetDialog.resetDetails();
//            alertDialog=new AlertDialog.Builder(context).create();
//            alertDialog.setTitle("Booking canceled successfully");
            SharedPreferences sh = context.getSharedPreferences("MySharedPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = sh.edit();
            editor.remove("ride_started");
            editor.remove("driver_phone");
            editor.remove("driver_lat");
            editor.remove("driver_lng");
            editor.remove("ride_id");
            editor.remove("cab_id");
            MainActivity.source_pt=null;
            MainActivity.destination_pt=null;
            ExampleBottomSheetDialog.resetDetails();
            editor.commit();

            Log.e("cancel cab","success");
        }
        else
        {
//            alertDialog=new AlertDialog.Builder(context).create();
//            alertDialog.setTitle("Unable to cancel booking");
            Log.e("cancel cab","not success");
        }


    }
}
