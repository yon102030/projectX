package com.example.projectx;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectx.adapter.TopColorsAdapter;
import com.example.projectx.model.User;
import com.example.projectx.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// מסך בחירת הצבעים. מאפשר למשתמש לבחור צבעי עליונים ותחתונים ללוק,
// ובמקביל מנהל אלגוריתם אישי של הצבעים הכי אהובים (פופולריים) על המשתמש.
public class colorpage extends AppCompatActivity {

    // משתני תצוגה
    private GridLayout layoutTopColors, layoutBottomColors;
    private Button btnApply, btnAllTop, btnAllB;

    // משתנים שהגיעו מהמסך הקודם דרך Intent
    private double temperature;
    private boolean isMale;

    // רשימות לשמירת הצבעים שהמשתמש בחר כרגע על המסך
    private final List<String> selectedTopColors = new ArrayList<>();
    private final List<String> selectedBottomColors = new ArrayList<>();

    // מאגר כל הצבעים האפשריים באפליקציה
    private final String[] allColors = {
            "שחור","לבן","אפור","כחול","כחול כהה","אדום","ירוק","חום",
            "בז","צהוב","כתום","סגול","ורוד","טורקיז","זית","תכלת"
    };

    private final List<Button> topButtons = new ArrayList<>();
    private final List<Button> bottomButtons = new ArrayList<>();

    // משתני דגל (Flags) לזיהוי מצב לחיצה על כפתורי "בחר הכל"
    private boolean isTopAllSelected = false;
    private boolean isBottomAllSelected = false;

    // *** משתני אלגוריתם הצבעים הפופולריים ***
    // המילון שאוגר את מספר הלחיצות על כל צבע אי פעם
    private final Map<String, Integer> colorClicks = new HashMap<>();
    // הרשימה שתכיל רק את 5 הצבעים המובילים
    private final List<String> topColorsList = new ArrayList<>();
    // האדפטר שיצייר את 5 הצבעים למסך
    private TopColorsAdapter topColorsAdapter;

    private RecyclerView rvTopColors;
    private ImageButton btnBack;

    /**
     * הפעולה הראשית שרצה עם פתיחת המסך.
     * מאתחלת את ה-UI, שולפת נתונים מה-Intent, מפעילה משיכת נתונים מ-Firebase
     * ומייצרת את כפתורי הצבעים.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colorpage);

        // שאיבת הנתונים שהועברו מהמסך הקודם (userpage)
        temperature = getIntent().getDoubleExtra("TEMPERATURE", 20);
        isMale = getIntent().getBooleanExtra("IS_MALE", true);

        // קישור משתני התצוגה (Views) מקובץ ה-XML
        layoutTopColors = findViewById(R.id.layoutTopColors);
        layoutBottomColors = findViewById(R.id.layoutBottomColors);
        btnApply = findViewById(R.id.btnApply);
        btnAllB = findViewById(R.id.btnSelectAllBColors);
        btnAllTop = findViewById(R.id.btnSelectAllTopColors);
        rvTopColors = findViewById(R.id.recyclerTopColors);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // הגדרת הרשימה הנגללת אופקית (RecyclerView) שתציג את 5 הצבעים הפופולריים
        rvTopColors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        topColorsAdapter = new TopColorsAdapter(topColorsList);
        rvTopColors.setAdapter(topColorsAdapter);

        // קריאה לפעולה שתשאב מ-Firebase את ההיסטוריה של הצבעים של המשתמש
        loadSavedColors();

        // טיפול בשוליים (Insets) להתאמה למסכי מגע מלאים (Edge-to-Edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });

        // הגדרת כפתורי "בחר הכל" - הם מפנים לפונקציית עזר משותפת (handleSelectAll)
        btnAllTop.setOnClickListener(v -> handleSelectAll(topButtons, selectedTopColors, true));
        btnAllB.setOnClickListener(v -> handleSelectAll(bottomButtons, selectedBottomColors, false));

        // ייצור דינמי של כל כפתורי הצבעים על המסך (למעלה ולמטה)
        populateColorBoxes(layoutTopColors, selectedTopColors);
        populateColorBoxes(layoutBottomColors, selectedBottomColors);

        // כפתור אישור (המשך למסך הבא)
        btnApply.setOnClickListener(v -> {
            // בדיקת תקינות: המשתמש חייב לבחור לפחות צבע עליון אחד וצבע תחתון אחד
            if (selectedTopColors.isEmpty() || selectedBottomColors.isEmpty()) {
                Toast.makeText(this, "חייב לבחור צבע לעליונים ולתחתונים", Toast.LENGTH_SHORT).show();
                return;
            }
            // אם הכל תקין, קריאה לפעולה ששומרת את ההיסטוריה ומתקדמת למסך הבא
            saveColorsToFirebaseAndNavigate();
        });
    }

    /**
     * פונקציית עזר לטיפול בכפתור "בחר הכל".
     * מחליפה מצב (Toggle) בין בחירה של כל הצבעים לבין ניקוי הבחירה.
     */
    private void handleSelectAll(List<Button> buttons, List<String> selectedList, boolean isTop) {
        // בדיקה האם כבר עשינו "בחר הכל" בעבר
        boolean alreadyAll = isTop ? isTopAllSelected : isBottomAllSelected;

        if (!alreadyAll) {
            // המשתמש לחץ על "בחר הכל" - נוסיף את כולם
            for (String color : allColors) {
                // נוסיף לספירה רק צבעים שעדיין לא נבחרו (כדי לא לספור פעמיים צבע שהוא כבר לחץ עליו)
                if (!selectedList.contains(color)) {
                    selectedList.add(color);
                    // הוספת לחיצה למילון הסטטיסטיקה האישי (colorClicks)
                    colorClicks.put(color, colorClicks.getOrDefault(color, 0) + 1);
                }
            }
            // הופכים את כל הכפתורים לבולטים (Alpha = 1) כדי לסמן שהם נבחרו
            for (Button b : buttons) b.setAlpha(1f);

        } else {
            // המשתמש לחץ שוב כדי לבטל את "בחירת הכל"
            selectedList.clear();
            // הופכים את הכפתורים לחצי-שקופים
            for (Button b : buttons) b.setAlpha(0.6f);
        }

        // בכל פעם שבוחרים צבעים, יש לעדכן את סרגל 5 הצבעים המובילים
        updateTopColors();

        // עדכון דגל המצב
        if (isTop) isTopAllSelected = !isTopAllSelected;
        else isBottomAllSelected = !isBottomAllSelected;
    }

