package com.example.projectx.adapter;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// מחלקה זו משמשת כ"מתאם" להצגת רשימת הצבעים הפופולריים.
// היא לוקחת רשימה של שמות צבעים והופכת אותם ל"תגיות" מעוגלות (כמו כדורים קטנים או כפתורים).
public class TopColorsAdapter extends RecyclerView.Adapter<TopColorsAdapter.Holder> {

    private List<String> colors;

    public TopColorsAdapter(List<String> colors) {
        this.colors = colors;
    }

    // מחלקה פנימית שמחזיקה את רכיב התצוגה של כל שורה (במקרה שלנו - רק TextView בודד)
    static class Holder extends RecyclerView.ViewHolder {
        TextView tv;

        public Holder(TextView v) {
            super(v);
            tv = v;
        }
    }

    // פונקציה זו רצה כשצריך לייצר "תגית" חדשה של צבע
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // 🔥 טריק מעניין: במקום לנפח קובץ XML, אנחנו יוצרים תיבת טקסט (TextView) חדשה לגמרי בקוד
        TextView tv = new TextView(parent.getContext());

        // הגדרות עיצוב לטקסט: גודל, כתב מודגש (Bold) וריווח פנימי (Padding)
        tv.setTextSize(15);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(27, 16, 27, 16);

        // הגדרת מאפייני הפריסה (Layout) - איך התיבה תשב בתוך הרשימה
        // WRAP_CONTENT אומר שהתיבה תהיה בדיוק בגודל של המילה שבתוכה
        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        // הגדרת שוליים (Margins) כדי שהתגיות לא יידבקו אחת לשנייה
        params.setMargins(10, 6, 10, 6);
        tv.setLayoutParams(params);

        return new Holder(tv);
    }

    // הפונקציה שותלת את הנתונים (הצבע הספציפי) בתוך התגית שהכנו
    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {

        String colorName = colors.get(position); // שליפת שם הצבע מהרשימה
        h.tv.setText(colorName);

        // קריאה לפונקציית העזר כדי לקבל את קוד הצבע האמיתי לפי השם שלו בעברית
        int colorValue = getColorValue(colorName);

        // ציור הרקע של התגית
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(colorValue); // צובע את הרקע
        bg.setCornerRadius(50); // 🔥 עושה את הפינות עגולות מאוד (כמו גלולה)
        bg.setStroke(3, 0xFF0D47A1); // מוסיף מסגרת כחולה דקה מסביב (עובי 3)

        // החלת הציור שעשינו בתור הרקע של תיבת הטקסט
        h.tv.setBackground(bg);

        // חישוב חכם של בהירות: מפרקים את הצבע לאדום, ירוק וכחול, ומחשבים כמה הוא בהיר.
        int r = (colorValue >> 16) & 0xFF;
        int g = (colorValue >> 8) & 0xFF;
        int b = colorValue & 0xFF;
        double brightness = (0.299 * r + 0.587 * g + 0.114 * b);

        // אם הצבע כהה (מתחת ל-128), נשים טקסט לבן. אם הצבע בהיר, נשים טקסט שחור.
        h.tv.setTextColor(brightness < 128 ? 0xFFFFFFFF : 0xFF000000);
    }

    // מחזיר את כמות הצבעים ברשימה
    @Override
    public int getItemCount() {
        return colors.size();
    }

    // פונקציית עזר המתרגמת שם של צבע בעברית לקוד מספרי (Hex) שהאנדרואיד יודע לצייר
    private int getColorValue(String colorName) {

        switch (colorName) {
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
            case "תכלת": return 0xFF81D4FA; // הוספנו את תכלת לכאן!
        }
        // צבע ברירת מחדל אפור במקרה של שגיאה או צבע לא מוכר
        return 0xFF9E9E9E;
    }
}