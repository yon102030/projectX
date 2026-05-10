package com.example.projectx;

import android.content.Intent;
import android.content.SharedPreferences;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// מסך זה מאפשר למשתמש לבחור צבעים מועדפים לפריטים עליונים ותחתונים.
// בנוסף, המסך עוקב אחרי הצבעים הפופולריים ביותר שנבחרו ומציג אותם.
public class colorpage extends AppCompatActivity {

    private GridLayout layoutTopColors, layoutBottomColors;
    private Button btnApply, btnAllTop, btnAllB;

    private double temperature;
    private boolean isMale;

    private final List<String> selectedTopColors = new ArrayList<>();
    private final List<String> selectedBottomColors = new ArrayList<>();

    private final String[] allColors = {
            "שחור","לבן","אפור","כחול","כחול כהה","אדום","ירוק","חום",
            "בז","צהוב","כתום","סגול","ורוד","טורקיז","זית","תכלת"
    };

    private final List<Button> topButtons = new ArrayList<>();
    private final List<Button> bottomButtons = new ArrayList<>();
    private boolean isTopAllSelected = false;
    private boolean isBottomAllSelected = false;

    // משתנים למעקב אחרי הצבעים הפופולריים ביותר (סטטיסטיקה)
    private final Map<String, Integer> colorClicks = new HashMap<>();
    private final List<String> topColorsList = new ArrayList<>();
    private TopColorsAdapter topColorsAdapter;

