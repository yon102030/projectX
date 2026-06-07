package com.example.projectx;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

// המסך הראשי של משתמש רגיל לאחר התחברות.
public class userpage extends AppCompatActivity {

    private TextView tvHelloUser, tvDate, tvForecast, tvTemperature;
    private Spinner spinnerCity;
    private Button Btnuser2, additem, btnSaved;
    private RadioGroup radioGender;

    private Map<String, String> cityMap;
    private boolean isMaleSelected = true;
    private SharedPreferences prefs;

    private static final int LOCATION_PERMISSION_CODE = 100;

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
        additem = findViewById(R.id.additem);
        btnSaved = findViewById(R.id.btnSavedLooks);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvDate.setText("תאריך: " + sdf.format(new Date()));

        String userName = getIntent().getStringExtra("USER_NAME");
        tvHelloUser.setText((userName != null && !userName.isEmpty()) ? "שלום " + userName : "שלום משתמש");
        prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        isMaleSelected = prefs.getBoolean("IS_MALE", true);
        if (isMaleSelected) radioGender.check(R.id.radioMale);
        else radioGender.check(R.id.radioFemale);

        radioGender.setOnCheckedChangeListener((group, checkedId) -> {
            isMaleSelected = (checkedId == R.id.radioMale);
            prefs.edit().putBoolean("IS_MALE", isMaleSelected).apply();
        });

        additem.setOnClickListener(v -> {
            Intent intent = new Intent(userpage.this, AddClothe.class);
            startActivity(intent);
        });

        btnSaved.setOnClickListener(v -> {
            if (radioGender.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "אנא בחר גבר/אישה לפני המעבר ללוקים שמורים", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(userpage.this, savedlooks.class);
            intent.putExtra("IS_MALE", isMaleSelected);
            startActivity(intent);
        });

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
        cityMap.put("אורנג’סטאד", "Oranjestad");
        cityMap.put("אלסקה", "Alaska");
        cityMap.put("דוהה", "Doha");
        cityMap.put("קינגסטון", "Kingston");

        ArrayList<String> cityList = new ArrayList<>();
        cityList.add("בחר עיר");
        cityList.addAll(cityMap.keySet());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cityList);
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

        // בדיקת מיקום בטעינת המסך
        checkLocationPermissionAndSetCity();

        findViewById(R.id.buttonLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(userpage.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        Btnuser2.setOnClickListener(v -> {
            boolean genderSelected = radioGender.getCheckedRadioButtonId() != -1;
            boolean citySelected = spinnerCity.getSelectedItemPosition() != 0;

            if (!genderSelected && !citySelected) {
                Toast.makeText(this, "חייב לבחור מגדר ועיר", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!genderSelected) {
                Toast.makeText(this, "חייב לבחור מגדר", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!citySelected) {
                Toast.makeText(this, "חייב לבחור עיר", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(userpage.this, colorpage.class);
            String tempStr = tvTemperature.getText().toString().replaceAll("[^0-9]", "");
            double temperature = tempStr.isEmpty() ? 25 : Double.parseDouble(tempStr);

            intent.putExtra("IS_MALE", isMaleSelected);
            intent.putExtra("TEMPERATURE", temperature);
            startActivity(intent);
        });
    }

    // 1. בודק הרשאות GPS
    private void checkLocationPermissionAndSetCity() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        } else {
            fetchDeviceLocation();
        }
    }

    // 2. תופס את תשובת המשתמש להרשאה
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchDeviceLocation();
            } else {
                Toast.makeText(this, "הרשאת מיקום נדחתה. יש לבחור עיר ידנית.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 3. שליפת המיקום מהמכשיר
    // 3. שליפת המיקום מהמכשיר (עם גיבוי אם חסר שם עיר)
    @SuppressLint("MissingPermission")
    private void fetchDeviceLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // שימוש בעדכון חד פעמי כדי לקבל מיקום טרי מה-GPS
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new android.location.LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                try {
                    Geocoder geocoder = new Geocoder(userpage.this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                    if (addresses != null && !addresses.isEmpty()) {
                        Address addr = addresses.get(0);

                        // מנסים לשלוף שם עיר (Locality)
                        String detectedCity = addr.getLocality();
                        // מנסים לשלוף שם אזור/עיר מחוז (SubAdminArea)
                        String area = addr.getSubAdminArea();

                        // לוג לבדיקה (תוכל לראות ב-Logcat מה גוגל מצא)
                        android.util.Log.d("LocationDebug", "Detected Locality: " + detectedCity + ", Area: " + area);

                        // המנגנון החכם: אם Locality הוא שכונה או ריק, נשתמש ב-Area
                        // נבדוק גם אם ה-Locality מכיל מילים לא רלוונטיות
                        if (detectedCity == null || detectedCity.length() < 2 || detectedCity.contains("Ganim")) {
                            detectedCity = area;
                        }

                        if (detectedCity != null) {
                            matchCityToSpinner(detectedCity);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, null);
    }

    // 4. התאמה חכמה לספינר - בודקת אוטומטית לפי המילון שיצרנו!
    private void matchCityToSpinner(String detectedCity) {
        String lowerDetected = detectedCity.toLowerCase().trim();

        // נשתמש ב-GPS כשם המטרה הראשי
        final String targetCity = findBestMatch(lowerDetected);

        spinnerCity.post(() -> {
            boolean found = false;
            for (int i = 0; i < spinnerCity.getCount(); i++) {
                // בדיקה אם שם העיר מה-GPS מופיע בספינר
                if (spinnerCity.getItemAtPosition(i).toString().equals(targetCity)) {
                    spinnerCity.setSelection(i);
                    Toast.makeText(userpage.this, "הספינר עודכן ל: " + targetCity, Toast.LENGTH_SHORT).show();
                    found = true;
                    break;
                }
            }
            if (!found) {
                Toast.makeText(userpage.this, "העיר " + targetCity + " לא נמצאה בספינר", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // פונקציית עזר שמתרגמת את ה-GPS לערים שלך
    private String findBestMatch(String gpsCity) {
        String city = gpsCity.toLowerCase();

        // בוא נבדוק את הערכים במילון שלך אחד אחד (גם עברית וגם אנגלית)
        for (Map.Entry<String, String> entry : cityMap.entrySet()) {
            String hebrewName = entry.getKey().toLowerCase();
            String englishName = entry.getValue().toLowerCase();

            // האם ה-GPS אמר משהו שדומה לעברית או לאנגלית של העיר?
            if (city.contains(hebrewName) || city.contains(englishName) ||
                    englishName.contains(city) || hebrewName.contains(city)) {
                return entry.getKey(); // מחזיר את השם בעברית (המפתח בספינר)
            }
        }
        return "בחר עיר"; // ברירת מחדל
    }

    // קריאה לשרת מזג האוויר
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