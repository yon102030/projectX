package com.example.projectx;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class userpage extends AppCompatActivity {

    private TextView tvHelloUser;
    private TextView tvDate, tvForecast, tvTemperature;
    private Spinner spinnerCity;

    private Map<String, String> cityMap;  // מיפוי עברית -> אנגלית

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_userpage);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvHelloUser = findViewById(R.id.tvHelloUser);
        tvDate = findViewById(R.id.tvDate);
        tvForecast = findViewById(R.id.tvForecast);
        tvTemperature = findViewById(R.id.tvTemperature);
        spinnerCity = findViewById(R.id.spinnerCity);

        // קבלת השם מה-Intent
        String userName = getIntent().getStringExtra("USER_NAME");
        if (userName != null && !userName.isEmpty()) {
            tvHelloUser.setText("שלום " + userName);
        } else {
            tvHelloUser.setText("שלום משתמש");
        }

        // תאריך של היום
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvDate.setText("תאריך: " + sdf.format(new Date()));

        // 🔹 רשימת ערים בעברית ומיפוי לאנגלית
        // 🔹 רשימת ערים בעברית ומיפוי לאנגלית
        cityMap = new LinkedHashMap<>();
        cityMap.put("תל אביב", "Tel-Aviv");
        cityMap.put("ירושלים", "Jerusalem");
        cityMap.put("חיפה", "Haifa");
        cityMap.put("ראשון לציון", "Rishon LeZion");
        cityMap.put("באר שבע", "Beersheba");
        cityMap.put("חולון", "Holon");
        cityMap.put("בת ים", "Bat Yam");
        cityMap.put("רמת גן", "Ramat Gan");
        cityMap.put("בני ברק", "Bnei Brak");
        cityMap.put("פתח תקווה", "Petah-Tikva");
        cityMap.put("ראש העין", "Rosh HaAyin");
        cityMap.put("כפר סבא", "Kfar Saba");
        cityMap.put("מודיעין", "Modi'in");
        cityMap.put("הרצליה", "Herzliya");
        cityMap.put("אשדוד", "Ashdod");

// יוצרים רשימה עם הפריט הראשון "בחר עיר"
        ArrayList<String> cityList = new ArrayList<>();
        cityList.add("בחר עיר"); // placeholder
        cityList.addAll(cityMap.keySet());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, cityList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(adapter);

// מאזין לבחירת עיר
        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) return; // אם בחרו "בחר עיר" לא עושים כלום

                String selectedHebrewCity = (String) parent.getItemAtPosition(position);
                String cityEnglish = cityMap.get(selectedHebrewCity);
                getWeather(cityEnglish); // קריאה אוטומטית למזג אוויר
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        // כפתור התנתקות
        Button buttonLogout = findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(userpage.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    // 🔹 פונקציה למזג אוויר לפי עיר באנגלית
    private void getWeather(String city) {
        new Thread(() -> {
            try {
                String apiKey = "e8e3be7aa7ae0f758c5ae79ac5e4d8be"; // המפתח שלך
                String urlString = "https://api.openweathermap.org/data/2.5/weather?q="
                        + city + "&units=metric&lang=he&appid=" + apiKey;

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    throw new Exception("Response code: " + responseCode);
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject json = new JSONObject(result.toString());
                double temp = json.getJSONObject("main").getDouble("temp");
                String description = json.getJSONArray("weather").getJSONObject(0).getString("description");

                runOnUiThread(() -> {
                    tvForecast.setText("תחזית: " + description);
                    tvTemperature.setText("מעלות: " + (int) temp + "°");
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    tvForecast.setText("לא ניתן לטעון מזג אוויר");
                    tvTemperature.setText("--");
                });
            }
        }).start();
    }
}
