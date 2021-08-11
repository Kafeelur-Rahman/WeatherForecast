package com.kafeel.weatherforcast;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    EditText searchBox;
    Button searchBtn;
    TextView cityTv,countryTv,regionTv,conditionTv,celsiusTv,fahrenheitTv,localTimeTv,dayNightTv,infoTv;
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVariables();
        Toast.makeText(getApplicationContext(),"Developed by Kafeelur Rahman T K",Toast.LENGTH_SHORT).show();

        infoTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                about();
            }
        });
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url=getString(R.string.url)+getString(R.string.api_key)+searchBox.getText().toString();
                if(checkInternet()) {
                    if(searchBox.getText().toString().length() < 2)
                        Toast.makeText(getApplicationContext(),"Invalid city name!!!",Toast.LENGTH_SHORT).show();
                    else {
                        clearText();
                        new GetJson().execute(url);
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(),"Internet connection Failed",Toast.LENGTH_SHORT).show();
                    Snackbar.make(v, "Internet is not connected! Try Again.", Snackbar.LENGTH_SHORT).setActionTextColor(Color.RED).show();
                }
            }
        });

    }

    public void initVariables(){
        cityTv=findViewById(R.id.city);
        searchBox=findViewById(R.id.searchBox);
        searchBtn=findViewById(R.id.button);
        countryTv=findViewById(R.id.country);
        regionTv=findViewById(R.id.region);
        conditionTv=findViewById(R.id.condition);
        celsiusTv=findViewById(R.id.temp_c);
        fahrenheitTv=findViewById(R.id.temp_f);
        localTimeTv=findViewById(R.id.localTime);
        dayNightTv=findViewById(R.id.dayNight);
        infoTv=findViewById(R.id.info);
    }
    public boolean checkInternet(){
        ConnectivityManager network=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info=network.getActiveNetworkInfo();
        boolean flag=info != null && info.isConnectedOrConnecting();
        return flag;
    }
    public void clearText(){
        cityTv.setText("");
        countryTv.setText("");
        regionTv.setText("");
        conditionTv.setText("");
        celsiusTv.setText("");
        fahrenheitTv.setText("");
        localTimeTv.setText("");
        dayNightTv.setText("");

    }
    public void about(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        String about="App Name  : Weather Forecast\n\n" +
                     "Version   : 1.0.0\n\n"+
                     "Developer : Kafeelur Rahman T K\n\n" +
                     "Website   : www.kafeelurrahman.weebly.com";
        builder.setTitle("Info");
        builder.setMessage(about);
        builder.setPositiveButton("Ok",null);
        AlertDialog dialog=builder.create();
        dialog.show();
    }
    public void setAnimation(TextView tv){
        tv.animate().translationX(100).setDuration(1500).translationX(-100).start();
        tv.animate().setDuration(1000).translationX(100).start();
    }
    private class GetJson extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Fetching Data...");
            pd.setCancelable(false);
            pd.show();
        }


        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                return buffer.toString();

            }
            catch (Exception e){
                regionTv.setText("Invalid name!!!");
                regionTv.setTextColor(Color.RED);
                regionTv.setVisibility(View.VISIBLE);
                setAnimation(regionTv);
            }
           finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()) {
                pd.dismiss();
            }

            String data[] = result.split(",");
            Map<String, String> map = new HashMap<String, String>();
            for (int i = 1; i < data.length; i++) {
                if (Pattern.compile(".region*").matcher(data[i]).find()) {
                    data[i] = data[i].substring(data[i].indexOf(":") + 1, data[i].length()).replace("\"", "");
                    map.put("Region", data[i]);
                } else if (Pattern.compile(".country*").matcher(data[i]).find()) {
                    data[i] = data[i].substring(data[i].lastIndexOf(":") + 1, data[i].length()).replace("\"", "");
                    map.put("Country", data[i]);
                } else if (Pattern.compile(".localtime[^a-z]*").matcher(data[i]).find()) {
                    data[i] = data[i].substring(data[i].indexOf(":") + 1, data[i].length() - 1).replace("\"", "");
                    map.put("Localtime", data[i]);
                } else if (Pattern.compile(".temp_c.").matcher(data[i]).find()) {
                    data[i] = data[i].substring(data[i].lastIndexOf(":") + 1, data[i].length()).replace("\"", "");
                    map.put("Temperature(Celsius)", data[i]);
                } else if (Pattern.compile(".*temp_f*").matcher(data[i]).find()) {
                    data[i] = data[i].substring(data[i].lastIndexOf(":") + 1, data[i].length()).replace("\"", "");
                    map.put("Temperature (Fahrenheit)", data[i]);
                } else if (Pattern.compile(".is_day*").matcher(data[i]).find()) {
                    if (data[i].charAt(data[i].length() - 1) == '1')
                        map.put("Day/Night", "Day");
                    else
                        map.put("Day/Night", "Night");
                } else if (Pattern.compile(".*text*").matcher(data[i]).find()) {
                    data[i] = data[i].substring(data[i].lastIndexOf(':') + 1, data[i].length()).replace("\"", "");
                    map.put("Condition", data[i]);
                } else {
                    continue;
                }
            }
            if (result != null) {
                cityTv.setText("\t"+searchBox.getText().toString());
                cityTv.setVisibility(View.VISIBLE);
                setAnimation(cityTv);
                TextView tv[] = {conditionTv, localTimeTv, dayNightTv, regionTv, countryTv, fahrenheitTv, celsiusTv};
                int i = 0;
                for (Map.Entry<String, String> e : map.entrySet()) {
                    tv[i].setText("\t"+e.getKey() + " : " + e.getValue());
                    tv[i].setVisibility(View.VISIBLE);
                    setAnimation(tv[i]);
                    i++;
                }
            }
        }
    }
}
