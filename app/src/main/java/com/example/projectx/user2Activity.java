package com.example.projectx;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectx.model.Clothe;
import com.example.projectx.model.Outfit;
import com.example.projectx.services.DatabaseService;
import com.example.projectx.util.ImageUtil;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class user2Activity extends AppCompatActivity {

    private ImageView ivTop, ivOuter, ivBottom;
    private Button btnRefresh, btnSaveLook, btnSaved;

    private LinearLayout rowTop, rowBottom;

    private final Random random = new Random();

    private double temperature;
    private boolean isMale; // 🔥 חדש

    private List<String> topColors, bottomColors;
private  ImageButton btnBack;
    private List<Clothe> filteredClothes = new ArrayList<>();
    private List<Clothe> topClothes = new ArrayList<>();
    private List<Clothe> outerClothes = new ArrayList<>();
    private List<Clothe> bottomClothes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user2);

        ivTop = findViewById(R.id.ivTop);
        ivOuter = findViewById(R.id.ivTopLayer);
        ivBottom = findViewById(R.id.ivbButtom);

        rowTop = findViewById(R.id.row_top);
        rowBottom = findViewById(R.id.row_bottom);

        btnRefresh = findViewById(R.id.btnRefresh);
        btnSaveLook = findViewById(R.id.btnSaveLook);
        btnSaved = findViewById(R.id.btnSavedLooks);

        temperature = getIntent().getDoubleExtra("TEMPERATURE", 20);
        isMale = getIntent().getBooleanExtra("IS_MALE", true); // 🔥 קבלת מגדר

        topColors = getIntent().getStringArrayListExtra("TOP_COLORS");
        bottomColors = getIntent().getStringArrayListExtra("BOTTOM_COLORS");
        btnBack = findViewById(R.id.btnBack);

        loadClothes();


// חזרה אחורה
        btnBack.setOnClickListener(v -> {
            finish();
        });

