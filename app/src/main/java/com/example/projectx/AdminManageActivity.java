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

public class AdminManageActivity extends AppCompatActivity {

    private ListView lvColors;
    private ImageButton btnBack;
    private final String[] allColors = {
            "שחור","לבן","אפור","כחול","כחול כהה","אדום","ירוק","חום",
            "בז","צהוב","כתום","סגול","ורוד","טורקיז","זית"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage);

        lvColors = findViewById(R.id.lvColors);
        btnBack = findViewById(R.id.btnBack);

        ColorAdapter adapter = new ColorAdapter(this, allColors);
        lvColors.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
    }

    // אדפטר פנימי לעיצוב הרשימה עם עיגולי צבע
    private class ColorAdapter extends ArrayAdapter<String> {
        public ColorAdapter(Context context, String[] colors) {
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
            View colorCircle = convertView.findViewById(R.id.viewColorCircle);

            tvName.setText(colorName);

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
                default: return 0xFF9E9E9E;
            }
        }
    }
}