    private RecyclerView rvTopColors;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colorpage);

        // משיכת המגדר והטמפרטורה מהמסך הקודם, כדי שנוכל להעביר אותם הלאה בסוף
        temperature = getIntent().getDoubleExtra("TEMPERATURE", 20);
        isMale = getIntent().getBooleanExtra("IS_MALE", true);

        layoutTopColors = findViewById(R.id.layoutTopColors);
        layoutBottomColors = findViewById(R.id.layoutBottomColors);
        btnApply = findViewById(R.id.btnApply);
        btnAllB = findViewById(R.id.btnSelectAllBColors);
        btnAllTop = findViewById(R.id.btnSelectAllTopColors);
        rvTopColors = findViewById(R.id.recyclerTopColors);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // הגדרת הרשימה האופקית שמציגה את 5 הצבעים הפופולריים
        rvTopColors.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        topColorsAdapter = new TopColorsAdapter(topColorsList);
        rvTopColors.setAdapter(topColorsAdapter);

        // קריאה לפונקציה שטוענת את היסטוריית הלחיצות מזיכרון המכשיר
        loadSavedColors();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            );
            return insets;
        });

        // כפתור "בחר הכל" לעליונים: בודק אם כבר הכל נבחר.
        // אם לא - מוסיף את כל הצבעים ומדגיש אותם. אם כן - מנקה הכל ומעמעם את הכפתורים.
        btnAllTop.setOnClickListener(v -> {
            if (!isTopAllSelected) {
                selectedTopColors.clear();
                selectedTopColors.addAll(Arrays.asList(allColors));
                for (Button b : topButtons) {
                    b.setAlpha(1f); // 1f = אטום לגמרי (נבחר)
                }
                isTopAllSelected = true;
                Toast.makeText(this, "נבחרו כל צבעי העליונים", Toast.LENGTH_SHORT).show();
            } else {
                selectedTopColors.clear();
                for (Button b : topButtons) {
                    b.setAlpha(0.5f); // 0.5f = חצי שקוף (לא נבחר)
                }
                isTopAllSelected = false;
                Toast.makeText(this, "בוטלה בחירת כל העליונים", Toast.LENGTH_SHORT).show();
            }
        });

        // כפתור "בחר הכל" לתחתונים (אותה לוגיקה כמו בעליונים)
        btnAllB.setOnClickListener(v -> {
            if (!isBottomAllSelected) {
                selectedBottomColors.clear();
                selectedBottomColors.addAll(Arrays.asList(allColors));
                for (Button b : bottomButtons) {
                    b.setAlpha(1f);
                }
                isBottomAllSelected = true;
                Toast.makeText(this, "נבחרו כל צבעי התחתונים", Toast.LENGTH_SHORT).show();
            } else {
                selectedBottomColors.clear();
                for (Button b : bottomButtons) {
                    b.setAlpha(0.5f);
                }
                isBottomAllSelected = false;
                Toast.makeText(this, "בוטלה בחירת כל התחתונים", Toast.LENGTH_SHORT).show();
            }
        });

        // הפעלת הפונקציה החכמה שמציירת את הכפתורים על המסך (פעם אחת לעליונים ופעם לתחתונים)
        populateColorBoxes(layoutTopColors, selectedTopColors);
        populateColorBoxes(layoutBottomColors, selectedBottomColors);

        // כפתור האישור והמעבר למסך הבא
        btnApply.setOnClickListener(v -> {
            boolean topValid = selectedTopColors.size() > 0;
            boolean bottomValid = selectedBottomColors.size() > 0;

            // בדיקות תקינות: אי אפשר להמשיך אם לא נבחר לפחות צבע אחד מכל סוג
            if (!topValid && !bottomValid) {
                Toast.makeText(this, "חייב לבחור לפחות צבע לעליונים ותחתונים", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!topValid) {
                Toast.makeText(this, "בחר לפחות צבע לעליונים", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!bottomValid) {
                Toast.makeText(this, "בחר לפחות צבע לתחתונים", Toast.LENGTH_SHORT).show();
                return;
            }

            // אם הכל תקין, אורזים את כל הנתונים (צבעים, טמפרטורה ומגדר) ועוברים למסך user2Activity
            try {
                Intent intent = new Intent(colorpage.this, user2Activity.class);
                intent.putStringArrayListExtra("TOP_COLORS", new ArrayList<>(selectedTopColors));
                intent.putStringArrayListExtra("BOTTOM_COLORS", new ArrayList<>(selectedBottomColors));
                intent.putExtra("TEMPERATURE", temperature);
                intent.putExtra("IS_MALE", isMale);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "שגיאה במעבר לעמוד הבא", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // פונקציה חכמה שרצה על רשימת הצבעים ומייצרת כפתור לכל צבע באופן דינמי מקוד ה-Java
    private void populateColorBoxes(GridLayout layout, List<String> selectedColors) {
        for (String colorName : allColors) {
            Button colorButton = new Button(this);
            colorButton.setText(colorName);
            colorButton.setAllCaps(false);
            colorButton.setTypeface(null, android.graphics.Typeface.BOLD);
            colorButton.setTextSize(10);
            colorButton.setPadding(5,5,5,5);

            int colorValue;

            // המרת שם הצבע מקוד טקסט לקוד צבע אמיתי
            if (colorName.equals("שחור")) colorValue = 0xFF000000;
            else if (colorName.equals("לבן")) colorValue = 0xFFFFFFFF;
            else if (colorName.equals("אפור")) colorValue = 0xFF808080;
            else if (colorName.equals("כחול")) colorValue = 0xFF2196F3;
            else if (colorName.equals("כחול כהה")) colorValue = 0xFF1565C0;
            else if (colorName.equals("אדום")) colorValue = 0xFFF44336;
            else if (colorName.equals("ירוק")) colorValue = 0xFF4CAF50;
            else if (colorName.equals("חום")) colorValue = 0xFF795548;
            else if (colorName.equals("בז")) colorValue = 0xFFEEE8AA;
            else if (colorName.equals("צהוב")) colorValue = 0xFFFFEB3B;
            else if (colorName.equals("כתום")) colorValue = 0xFFFF9800;
            else if (colorName.equals("סגול")) colorValue = 0xFF9C27B0;
            else if (colorName.equals("ורוד")) colorValue = 0xFFE91E63;
            else if (colorName.equals("טורקיז")) colorValue = 0xFF00BCD4;
            else if (colorName.equals("זית")) colorValue = 0xFF808000;
            else if (colorName.equals("תכלת")) colorValue = 0xFF81D4FA;
            else colorValue = 0xFF9E9E9E;

            // עיצוב הכפתור עם צבע הרקע ופינות עגולות
            android.graphics.drawable.GradientDrawable border =
                    new android.graphics.drawable.GradientDrawable();
            border.setColor(colorValue);
            border.setStroke(2, 0xFF000000);
            border.setCornerRadius(12f);
            colorButton.setBackground(border);

            // חישוב חכם של בהירות צבע הרקע:
            // אם הרקע כהה (מתחת ל-128), הטקסט יהיה לבן. אם הרקע בהיר, הטקסט יהיה שחור.
            int r = (colorValue >> 16) & 0xFF;
            int g = (colorValue >> 8) & 0xFF;
            int b = colorValue & 0xFF;
            double brightness = (0.299 * r + 0.587 * g + 0.114 * b);

            if (brightness < 128) {
                colorButton.setTextColor(0xFFFFFFFF);
            } else {
                colorButton.setTextColor(0xFF000000);
            }

            colorButton.setAlpha(0.6f); // כברירת מחדל הכפתור מתחיל מעומעם (לא נבחר)

            // אירוע לחיצה על כפתור של צבע מסוים
            colorButton.setOnClickListener(v -> {
                if (selectedColors.contains(colorName)) {
                    // אם כבר נבחר בעבר -> מסירים מהרשימה ומעמעמים את הכפתור
                    selectedColors.remove(colorName);
                    colorButton.setAlpha(0.6f);
                } else {
                    // אם טרם נבחר -> מוסיפים לרשימה, עושים אותו אטום (בולט)
                    selectedColors.add(colorName);
                    colorButton.setAlpha(1f);

                    // עדכון הסטטיסטיקה בזיכרון המכשיר (SharedPreferences) על הפופולריות של הצבע הזה
                    SharedPreferences prefs = getSharedPreferences("colors", MODE_PRIVATE);
                    int count = prefs.getInt(colorName, 0);
                    prefs.edit().putInt(colorName, count + 1).apply();
                    colorClicks.put(colorName, count + 1);

                    // רענון רשימת הצבעים הפופולריים המוצגת למעלה
                    updateTopColors();
                }
            });

            // הגדרת מאפייני תצוגה (רוחב, שוליים) בתוך הגריד (הטבלה)
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(5, 5, 5, 5);
            colorButton.setLayoutParams(params);

            layout.addView(colorButton);

            // שמירת הכפתור במערך בהתאם למיקום שלו (למעלה או למטה) לשימוש ב"בחר הכל"
            if (layout == layoutTopColors) {
                topButtons.add(colorButton);
            } else {
                bottomButtons.add(colorButton);
            }
        }
    }

    // פונקציה ששואבת את כמות הלחיצות ההיסטורית מהזיכרון המקומי
    private void loadSavedColors() {
        SharedPreferences prefs = getSharedPreferences("colors", MODE_PRIVATE);
        for (String color : allColors) {
            int count = prefs.getInt(color, 0);
            if (count > 0) {
                colorClicks.put(color, count);
            }
        }
        updateTopColors();
    }

    // פונקציה שלוקחת את הסטטיסטיקה, ממיינת ומציגה את 5 הצבעים הכי פופולריים
    private void updateTopColors() {
        topColorsList.clear();

        List<Map.Entry<String, Integer>> sorted =
                new ArrayList<>(colorClicks.entrySet());

        // מיון מהמספר הגדול ביותר לקטן ביותר
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        int limit = Math.min(5, sorted.size());

        for (int i = 0; i < limit; i++) {
            topColorsList.add(sorted.get(i).getKey());
        }

        topColorsAdapter.notifyDataSetChanged(); // עדכון התצוגה הגרפית
    }
}