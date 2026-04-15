package com.example.projectx;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projectx.adapter.TopColorsAdapter;
import com.example.projectx.model.Clothe;
import com.example.projectx.model.Outfit;
import com.example.projectx.model.User;
import com.example.projectx.services.DatabaseService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminStatsActivity extends AppCompatActivity {

    private TextView tvTotalUsers, tvTotalClothes, tvSeasonStats;
    private DatabaseService databaseService;
    private ImageButton btnBack;

    private RecyclerView rvAdminTopColors;
    private TopColorsAdapter topColorsAdapter;
    private List<String> topColorsList = new ArrayList<>();
    private Map<String, Integer> colorClicks = new HashMap<>();

    private final String[] allColors = {
            "שחור","לבן","אפור","כחול","כחול כהה","אדום","ירוק","חום",
            "בז","צהוב","כתום","סגול","ורוד","טורקיז","זית"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_stats);

        initViews();
        loadStats();
        loadTopColorsData();
    }

    private void initViews() {
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalClothes = findViewById(R.id.tvTotalClothes);
        tvSeasonStats = findViewById(R.id.tvSeasonStats);
        btnBack = findViewById(R.id.btnBack);
        rvAdminTopColors = findViewById(R.id.rvAdminTopColors);

        databaseService = DatabaseService.getInstance();

        btnBack.setOnClickListener(v -> finish());

        rvAdminTopColors.setLayoutManager(new LinearLayoutManager(this));
        topColorsAdapter = new TopColorsAdapter(topColorsList);
        rvAdminTopColors.setAdapter(topColorsAdapter);
    }

    private void loadStats() {
        // ספירת משתמשים
        databaseService.getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                if (users != null) tvTotalUsers.setText("משתמשים רשומים: " + users.size());
            }
            @Override
            public void onFailed(Exception e) {}
        });

        // ספירת בגדים
        databaseService.getClotheList(new DatabaseService.DatabaseCallback<List<Clothe>>() {
            @Override
            public void onCompleted(List<Clothe> clothes) {
                if (clothes != null) tvTotalClothes.setText("פריטים במערכת: " + clothes.size());
            }
            @Override
            public void onFailed(Exception e) {}
        });

        // ספירת אאוטפיטים לפי הלוגיקה של מסך savedlooks
        databaseService.getOutfitList(new DatabaseService.DatabaseCallback<List<Outfit>>() {
            @Override
            public void onCompleted(List<Outfit> outfits) {
                if (outfits != null) {
                    int summerCount = 0;
                    int winterCount = 0;

                    for (Outfit outfit : outfits) {
                        // אם יש פריט עליון (מעיל/ז'קט) -> חורף
                        if (outfit.getOuter() != null) {
                            winterCount++;
                        } else {
                            // אם אין פריט עליון -> קיץ
                            summerCount++;
                        }
                    }
                    tvSeasonStats.setText("אאוטפיטים קיציים: " + summerCount + " | אאוטפיטים חורפיים: " + winterCount);
                }
            }

            @Override
            public void onFailed(Exception e) {
                tvSeasonStats.setText("שגיאה בטעינת נתונים");
            }
        });
    }

    private void loadTopColorsData() {
        SharedPreferences prefs = getSharedPreferences("colors", MODE_PRIVATE);
        colorClicks.clear();
        for (String color : allColors) {
            int count = prefs.getInt(color, 0);
            if (count > 0) colorClicks.put(color, count);
        }
        updateTopColorsList();
    }

    private void updateTopColorsList() {
        topColorsList.clear();
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(colorClicks.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        int limit = Math.min(5, sorted.size());
        for (int i = 0; i < limit; i++) {
            topColorsList.add(sorted.get(i).getKey());
        }
        topColorsAdapter.notifyDataSetChanged();
    }
}