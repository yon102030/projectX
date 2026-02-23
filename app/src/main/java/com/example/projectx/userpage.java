package com.example.projectx;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
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

    private TextView tvHelloUser, tvDate, tvForecast, tvTemperature;
    private Spinner spinnerCity;
    private Button Btnuser2, additem;
    private RadioGroup radioGender;

    private Map<String, String> cityMap;
    private boolean isMaleSelected = true; // ברירת מחדל

    @SuppressLint("MissingInflatedId")
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
        Btnuser2 = findViewById(R.id.buttonuser2);
        radioGender = findViewById(R.id.radioGender);
        additem=findViewById(R.id.additem);

        // תאריך
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvDate.setText("תאריך: " + sdf.format(new Date()));

        // בחירת שם משתמש
        String userName = getIntent().getStringExtra("USER_NAME");
        tvHelloUser.setText((userName != null && !userName.isEmpty()) ? "שלום " + userName : "שלום משתמש");

        // מאזין ל־RadioGroup
        radioGender.setOnCheckedChangeListener((group, checkedId) -> isMaleSelected = (checkedId == R.id.radioMale));

        additem.setOnClickListener(v -> {
            Intent intent = new Intent(userpage.this, AddClothe.class);
            startActivity(intent);
        });

        // רשימת ערים
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
        cityMap.put("אילת", "Eilat");
        cityMap.put("אורנג’סטאד", "Oranjestad"); // טמפרטורה כעת ~27°C

        ArrayList<String> cityList = new ArrayList<>();
        cityList.add("בחר עיר");
        cityList.addAll(cityMap.keySet());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, cityList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(adapter);

        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) return;
                String cityEng = cityMap.get(parent.getItemAtPosition(position));
                getWeather(cityEng);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // כפתור התנתקות
        findViewById(R.id.buttonLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(userpage.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // מעבר ל־user2Activity
        Btnuser2.setOnClickListener(v -> {
            Intent intent = new Intent(userpage.this, colorpage.class);

            // שליחת הגדרות למעבר ל־user2Activity
            boolean isMale = isMaleSelected;
            String tempStr = tvTemperature.getText().toString().replaceAll("[^0-9]", "");
            double temperature = tempStr.isEmpty() ? 25 : Double.parseDouble(tempStr);

            intent.putExtra("IS_MALE", isMale);
            intent.putExtra("TEMPERATURE", temperature);

            startActivity(intent);
        });
    }

    // פונקציה למשיכת מזג אוויר
    private void getWeather(String city) {
        new Thread(() -> {
            try {
                String apiKey = "e8e3be7aa7ae0f758c5ae79ac5e4d8be";
                String urlString = "https://api.openweathermap.org/data/2.5/weather?q="
                        + city + "&units=metric&lang=he&appid=" + apiKey;

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) throw new Exception("Response code: " + responseCode);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) result.append(line);

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