    /**
     * בנייה דינמית של כפתורי הצבעים על המסך מתוך מערך המחרוזות.
     * מופעלת מה-onCreate עבור העליונים והתחתונים בנפרד.
     */
    private void populateColorBoxes(GridLayout layout, List<String> selectedColors) {
        for (String colorName : allColors) {
            // יצירת כפתור חדש בקוד
            Button colorButton = new Button(this);
            colorButton.setText(colorName);

            // הפיכת הטקסט לעבה וגדול יותר
            colorButton.setTextSize(14);
            colorButton.setTypeface(null, android.graphics.Typeface.BOLD);

            // עיצוב כפתור הצבע: לוקחים קוד Hex ויוצרים רקע עם פינות מעוגלות
            int colorValue = getColorValue(colorName);
            android.graphics.drawable.GradientDrawable border = new android.graphics.drawable.GradientDrawable();
            border.setColor(colorValue);
            border.setCornerRadius(12f);
            colorButton.setBackground(border);

            // שימוש בפונקציית אלגוריתם שבודקת מתי טקסט צריך להיות לבן ומתי שחור כדי שיבלוט רקע
            colorButton.setTextColor(getContrastColor(colorValue));
            colorButton.setAlpha(0.6f); // התחלה כחצי שקוף (לא נבחר)

            // מאזין לחיצה לכל כפתור בנפרד
            colorButton.setOnClickListener(v -> {
                if (selectedColors.contains(colorName)) {
                    // ביטול בחירה
                    selectedColors.remove(colorName);
                    colorButton.setAlpha(0.6f);
                } else {
                    // בחירת צבע
                    selectedColors.add(colorName);
                    colorButton.setAlpha(1f);

                    // הלב של הסטטיסטיקה: קידום המונה של אותו צבע במילון
                    colorClicks.put(colorName, colorClicks.getOrDefault(colorName, 0) + 1);

                    // קריאה לרענון אלגוריתם ה"מובילים"
                    updateTopColors();
                }
            });

            // הגדרות עיצוב לשילוב הכפתור בתוך ה-GridLayout בצורה יפה
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(5, 5, 5, 5);
            colorButton.setLayoutParams(params);

            // הוספה פיזית של הכפתור למסך
            layout.addView(colorButton);
            if (layout == layoutTopColors) topButtons.add(colorButton);
            else bottomButtons.add(colorButton);
        }
    }

    /**
     * פעולה המושכת מ-Firebase את היסטוריית בחירות הצבעים של המשתמש.
     * מופעלת בזמן עליית המסך כדי לאפסן את הנתונים הישנים במילון המקומי.
     */
    private void loadSavedColors() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // שימוש ב-UID לאבטחה אישית

