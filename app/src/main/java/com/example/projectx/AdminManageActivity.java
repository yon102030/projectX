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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage);

        lvColors = findViewById(R.id.lvColors);
        btnBack = findViewById(R.id.btnBack);

        // אתחול ראשוני של המילון (כל הצבעים מתחילים ב-0) והרשימה
        colorList.addAll(Arrays.asList(allColors));
        for (String color : allColors) {
            globalColorStats.put(color, 0);
        }

        // חיבור האדפטר
        adapter = new ColorAdapter(this, colorList);
        lvColors.setAdapter(adapter);

        // טעינת הנתונים מפיירבייס
        loadGlobalStatistics();

        btnBack.setOnClickListener(v -> finish());
    }

    // פונקציה חכמה ששואבת את הסטטיסטיקות מכל המשתמשים במסד הנתונים
    private void loadGlobalStatistics() {
        DatabaseService.getInstance().getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                if (users != null) {
                    // מעבר על כל המשתמשים באפליקציה
                    for (User u : users) {
                        Map<String, Integer> userStats = u.getColorStats();
                        if (userStats != null) {
                            // מוסיפים את הלחיצות של המשתמש לסך הכל הגלובלי
                            for (Map.Entry<String, Integer> entry : userStats.entrySet()) {
                                String colorName = entry.getKey();
                                int count = entry.getValue();
                                globalColorStats.put(colorName, globalColorStats.getOrDefault(colorName, 0) + count);
                            }
                        }
                    }

                    // מיון הרשימה: מהצבע עם הכי הרבה לחיצות להכי מעט (Leaderboard)
                    colorList.sort((c1, c2) -> Integer.compare(
                            globalColorStats.getOrDefault(c2, 0),
                            globalColorStats.getOrDefault(c1, 0)
                    ));

                    // רענון הרשימה במסך
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AdminManageActivity.this, "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==========================================
    // מחלקת מתאם (Adapter)
    // ==========================================
    private class ColorAdapter extends ArrayAdapter<String> {

        public ColorAdapter(Context context, List<String> colors) {
            super(context, R.layout.color_item_row, colors);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.color_item_row, parent, false);
            }

            String colorName = getItem(position);
            TextView tvName = convertView.findViewById(R.id.tvColorName);
            TextView tvCount = convertView.findViewById(R.id.tvColorCount); // השדה החדש
            View colorCircle = convertView.findViewById(R.id.viewColorCircle);

            tvName.setText(colorName);

            // הצגת כמות הלחיצות (שליפה מהמילון הגלובלי)
            int count = globalColorStats.getOrDefault(colorName, 0);
            tvCount.setText(count + " בחירות");

            int colorValue = getColorValue(colorName);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(colorValue);
            shape.setStroke(2, 0xFF000000);

            colorCircle.setBackground(shape);

            return convertView;
        }

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
                default: return 0xFF9E9E9E;
            }
        }
    }
}