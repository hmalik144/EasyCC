package com.example.haimalik.myapplication;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;

import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    EditText currencyOneEditText;
    EditText currencyTwoEditText;
    Spinner spinnerTop;
    Spinner spinnerBottom;
    double conversionRateOne;
    ProgressBar spinner;

    private String URL = "https://free.currencyconverterapi.com/api/v3/convert?";

    private static final String LOG_TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currencyOneEditText = (EditText) findViewById(R.id.editText);
        currencyTwoEditText = (EditText) findViewById(R.id.editText2);

        currencyOneEditText.addTextChangedListener(TextWatcherClass);
        currencyTwoEditText.addTextChangedListener(TextWatcherClass2);

        spinner = (ProgressBar) findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        addListenerOnSpinnerItemSelection();

        String stringURL = UriBuilder();
        MyAsyncTask task = new MyAsyncTask();
        task.execute(stringURL);
    }


    private TextWatcher TextWatcherClass = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            currencyTwoEditText.removeTextChangedListener(TextWatcherClass2);
            if(currencyOneEditText.getText().toString().isEmpty()){
                currencyTwoEditText.setText("");
                return;
            }

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {

            try{

            Double topValue = Double.parseDouble(currencyOneEditText.getText().toString());
            Double bottomValue = topValue * conversionRateOne;
            DecimalFormat df = new DecimalFormat("#.##");
            bottomValue = Double.valueOf(df.format(bottomValue));
            currencyTwoEditText.setText(bottomValue.toString());


            }
            catch (NumberFormatException e){
                Log.e(LOG_TAG, "no numbers inserted");
            }
            currencyTwoEditText.addTextChangedListener(TextWatcherClass2);
        }

    };

    private TextWatcher TextWatcherClass2 = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            currencyOneEditText.removeTextChangedListener(TextWatcherClass);
            if(currencyTwoEditText.getText().toString().isEmpty()){
                currencyOneEditText.setText("");
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {

            try{

            Double bottomValue = Double.parseDouble(currencyTwoEditText.getText().toString());
            Double topValue = bottomValue * (1 / conversionRateOne);
            DecimalFormat df = new DecimalFormat("#.##");
            topValue = Double.valueOf(df.format(topValue));
            currencyOneEditText.setText(topValue.toString());


            }
            catch (NumberFormatException e){
                Log.e(LOG_TAG, "no numbers inserted");
            }
            currencyOneEditText.addTextChangedListener(TextWatcherClass);
        }

    };

    public void addListenerOnSpinnerItemSelection() {
        spinnerTop = (Spinner) findViewById(R.id.spinner1);
        spinnerTop.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                String currencyOne = parentView.getItemAtPosition(pos).toString();
                currencyTwoEditText.setText("");
                currencyOneEditText.setText("");
                currencyOneEditText.setHint("insert " + currencyOne.substring(0,3));

                String stringURL = UriBuilder();
                MyAsyncTask task = new MyAsyncTask();
                task.execute(stringURL);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        spinnerBottom = (Spinner) findViewById(R.id.spinner2);
        spinnerBottom.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                String currencyTwo = parentView.getItemAtPosition(pos).toString();
                currencyOneEditText.setText("");
                currencyTwoEditText.setText("");
                currencyTwoEditText.setHint("insert " + currencyTwo.substring(0,3));

                String stringURL = UriBuilder();
                MyAsyncTask task = new MyAsyncTask();
                task.execute(stringURL);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

    }

    private String UriBuilder(){
        String currencyOne = (spinnerTop.getSelectedItem().toString()).substring(0,3);
        String currencyTwo = (spinnerBottom.getSelectedItem().toString()).substring(0,3);

        Uri baseUri = Uri.parse(URL);
        Uri.Builder builder = baseUri.buildUpon();
        builder.appendQueryParameter("q", currencyOne + "_" + currencyTwo).appendQueryParameter("compact", "ultra");

        return builder.build().toString();

    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }


    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(30000);
            urlConnection.setConnectTimeout(30000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

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

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = "";
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        Log.d(LOG_TAG, output.toString());
        return output.toString();

    }

    private double extractFeatureFromJson(String newsJSON) {
        double conversionValue = 0.00;
        String currencyOne = spinnerTop.getSelectedItem().toString().substring(0,3);
        String currencyTwo = spinnerBottom.getSelectedItem().toString().substring(0,3);

        if (TextUtils.isEmpty(newsJSON)) {
            return 0.00;
        }

        try {
            JSONObject jObject = new JSONObject(newsJSON);
            conversionValue = jObject.getDouble(currencyOne + "_" + currencyTwo);

        } catch (JSONException e) {

            Log.e("MainActivity", "Problem parsing the JSON results", e);
        }
        return conversionValue;
    }

    private class MyAsyncTask extends AsyncTask<String, Void, Double> {

        @Override
        protected Double doInBackground(String... urlString) {
            String jsonResponse = null;

            if (urlString.length < 1 || urlString[0] == null) {
                return null;
            }
            try {
                URL url = createUrl(urlString[0]);
                jsonResponse = makeHttpRequest(url);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem making the HTTP request.", e);
            }


            return extractFeatureFromJson(jsonResponse);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            spinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Double result) {
            super.onPostExecute(result);

            conversionRateOne = result;
            spinner.setVisibility(View.GONE);

        }

    }

}
