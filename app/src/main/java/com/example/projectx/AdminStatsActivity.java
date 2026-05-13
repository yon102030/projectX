package com.example.projectx;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projectx.adapter.TopColorsAdapter;
import com.example.projectx.model.Clothe;
import com.example.projectx.model.Outfit;
import com.example.projectx.model.User;
import com.example.projectx.services.DatabaseService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// מסך הסטטיסטיקות של המנהל: מציג כמות משתמשים, בגדים, חלוקה לעונות וצבעים פופולריים
public class AdminStatsActivity extends AppCompatActivity {

    private TextView tvTotalUsers, tvTotalClothes, tvSeasonStats;
    private DatabaseService databaseService;
    private ImageButton btnBack;

    // רכיבים עבור רשימת הצבעים הפופולריים
    private RecyclerView rvAdminTopColors;
    private TopColorsAdapter topColorsAdapter;
    private List<String> topColorsList = new ArrayList<>();

    // מילון גלובלי שישמור כל צבע וכמה פעמים *כל המשתמשים* בחרו בו יחד
    private Map<String, Integer> colorClicks = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_stats);

        // הפעלת הפונקציות שמכינות את המסך וטוענות את הנתונים מהשרת
        initViews();
        loadStats();
    }

    // קישור רכיבי העיצוב לקוד והגדרות ראשוניות
    private void initViews() {
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalClothes = findViewById(R.id.tvTotalClothes);
        tvSeasonStats = findViewById(R.id.tvSeasonStats);
        btnBack = findViewById(R.id.btnBack);
        rvAdminTopColors = findViewById(R.id.rvAdminTopColors);

        databaseService = DatabaseService.getInstance();

        btnBack.setOnClickListener(v -> finish());

        // הגדרת רשימת הצבעים הפופולריים (RecyclerView) שתציג את הפריטים אחד מתחת לשני
        rvAdminTopColors.setLayoutManager(new LinearLayoutManager(this));
        topColorsAdapter = new TopColorsAdapter(topColorsList);
        rvAdminTopColors.setAdapter(topColorsAdapter);
    }

    // פונקציה ששואבת נתונים מפיירבייס ומציגה אותם במסך
    private void loadStats() {

        // 1. ספירת משתמשים + איסוף נתוני צבעים מכל המשתמשים יחד
        databaseService.getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                if (users != null) {
                    tvTotalUsers.setText("משתמשים רשומים: " + users.size());

                    colorClicks.clear(); // איפוס מונה הצבעים הגלובלי

                    // עוברים על כל משתמש במערכת
                    for (User user : users) {
                        Map<String, Integer> userColors = user.getColorStats();

                        // אם למשתמש יש היסטוריית צבעים, נוסיף אותה לספירה הכללית
                        if (userColors != null) {
                            for (Map.Entry<String, Integer> entry : userColors.entrySet()) {
                                String colorName = entry.getKey();
                                int count = entry.getValue();

                                // מוסיפים לסך הכל הקיים (או ל-0 אם זה צבע שטרם נתקלנו בו)
                                int currentTotal = colorClicks.getOrDefault(colorName, 0);
                                colorClicks.put(colorName, currentTotal + count);
                            }
                        }
                    }

                    // אחרי שסיימנו לעבור על כל המשתמשים ולאסוף את הלחיצות, מעדכנים את הרשימה
                    updateTopColorsList();
                }
            }
            @Override
            public void onFailed(Exception e) {}
        });

        // 2. ספירת בגדים: מבקשים את כל הפריטים ומציגים את גודל הרשימה
        databaseService.getClotheList(new DatabaseService.DatabaseCallback<List<Clothe>>() {
            @Override
            public void onCompleted(List<Clothe> clothes) {
                if (clothes != null) tvTotalClothes.setText("פריטים במערכת: " + clothes.size());
            }
            @Override
            public void onFailed(Exception e) {}
        });

        // 3. ספירת אאוטפיטים לפי עונות
        databaseService.getOutfitList(new DatabaseService.DatabaseCallback<List<Outfit>>() {
            @Override
            public void onCompleted(List<Outfit> outfits) {
                if (outfits != null) {
                    int summerCount = 0;
                    int winterCount = 0;

                    // עוברים על כל האאוטפיטים ובודקים:
                    for (Outfit outfit : outfits) {
                        // הלוגיקה קובעת: אם יש פריט עליון (מעיל/ז'קט) - זה אאוטפיט חורף
                        if (outfit.getOuter() != null && !outfit.getOuter().isEmpty()) {
                            winterCount++;
                        } else {
                            // אם אין פריט עליון - זה אאוטפיט קיץ
                            summerCount++;
                        }
                    }
                    tvSeasonStats.setText("אאוטפיטים קיציים: " + summerCount + " | אאוטפיטים חורפיים: " + winterCount);
                }
            }

            @Override
            public void onFailed(Exception e) {
                tvSeasonStats.setText("שגיאה בטעינת נתונים");
            }
        });
    }

    // פונקציה שלוקחת את נתוני הלחיצות, ממיינת אותם מהגבוה לנמוך ומציגה עד 5 מובילים
    private void updateTopColorsList() {
        topColorsList.clear();

        // הופכים את ה"מילון" (Map) לרשימה שאפשר למיין
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(colorClicks.entrySet());

        // מיון הרשימה מהמספר הגדול (הכי הרבה לחיצות בכל המערכת) למספר הקטן
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        // בחירה של עד 5 הצבעים הראשונים
        int limit = Math.min(5, sorted.size());
        for (int i = 0; i < limit; i++) {
            topColorsList.add(sorted.get(i).getKey()); // מוסיפים רק את שם הצבע
        }

        // מודיעים למתאם (Adapter) שהנתונים השתנו כדי שירענן את התצוגה במסך
        topColorsAdapter.notifyDataSetChanged();
    }
}