        DatabaseService.getInstance().getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                if (user != null && user.getColorStats() != null) {
                    colorClicks.clear(); // מנקים את המילון המקומי הריק
                    colorClicks.putAll(user.getColorStats()); // מעתיקים את כל ההיסטוריה מהשרת לטלפון
                    updateTopColors(); // מעדכנים את ה-UI עם ה-5 החזקים
                }
            }
            @Override public void onFailed(Exception e) {}
        });
    }

    /**
     * בסיום הבחירה, פעולה זו מסנכרנת את המילון המעודכן (עם הלחיצות החדשות) חזרה לשרת.
     * כשהסנכרון מצליח - עוברים למסך בניית הלוק (user2Activity)
     */
    private void saveColorsToFirebaseAndNavigate() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseService.getInstance().getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                if (user != null) {
                    // עדכון אובייקט המשתמש עם הסטטיסטיקה החדשה
                    user.setColorStats(colorClicks);
                    // שמירת המשתמש חזרה במסד הנתונים
                    DatabaseService.getInstance().updateUser(user, new DatabaseService.DatabaseCallback<Void>() {
                        @Override
                        public void onCompleted(Void object) {
                            // רק אחרי שהשמירה הצליחה, מעבירים את הנתונים למסך הבא ומתקדמים
                            Intent intent = new Intent(colorpage.this, user2Activity.class);
                            intent.putStringArrayListExtra("TOP_COLORS", new ArrayList<>(selectedTopColors));
                            intent.putStringArrayListExtra("BOTTOM_COLORS", new ArrayList<>(selectedBottomColors));
                            intent.putExtra("TEMPERATURE", temperature);
                            intent.putExtra("IS_MALE", isMale);
                            startActivity(intent);
                        }
                        @Override public void onFailed(Exception e) {
                            Toast.makeText(colorpage.this, "שגיאה בסנכרון נתונים", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override public void onFailed(Exception e) {}
        });
    }

    /**
     * האלגוריתם המרכזי למציאת 5 הצבעים הפופולריים.
     * רץ בכל פעם שיש לחיצה על צבע חדש.
     */
    private void updateTopColors() {
        topColorsList.clear(); // מרוקנים את הרשימה הישנה שעל המסך

        // 1. המרת מילון הלחיצות לרשימה כדי שנוכל למיין אותו
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(colorClicks.entrySet());

        // 2. מיון הרשימה בסדר יורד: ממי שנלחץ הכי הרבה למי שנלחץ הכי מעט.
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        // 3. ריצה על הרשימה הממוינת, חיתוך וסיום אחרי 5 פריטים (Math.min מגן משגיאות אם יש פחות מ-5)
        for (int i = 0; i < Math.min(5, sorted.size()); i++) {
            topColorsList.add(sorted.get(i).getKey());
        }

        // 4. הודעה לאדפטר של ה-RecyclerView שהרשימה השתנתה ושצריך לצייר מחדש
        topColorsAdapter.notifyDataSetChanged();
    }

    /**
     * פונקציית עזר המתרגמת שם של צבע בעברית לקוד מספרי (Hex) שהאנדרואיד יודע לצייר
     */
    private int getColorValue(String name) {
        switch (name) {
            case "שחור": return 0xFF000000;
            case "לבן": return 0xFFFFFFFF;
            case "אפור": return 0xFF808080;
            case "כחול": return 0xFF2196F3;
            case "כחול כהה": return 0xFF1565C0;
            case "אדום": return 0xFFF44336;
            case "ירוק": return 0xFF4CAF50;
            case "חום": return 0xFF795548;
            case "בז": return 0xFFEEE8AA;
            case "צהוב": return 0xFFFFEB3B;
            case "כתום": return 0xFFFF9800;
            case "סגול": return 0xFF9C27B0;
            case "ורוד": return 0xFFE91E63;
            case "טורקיז": return 0xFF00BCD4;
            case "זית": return 0xFF808000;
            case "תכלת": return 0xFF81D4FA;
            default: return 0xFF9E9E9E; // אפור כברירת מחדל
        }
    }

    /**
     * פונקציית עזר (אלגוריתם חזותי): בודקת עד כמה צבע הרקע הוא כהה או בהיר,
     * ומחזירה צבע טקסט מנוגד (שחור או לבן) כדי שהטקסט תמיד יהיה קריא לעין.
     */
    private int getContrastColor(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (0.299 * r + 0.587 * g + 0.114 * b) < 128 ? 0xFFFFFFFF : 0xFF000000;
    }
}