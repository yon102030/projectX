package com.example.projectx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
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

public class colorpage extends AppCompatActivity {

    private GridLayout layoutTopColors, layoutBottomColors;
    private Button btnApply, btnAllTop, btnAllB;

    private double temperature;
    private boolean isMale;

    private final List<String> selectedTopColors = new ArrayList<>();
    private final List<String> selectedBottomColors = new ArrayList<>();

    private final String[] allColors = {
            "שחור","לבן","אפור","כחול","כחול כהה","אדום","ירוק","חום",
            "בז","צהוב","כתום","סגול","ורוד","טורקיז","זית"
    };

    private final List<Button> topButtons = new ArrayList<>();
    private final List<Button> bottomButtons = new ArrayList<>();
    private boolean isTopAllSelected = false;
    private boolean isBottomAllSelected = false;

    // 🔥 TOP COLORS TRACKING
    private final Map<String, Integer> colorClicks = new HashMap<>();
    private final List<String> topColorsList = new ArrayList<>();
    private TopColorsAdapter topColorsAdapter;

    private RecyclerView rvTopColors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colorpage);

        temperature = getIntent().getDoubleExtra("TEMPERATURE", 20);
        isMale = getIntent().getBooleanExtra("IS_MALE", true);

        layoutTopColors = findViewById(R.id.layoutTopColors);
        layoutBottomColors = findViewById(R.id.layoutBottomColors);

        btnApply = findViewById(R.id.btnApply);
        btnAllB = findViewById(R.id.btnSelectAllBColors);
        btnAllTop = findViewById(R.id.btnSelectAllTopColors);

        rvTopColors = findViewById(R.id.recyclerTopColors);

        rvTopColors.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        topColorsAdapter = new TopColorsAdapter(topColorsList);
        rvTopColors.setAdapter(topColorsAdapter);

        // ✅ טעינת נתונים שמורים
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

        btnAllTop.setOnClickListener(v -> {

            if (!isTopAllSelected) {

                selectedTopColors.clear();
                selectedTopColors.addAll(Arrays.asList(allColors));

                for (Button b : topButtons) {
                    b.setAlpha(1f);
                }

                isTopAllSelected = true;
                Toast.makeText(this, "נבחרו כל צבעי העליונים", Toast.LENGTH_SHORT).show();

            } else {

                selectedTopColors.clear();

                for (Button b : topButtons) {
                    b.setAlpha(0.5f);
                }

                isTopAllSelected = false;
                Toast.makeText(this, "בוטלה בחירת כל העליונים", Toast.LENGTH_SHORT).show();
            }
        });

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

            android.graphics.drawable.GradientDrawable border =
                    new android.graphics.drawable.GradientDrawable();

            border.setColor(colorValue);
            border.setStroke(2, 0xFF000000);
            border.setCornerRadius(8f);

            colorButton.setBackground(border);
            colorButton.setAlpha(0.5f);

            colorButton.setOnClickListener(v -> {

                if (selectedColors.contains(colorName)) {

                    selectedColors.remove(colorName);
                    colorButton.setAlpha(0.5f);

                } else {

                    selectedColors.add(colorName);
                    colorButton.setAlpha(1f);

                    // 🔥 שמירה קבועה
                    SharedPreferences prefs = getSharedPreferences("colors", MODE_PRIVATE);
                    int count = prefs.getInt(colorName, 0);
                    prefs.edit().putInt(colorName, count + 1).apply();

                    colorClicks.put(colorName, count + 1);

                    updateTopColors();
                }
            });

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setMargins(8, 8, 8, 8);
            colorButton.setLayoutParams(params);

            layout.addView(colorButton);

            if (layout == layoutTopColors) {
                topButtons.add(colorButton);
            } else {
                bottomButtons.add(colorButton);
            }
        }
    }

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

    private void updateTopColors() {

        topColorsList.clear();

        List<Map.Entry<String, Integer>> sorted =
                new ArrayList<>(colorClicks.entrySet());

        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        int limit = Math.min(5, sorted.size());

        for (int i = 0; i < limit; i++) {
            topColorsList.add(sorted.get(i).getKey());
        }

        topColorsAdapter.notifyDataSetChanged();
    }
}