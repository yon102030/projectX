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
import android.widget.Toast;

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
import java.util.Map;
import java.util.Locale;

// המסך הראשי של משתמש רגיל לאחר התחברות.
// כאן המשתמש רואה מזג אוויר חי, בוחר מגדר, ויכול להתחיל בהרכבת אאוטפיט.
public class userpage extends AppCompatActivity {

    private TextView tvHelloUser, tvDate, tvForecast, tvTemperature;
    private Spinner spinnerCity;
    private Button Btnuser2, additem, btnSaved;
    private RadioGroup radioGender;

    // מילון נתונים (Map) שישמור את שמות הערים בעברית (לתצוגה) יחד עם השם שלהן באנגלית (לחיפוש באינטרנט)
    private Map<String, String> cityMap;

    private boolean isMaleSelected = true; // ברירת מחדל (מוגדר כגבר עד שהמשתמש ישנה)

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_userpage);

        // סידור ריווחי המסך (למניעת הסתרת כפתורים על ידי סרגל הניווט של הטלפון)
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

        // 🔥 קישור כפתור הלוקים השמורים ל-XML (שים לב שזה ה-ID הנכון אצלך)
        btnSaved = findViewById(R.id.btnSavedLooks);

        // משיכת התאריך של היום מהמכשיר והצגתו בפורמט יום/חודש/שנה
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvDate.setText("תאריך: " + sdf.format(new Date()));

        // קבלת שם המשתמש ממסך ההתחברות והצגת ברכת שלום אישית
        String userName = getIntent().getStringExtra("USER_NAME");
        tvHelloUser.setText((userName != null && !userName.isEmpty()) ? "שלום " + userName : "שלום משתמש");

        // האזנה לכפתורי הרדיו (מגדר): מעדכנים את המשתנה isMaleSelected בכל פעם שהמשתמש מחליף בחירה
        radioGender.setOnCheckedChangeListener((group, checkedId) -> isMaleSelected = (checkedId == R.id.radioMale));

        // כפתור מעבר למסך "הוספת פריט" לארון
        additem.setOnClickListener(v -> {
            Intent intent = new Intent(userpage.this, AddClothe.class);
            startActivity(intent);
        });

        // 🔥 כפתור מעבר למסך הלוקים המועדפים (הופרד מכפתור ההוספה)
        btnSaved.setOnClickListener(v -> {
            // בדיקה שבאמת נבחר מגדר כדי שלא יקרוס או יעביר נתון שגוי
            if (radioGender.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "אנא בחר גבר/אישה לפני המעבר ללוקים שמורים", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(userpage.this, savedlooks.class);
            // העברת המגדר הנבחר לעמוד הבא
            intent.putExtra("IS_MALE", isMaleSelected);
            startActivity(intent);
        });

        // יצירת רשימת הערים לחיפוש מזג האוויר.
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
        cityMap.put("אורנג’סטאד", "Oranjestad"); // עיר נוספת לבדיקת מזג אוויר טרופי
        cityMap.put("אלסקה", "Alaska");
        cityMap.put("דוהה", "Doha");

        // הכנת הרשימה שתוצג בתוך הספינר (תפריט נגלל)
        ArrayList<String> cityList = new ArrayList<>();
        cityList.add("בחר עיר"); // אופציית ברירת המחדל (לא מבצעת חיפוש)
        cityList.addAll(cityMap.keySet()); // הוספת כל הערים בעברית

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, cityList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(adapter);

        // האזנה לבחירת עיר מתוך הרשימה
        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) return; // התעלמות אם נבחר "בחר עיר"

                // שליפת השם באנגלית מתוך ה-Map (למשל, "תל אביב" הופך ל-"Tel-Aviv")
                String cityEng = cityMap.get(parent.getItemAtPosition(position));

                // הפעלת הפונקציה שניגשת לאינטרנט כדי להביא את מזג האוויר
                getWeather(cityEng);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // כפתור התנתקות מהמערכת וחזרה למסך פתיחה תוך ניקוי זיכרון המסכים
        findViewById(R.id.buttonLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(userpage.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // ==========================================
        // כפתור "הבא" - מעבר למסך בחירת הצבעים (colorpage)
        // ==========================================
        Btnuser2.setOnClickListener(v -> {

            // בדיקות תקינות: וידוא שהמשתמש באמת בחר מגדר ועיר לפני שהוא ממשיך
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

            // יצירת המעבר לעמוד בחירת הצבעים (colorpage)
            Intent intent = new Intent(userpage.this, colorpage.class);

            // חילוץ המספר (מעלות) מתוך הטקסט של מזג האוויר בעזרת Regular Expression (מוחק את כל מה שהוא לא מספר)
            String tempStr = tvTemperature.getText().toString().replaceAll("[^0-9]", "");

            // אם הטקסט ריק משום מה, נשים 25 מעלות כברירת מחדל כדי לא לקרוס
            double temperature = tempStr.isEmpty() ? 25 : Double.parseDouble(tempStr);

            // הוספת הנתונים שאספנו (מגדר וטמפרטורה) "לתוך המזוודה" (Extras) של המעבר לעמוד הבא
            intent.putExtra("IS_MALE", isMaleSelected);
            intent.putExtra("TEMPERATURE", temperature);

            startActivity(intent);
        });
    }

    // ==========================================
    // קריאה בזמן אמת לשרת מזג האוויר (API)
    // ==========================================
    private void getWeather(String city) {

        // אנדרואיד לא מסכים לבצע קריאות לאינטרנט על ה-Thread הראשי (זה שאחראי על הציור של המסך)
        // כדי שהאפליקציה לא תיתקע. לכן פותחים כאן Thread (תהליך) נפרד ברקע.
        new Thread(() -> {
            try {
                // מפתח הגישה שלנו לשירות OpenWeatherMap
                String apiKey = "e8e3be7aa7ae0f758c5ae79ac5e4d8be";

                // בניית הכתובת אליה אנחנו פונים (מוסיפים את שם העיר, מבקשים מעלות צלזיוס=metric ושפה=עברית)
                String urlString = "https://api.openweathermap.org/data/2.5/weather?q="
                        + city + "&units=metric&lang=he&appid=" + apiKey;

                // פתיחת חיבור לאינטרנט
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000); // מקסימום זמן המתנה להתחברות
                connection.setReadTimeout(15000);    // מקסימום זמן המתנה לקריאת הנתונים
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                // אם התשובה לא הייתה 200 (200 אומר "הכל בסדר"), נזרוק שגיאה שנעצור
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) throw new Exception("Response code: " + responseCode);

                // קריאת התשובה של השרת שורה אחרי שורה
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) result.append(line);

                // התשובה מגיעה כקובץ טקסט בפורמט JSON. אנחנו מפענחים אותו לאובייקט
                JSONObject json = new JSONObject(result.toString());

                // שולפים את הטמפרטורה המספרית
                double temp = json.getJSONObject("main").getDouble("temp");

                // שולפים את תיאור מזג האוויר ("מעונן חלקית", "שמש" וכו')
                String description = json.getJSONArray("weather").getJSONObject(0).getString("description");

                // אחרי שסיימנו לעבוד ברקע, חייבים לחזור ל-Thread הראשי (UI Thread) כדי לשנות את הטקסט במסך
                runOnUiThread(() -> {
                    tvForecast.setText("תחזית: " + description);
                    tvTemperature.setText("מעלות: " + (int) temp + "°");
                });

            } catch (Exception e) {
                // במקרה של שגיאה (אין אינטרנט או שהעיר לא קיימת) נציג למשתמש הודעת גיבוי
                e.printStackTrace();
                runOnUiThread(() -> {
                    tvForecast.setText("לא ניתן לטעון מזג אוויר");
                    tvTemperature.setText("--");
                });
            }
        }).start(); // הפעלת התהליך שמוגדר פה למעלה
    }
}