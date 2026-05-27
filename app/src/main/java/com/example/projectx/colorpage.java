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

    private final Map<String, Integer> colorClicks = new HashMap<>();
    private final List<String> topColorsList = new ArrayList<>();
    private TopColorsAdapter topColorsAdapter;

    private RecyclerView rvTopColors;
    private ImageButton btnBack;

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
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        rvTopColors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        topColorsAdapter = new TopColorsAdapter(topColorsList);
        rvTopColors.setAdapter(topColorsAdapter);

        loadSavedColors();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });

        btnAllTop.setOnClickListener(v -> handleSelectAll(topButtons, selectedTopColors, true));
        btnAllB.setOnClickListener(v -> handleSelectAll(bottomButtons, selectedBottomColors, false));

        populateColorBoxes(layoutTopColors, selectedTopColors);
        populateColorBoxes(layoutBottomColors, selectedBottomColors);

        btnApply.setOnClickListener(v -> {
            if (selectedTopColors.isEmpty() || selectedBottomColors.isEmpty()) {
                Toast.makeText(this, "חייב לבחור צבע לעליונים ולתחתונים", Toast.LENGTH_SHORT).show();
                return;
            }
            saveColorsToFirebaseAndNavigate();
        });
    }

    private void handleSelectAll(List<Button> buttons, List<String> selectedList, boolean isTop) {
        boolean alreadyAll = isTop ? isTopAllSelected : isBottomAllSelected;

        if (!alreadyAll) {
            // המשתמש לחץ על "בחר הכל"
            for (String color : allColors) {
                // נוסיף לספירה רק צבעים שעדיין לא נבחרו (כדי לא לספור פעמיים צבע שהוא כבר לחץ עליו)
                if (!selectedList.contains(color)) {
                    selectedList.add(color);
                    // הוספה לסטטיסטיקה
                    colorClicks.put(color, colorClicks.getOrDefault(color, 0) + 1);
                }
            }
            // הופכים את כל הכפתורים לבולטים
            for (Button b : buttons) b.setAlpha(1f);

        } else {
            // המשתמש לחץ שוב כדי לבטל את בחירת הכל
            selectedList.clear();
            for (Button b : buttons) b.setAlpha(0.6f);
        }


        updateTopColors();

        // עדכון המצב
        if (isTop) isTopAllSelected = !isTopAllSelected;
        else isBottomAllSelected = !isBottomAllSelected;
    }

    private void populateColorBoxes(GridLayout layout, List<String> selectedColors) {
        for (String colorName : allColors) {
            Button colorButton = new Button(this);
            colorButton.setText(colorName);

            // הפיכת הטקסט לעבה וגדול יותר
            colorButton.setTextSize(14);
            colorButton.setTypeface(null, android.graphics.Typeface.BOLD);

            int colorValue = getColorValue(colorName);
            android.graphics.drawable.GradientDrawable border = new android.graphics.drawable.GradientDrawable();
            border.setColor(colorValue);
            border.setCornerRadius(12f);
            colorButton.setBackground(border);
            colorButton.setTextColor(getContrastColor(colorValue));
            colorButton.setAlpha(0.6f);

            colorButton.setOnClickListener(v -> {
                if (selectedColors.contains(colorName)) {
                    selectedColors.remove(colorName);
                    colorButton.setAlpha(0.6f);
                } else {
                    selectedColors.add(colorName);
                    colorButton.setAlpha(1f);
                    colorClicks.put(colorName, colorClicks.getOrDefault(colorName, 0) + 1);
                    updateTopColors();
                }
            });

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(5, 5, 5, 5);
            colorButton.setLayoutParams(params);
            layout.addView(colorButton);
            if (layout == layoutTopColors) topButtons.add(colorButton);
            else bottomButtons.add(colorButton);
        }
    }

    private void loadSavedColors() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseService.getInstance().getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                if (user != null && user.getColorStats() != null) {
                    colorClicks.clear();
                    colorClicks.putAll(user.getColorStats());
                    updateTopColors();
                }
            }
            @Override public void onFailed(Exception e) {}
        });
    }

    private void saveColorsToFirebaseAndNavigate() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseService.getInstance().getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                if (user != null) {
                    user.setColorStats(colorClicks);
                    DatabaseService.getInstance().updateUser(user, new DatabaseService.DatabaseCallback<Void>() {
                        @Override
                        public void onCompleted(Void object) {
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

    private void updateTopColors() {
        topColorsList.clear();
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(colorClicks.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        for (int i = 0; i < Math.min(5, sorted.size()); i++) topColorsList.add(sorted.get(i).getKey());
        topColorsAdapter.notifyDataSetChanged();
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

    private int getContrastColor(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (0.299 * r + 0.587 * g + 0.114 * b) < 128 ? 0xFFFFFFFF : 0xFF000000;
    }
}