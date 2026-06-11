package com.example.projectx;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectx.model.User;
import com.example.projectx.services.DatabaseService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// מסך מנהל המציג אנליטיקה: איזה צבעים הם הפופולריים ביותר בקרב כלל המשתמשים
public class AdminManageActivity extends AppCompatActivity {

    private ListView lvColors;
    private ImageButton btnBack;
    private ColorAdapter adapter;

    // הרשימה שתוצג בפועל (עכשיו היא דינמית כדי שנוכל למיין אותה)
    private final List<String> colorList = new ArrayList<>();

    // מילון שומר את כמות הפעמים שכל צבע נבחר
    private final Map<String, Integer> globalColorStats = new HashMap<>();

    private final String[] allColors = {
            "שחור","לבן","אפור","כחול","כחול כהה","אדום","ירוק","חום",
            "בז","צהוב","כתום","סגול","ורוד","טורקיז","זית","תכלת"
    };

    /**
     * פעולת האתחול של המסך. מופעלת בעת יצירת ה-Activity.
     * כאן אנו מאתחלים את המשתנים, מחברים את ה-UI ומפעילים את השליפה מ-Firebase.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage);

        lvColors = findViewById(R.id.lvColors);
        btnBack = findViewById(R.id.btnBack);

        // אתחול ראשוני של המילון (כל הצבעים מתחילים ב-0) והרשימה.
        // זה חשוב כדי שגם צבעים שאף אחד עדיין לא בחר, יופיעו ברשימה עם הערך 0.
        colorList.addAll(Arrays.asList(allColors));
        for (String color : allColors) {
            globalColorStats.put(color, 0);
        }

        // חיבור האדפטר (המתאם) בין רשימת הצבעים (colorList) לבין רכיב ה-ListView שעל המסך
        adapter = new ColorAdapter(this, colorList);
        lvColors.setAdapter(adapter);

        // קריאה לפעולה שאחראית לטעון את הנתונים מ-Firebase
        loadGlobalStatistics();

        // סגירת המסך הנוכחי וחזרה למסך הקודם
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * פונקציה חכמה ששואבת את הסטטיסטיקות מכל המשתמשים במסד הנתונים ומשלבת אותן.
     * זוהי פעולת "משיכה" (Read) אסינכרונית מה-Firebase.
     */
    private void loadGlobalStatistics() {
        // קריאה לשירות ה-Database כדי לקבל את רשימת כל המשתמשים באפליקציה
        DatabaseService.getInstance().getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                if (users != null) {
                    // מעבר (לולאה) על כל משתמש ומשתמש מתוך רשימת המשתמשים שהתקבלה מהשרת
                    for (User u : users) {
                        // לכל משתמש יש מילון אישי משלו של צבעים וכמה פעמים הוא לחץ עליהם
                        Map<String, Integer> userStats = u.getColorStats();
                        if (userStats != null) {
                            // מעבר על המילון האישי של המשתמש הנוכחי
                            for (Map.Entry<String, Integer> entry : userStats.entrySet()) {
                                String colorName = entry.getKey();
                                int count = entry.getValue();

                                // פעולת הסכימה: לוקחים את הכמות הקיימת במילון הגלובלי (globalColorStats)
                                // ומוסיפים לה את הכמות שהמשתמש הנוכחי לחץ.
                                globalColorStats.put(colorName, globalColorStats.getOrDefault(colorName, 0) + count);
                            }
                        }
                    }

                    // מיון הרשימה: מהצבע עם הכי הרבה לחיצות להכי מעט (Leaderboard).
                    // שימוש ב-Comparator מותאם אישית שמשווה בין הערכים (הלחיצות) המאוחסנים במילון הגלובלי.
                    colorList.sort((c1, c2) -> Integer.compare(
                            globalColorStats.getOrDefault(c2, 0),
                            globalColorStats.getOrDefault(c1, 0)
                    ));

                    // רענון הרשימה במסך. פעולה זו מודיעה לאדפטר שהנתונים (colorList) השתנו
                    // ושעליו לצייר את הרשימה מחדש.
                    adapter.notifyDataSetChanged();
                }
            }

            // במקרה של שגיאה בתקשורת או בשליפה, נציג הודעה למנהל
            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AdminManageActivity.this, "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==========================================
    // מחלקת מתאם (Adapter)
    // ==========================================

    /**
     * מחלקה פנימית היורשת מ-ArrayAdapter.
     * תפקידה: לקחת פריט אחד מרשימת ה-colorList (שם הצבע) ולצייר עבורו שורה מעוצבת ב-ListView.
     */
    private class ColorAdapter extends ArrayAdapter<String> {

        // בנאי האדפטר: מעביר למחלקת האב את ההקשר (Context), את עיצוב השורה (XML) ואת הרשימה
        public ColorAdapter(Context context, List<String> colors) {
            super(context, R.layout.color_item_row, colors);
        }

        /**
         * פעולה שנקראת עבור כל שורה ברשימה (ListView) כדי לייצר את התצוגה שלה.
         * @param position - המיקום (אינדקס) של הפריט הנוכחי ברשימה
         * @param convertView - השורה המעוצבת (אם כבר נוצרה בעבר ויש למחזר אותה)
         * @param parent - הרכיב המכיל את השורות (ListView)
         * @return View - רכיב ה-View השלם המייצג שורה אחת
         */
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            // יעילות: אם convertView הוא null (כלומר שורה חדשה לחלוטין), אנחנו 'מנפחים' (Inflate) את ה-XML
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.color_item_row, parent, false);
            }

            // 1. קבלת הנתונים (מאינדקס position)
            String colorName = getItem(position);

            // 2. קישור למרכיבי ה-UI בשורה הספציפית
            TextView tvName = convertView.findViewById(R.id.tvColorName);
            TextView tvCount = convertView.findViewById(R.id.tvColorCount); // השדה החדש
            View colorCircle = convertView.findViewById(R.id.viewColorCircle);

            // 3. הצבת הנתונים ב-UI
            tvName.setText(colorName);

            // הצגת כמות הלחיצות (שליפה מהמילון הגלובלי לפי שם הצבע)
            int count = globalColorStats.getOrDefault(colorName, 0);
            tvCount.setText(count + " בחירות");

            // ציור עיגול הצבע באופן דינמי
            int colorValue = getColorValue(colorName);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL); // הגדרת הצורה לאליפסה/עיגול
            shape.setColor(colorValue); // מילוי הצבע
            shape.setStroke(2, 0xFF000000); // מסגרת שחורה דקה

            // החלת הציור (GradientDrawable) כרקע של רכיב ה-View העגול
            colorCircle.setBackground(shape);

            return convertView;
        }

        /**
         * פעולת עזר המתרגמת שם של צבע בעברית ("שחור") לקוד צבע דיגיטלי (Hexadecimal)
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
                default: return 0xFF9E9E9E; // במקרה שלא נמצאה התאמה, נחזיר צבע ברירת מחדל אפור
            }
        }
    }
}