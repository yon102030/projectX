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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

// מסך המאפשר למנהל (Admin) לראות את רשימת הצבעים הזמינים במערכת, כולל תצוגה ויזואלית
public class AdminManageActivity extends AppCompatActivity {

    private ListView lvColors;
    private ImageButton btnBack;

    // מערך קבוע (Hardcoded) שמכיל את כל שמות הצבעים שקיימים באפליקציה
    private final String[] allColors = {
            "שחור","לבן","אפור","כחול","כחול כהה","אדום","ירוק","חום",
            "בז","צהוב","כתום","סגול","ורוד","טורקיז","זית","תכלת"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage);

        // קישור הרכיבים מהעיצוב (XML) לקוד
        lvColors = findViewById(R.id.lvColors);
        btnBack = findViewById(R.id.btnBack);

        // יצירת ה"מתאם" (Adapter) האישי שלנו - הוא זה שיודע לקחת את רשימת הטקסטים
        // ולהפוך אותם לשורות מעוצבות בתוך ה-ListView
        ColorAdapter adapter = new ColorAdapter(this, allColors);
        lvColors.setAdapter(adapter);

        // סגירת המסך בלחיצה על חזור
        btnBack.setOnClickListener(v -> finish());
    }

    // ==========================================
    // מחלקת מתאם (Adapter) פנימית ומותאמת אישית
    // ==========================================
    private class ColorAdapter extends ArrayAdapter<String> {

        public ColorAdapter(Context context, String[] colors) {
            super(context, R.layout.color_item_row, colors);
        }

        // פונקציה זו נקראת עבור כל שורה ושורה ברשימה, ותפקידה לבנות את העיצוב שלה
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            // "מיחזור שורות": אנדרואיד חכמה ולא בונה שורות חדשות אם אפשר למחזר שורות שנגללו מחוץ למסך.
            // אם אין שורה ממוחזרת (null), ניצור אחת חדשה מקובץ העיצוב color_item_row
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.color_item_row, parent, false);
            }

            // משיכת הרכיבים של השורה הספציפית הזו
            String colorName = getItem(position);
            TextView tvName = convertView.findViewById(R.id.tvColorName);
            View colorCircle = convertView.findViewById(R.id.viewColorCircle);

            tvName.setText(colorName);

            // לוגיקה מעניינת: במקום להכין תמונה מראש לכל צבע, אנחנו "מציירים" עיגול ישירות בקוד!
            int colorValue = getColorValue(colorName); // שליפת קוד הצבע המדויק לפי השם שלו
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL); // הגדרת הצורה לאליפסה/עיגול
            shape.setColor(colorValue); // צביעת העיגול בצבע המתאים
            shape.setStroke(2, 0xFF000000); // הוספת מסגרת שחורה דקה (חשוב כדי שצבע כמו לבן לא יבלע ברקע)

            // החלת העיגול שציירנו כרקע ל-View הקטן שיש לנו בשורה
            colorCircle.setBackground(shape);

            return convertView;
        }

        // פונקציית עזר המתרגמת את שם הצבע בעברית לקוד צבע (Hex) שהמערכת יודעת לצייר איתו
        // לדוגמה: 0xFFFFFFFF אומר "צבע אטום לגמרי (FF) בצבע לבן (FFFFFF)"
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

                // צבע ברירת מחדל (אפור בינוני) במקרה של תקלה או צבע לא מוכר
                default: return 0xFF9E9E9E;
            }
        }
    }
}