// מעבר למסך userpage (בית בתוך האפליקציה)

        btnRefresh.setOnClickListener(v -> setRandomLook());
        btnSaveLook.setOnClickListener(v -> saveLook());
        btnSaved.setOnClickListener(v -> {
            Intent intent = new Intent(this, savedlooks.class);
            startActivity(intent);
        });
    }

    // ================= LOAD =================
    private void loadClothes() {

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseService.getInstance().getUserClothes(userId,
                new DatabaseService.DatabaseCallback<List<Clothe>>() {

                    @Override
                    public void onCompleted(List<Clothe> clothes) {

                        if (clothes == null) return;

                        filteredClothes = filterClothes(clothes);
                        separateClothes(filteredClothes);
                        populateRows();
                        setRandomLook();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(user2Activity.this,
                                "Error loading clothes",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ================= FILTER =================
    private List<Clothe> filterClothes(List<Clothe> clothes) {

        List<Clothe> filtered = new ArrayList<>();

        String season;
        if (temperature >= 25) season = "קיץ";
        else if (temperature >= 20) season = "אביב";
        else if (temperature >= 15) season = "סתיו";
        else season = "חורף";

        boolean isMale = getIntent().getBooleanExtra("IS_MALE", true);

        for (Clothe c : clothes) {

            if (c == null) continue;

            // 🔥 סינון לפי מגדר (לפי isFavorite)
            boolean genderMatch;
            if (isMale) {
                genderMatch = c.isFavorite(); // גבר = מועדף
            } else {
                genderMatch = !c.isFavorite(); // אישה = לא מועדף
            }

            boolean seasonMatch =
                    c.getSeason() == null ||
                            c.getSeason().equalsIgnoreCase("All") ||
                            c.getSeason().contains(season);

            boolean colorMatch = isColorMatch(c);

            if (seasonMatch && colorMatch && genderMatch) {
                filtered.add(c);
            }
        }

        return filtered;
    }

    // ================= COLOR =================
    private boolean isColorMatch(Clothe c) {

        String color = c.getColor();
        if (color == null) return true;

        List<String> list =
                (isTop(c.getType()) || isOuter(c.getType()))
                        ? topColors
                        : bottomColors;

        if (list == null || list.isEmpty()) return true;

        for (String s : list) {
            if (s != null && s.equalsIgnoreCase(color.trim())) {
                return true;
            }
        }
        return false;
    }

    // ================= POPULATE =================
    private void populateRows() {

        rowTop.removeAllViews();
        rowBottom.removeAllViews();

        for (Clothe c : filteredClothes) {

            ImageView img = new ImageView(this);

            int size = (int) getResources().getDimension(R.dimen.item_thumbnail);
            if (size == 0) size = 140;

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(size, size);

            params.setMargins(10, 10, 10, 10);
            img.setLayoutParams(params);

            img.setScaleType(ImageView.ScaleType.CENTER_CROP);

            Bitmap bmp = ImageUtil.convertFrom64base(c.getImageUrl());
            img.setImageBitmap(bmp);

            img.setOnClickListener(v -> {

                if (isTop(c.getType())) ivTop.setImageBitmap(bmp);
                else if (isOuter(c.getType())) ivOuter.setImageBitmap(bmp);
                else ivBottom.setImageBitmap(bmp);
            });

            if (isTop(c.getType()) || isOuter(c.getType())) {
                rowTop.addView(img);
            } else {
                rowBottom.addView(img);
            }
        }
    }

    // ================= SEPARATE =================
    private void separateClothes(List<Clothe> clothes) {

        topClothes.clear();
        outerClothes.clear();
        bottomClothes.clear();

        for (Clothe c : clothes) {

            if (isOuter(c.getType())) {
                outerClothes.add(c);
            } else if (isTop(c.getType())) {
                topClothes.add(c);
            } else {
                bottomClothes.add(c);
            }
        }
    }

    // ================= RANDOM =================
    private void setRandomLook() {

        if (!topClothes.isEmpty()) {
            Clothe top = topClothes.get(random.nextInt(topClothes.size()));
            ivTop.setImageBitmap(ImageUtil.convertFrom64base(top.getImageUrl()));
        }

        if (!outerClothes.isEmpty() && temperature < 20) {
            Clothe outer = outerClothes.get(random.nextInt(outerClothes.size()));
            ivOuter.setVisibility(View.VISIBLE);
            ivOuter.setImageBitmap(ImageUtil.convertFrom64base(outer.getImageUrl()));
        } else {
            ivOuter.setVisibility(View.GONE);
        }

        if (!bottomClothes.isEmpty()) {
            Clothe bottom = bottomClothes.get(random.nextInt(bottomClothes.size()));
            ivBottom.setImageBitmap(ImageUtil.convertFrom64base(bottom.getImageUrl()));
        }
    }

    // ================= SAVE =================
    private void saveLook() {

        String outfitId = DatabaseService.getInstance().generateOutfitId();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String top = ImageUtil.convertTo64Base(ivTop);

        String outer = ivOuter.getVisibility() == View.VISIBLE
                ? ImageUtil.convertTo64Base(ivOuter)
                : null;

        String bottom = ImageUtil.convertTo64Base(ivBottom);

        Outfit outfit = new Outfit(outfitId, userId, top, outer, bottom);

        DatabaseService.getInstance().createNewOutfit(outfit,
                new DatabaseService.DatabaseCallback<Void>() {

                    @Override
                    public void onCompleted(Void object) {

                        Toast.makeText(user2Activity.this,
                                "Saved!",
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(user2Activity.this, savedlooks.class));
                        finish();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(user2Activity.this,
                                "Error saving",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ================= TYPES =================
    private boolean isTop(String type) {
        return type != null && (
                type.equals("חולצה קצרה") ||
                        type.equals("חולצה ארוכה") ||
                        type.equals("גופייה")
        );
    }

    private boolean isOuter(String type) {
        return type != null && (
                type.equals("מעיל") ||
                        type.equals("קפוצון") ||
                        type.equals("זקט") ||
                        type.equals("סוודר")
        );
    }
}