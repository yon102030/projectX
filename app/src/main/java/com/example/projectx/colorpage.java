package com.example.projectx;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class colorpage extends AppCompatActivity {

    private GridLayout layoutTopColors, layoutBottomColors;
    private Button btnApply;
    private double temperature;
    private boolean isMale;

    private final List<String> selectedTopColors = new ArrayList<>();
    private final List<String> selectedBottomColors = new ArrayList<>();
    private final String[] allColors = {"שחור","לבן","אפור","כחול","כחול כהה","אדום","ירוק","חום","בז","צהוב","כתום","סגול","ורוד","טורקיז","זית"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colorpage);

        // לקבל נתונים מהעמוד הקודם
        temperature = getIntent().getDoubleExtra("TEMPERATURE", 20);
        isMale = getIntent().getBooleanExtra("IS_MALE", true);

        layoutTopColors = findViewById(R.id.layoutTopColors);
        layoutBottomColors = findViewById(R.id.layoutBottomColors);
        btnApply = findViewById(R.id.btnApply);

        // Padding נכון עבור system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            );
            return insets;
        });

        populateColorBoxes(layoutTopColors, selectedTopColors);
        populateColorBoxes(layoutBottomColors, selectedBottomColors);

        btnApply.setOnClickListener(v -> {
            if (selectedTopColors.isEmpty() && selectedBottomColors.isEmpty()) {
                Toast.makeText(this, "בחר לפחות צבע אחד", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(colorpage.this, user2Activity.class);
            intent.putStringArrayListExtra("TOP_COLORS", new ArrayList<>(selectedTopColors));
            intent.putStringArrayListExtra("BOTTOM_COLORS", new ArrayList<>(selectedBottomColors));
            intent.putExtra("TEMPERATURE", temperature);
            intent.putExtra("IS_MALE", isMale);
            startActivity(intent);
        });
    }

    private void populateColorBoxes(GridLayout layout, List<String> selectedColors) {
        for (String colorName : allColors) {
            Button colorButton = new Button(this);
            colorButton.setText(colorName);
            colorButton.setAllCaps(false);
            colorButton.setTypeface(null, android.graphics.Typeface.BOLD); // Bold

            // המרת שם צבע ל־RGB
            int colorValue;
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
            else colorValue = 0xFF9E9E9E;

            colorButton.setBackgroundColor(colorValue);

            // קביעת צבע טקסט בהתאם לבהירות הצבע
            float[] hsv = new float[3];
            android.graphics.Color.colorToHSV(colorValue, hsv);
            if (hsv[2] > 0.6f) { // בהיר
                colorButton.setTextColor(0xFF000000); // שחור
            } else { // כהה
                colorButton.setTextColor(0xFFFFFFFF); // לבן
            }

            // מסגרת שחורה
            android.graphics.drawable.GradientDrawable border = new android.graphics.drawable.GradientDrawable();
            border.setColor(colorValue);
            border.setStroke(2, 0xFF000000); // 2px מסגרת שחורה
            border.setCornerRadius(8f); // פינות מעוגלות
            colorButton.setBackground(border);

            // LayoutParams עבור GridLayout
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = GridLayout.LayoutParams.WRAP_CONTENT;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.setMargins(8, 8, 8, 8);
            colorButton.setLayoutParams(params);

            colorButton.setOnClickListener(v -> {
                if (selectedColors.contains(colorName)) {
                    selectedColors.remove(colorName);
                    colorButton.setAlpha(0.5f); // לא נבחר
                } else {
                    selectedColors.add(colorName);
                    colorButton.setAlpha(1f); // נבחר
                }
            });

            colorButton.setAlpha(0.5f); // ברירת מחדל - לא נבחר
            layout.addView(colorButton);
        }
    }
}