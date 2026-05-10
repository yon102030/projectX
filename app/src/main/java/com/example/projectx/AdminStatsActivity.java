package com.example.projectx;

import android.content.SharedPreferences;
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

    // מילון (Map) שישמור כל צבע וכמה פעמים בחרו בו
    private Map<String, Integer> colorClicks = new HashMap<>();

    private final String[] allColors = {
            "שחור","לבן","אפור","כחול","כחול כהה","אדום","ירוק","חום",
            "בז","צהוב","כתום","סגול","ורוד","טורקיז","זית"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_stats);

        // הפעלת הפונקציות שמכינות את המסך וטוענות את הנתונים
        initViews();
        loadStats();
        loadTopColorsData();
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

        // 1. ספירת משתמשים: מבקשים את כל המשתמשים ומציגים את גודל הרשימה
        databaseService.getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                if (users != null) tvTotalUsers.setText("משתמשים רשומים: " + users.size());
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
                        if (outfit.getOuter() != null) {
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

    // פונקציה שקוראת נתונים מזיכרון המכשיר המקומי (SharedPreferences)
    // כדי לבדוק כמה פעמים המשתמשים לחצו על כל צבע
    private void loadTopColorsData() {
        // פותחים את קובץ ההגדרות המקומי שנקרא "colors"
        SharedPreferences prefs = getSharedPreferences("colors", MODE_PRIVATE);
        colorClicks.clear();

        // עוברים על כל הצבעים הקיימים, ושואבים כמה לחיצות (count) נשמרו להם.
        // אם לחצו על צבע מסוים יותר מ-0 פעמים, שומרים אותו בתוך ה"מילון" (colorClicks)
        for (String color : allColors) {
            int count = prefs.getInt(color, 0);
            if (count > 0) colorClicks.put(color, count);
        }

        // אחרי שסיימנו לאסוף נתונים, מעדכנים את הרשימה במסך
        updateTopColorsList();
    }

    // פונקציה שלוקחת את נתוני הלחיצות, ממיינת אותם מהגבוה לנמוך ומציגה עד 5 מובילים
    private void updateTopColorsList() {
        topColorsList.clear();

        // הופכים את ה"מילון" לרשימה שאפשר למיין
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(colorClicks.entrySet());

        // מיון הרשימה מהמספר הגדול (הכי הרבה לחיצות) למספר הקטן
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        // בחירה של עד 5 הצבעים הראשונים (או פחות, אם אין 5 צבעים שנלחצו)
        int limit = Math.min(5, sorted.size());
        for (int i = 0; i < limit; i++) {
            topColorsList.add(sorted.get(i).getKey()); // מוסיפים רק את שם הצבע
        }

        // מודיעים למתאם (Adapter) שהנתונים השתנו כדי שירענן את התצוגה במסך
        topColorsAdapter.notifyDataSetChanged();
    }
}