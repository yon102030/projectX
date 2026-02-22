package com.example.projectx;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectx.model.Clothe;
import com.example.projectx.services.DatabaseService;
import com.example.projectx.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class user2Activity extends AppCompatActivity {

    private LinearLayout rowTop, rowBottom;
    private ImageView ivTop, ivButtom;
    private Button btnRefresh;

    private double temperature;
    private boolean isMale;

    private List<Clothe> filteredClothes = new ArrayList<>();
    private List<Clothe> topClothes = new ArrayList<>();
    private List<Clothe> bottomClothes = new ArrayList<>();

    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user2);

        rowTop = findViewById(R.id.row_top);
        rowBottom = findViewById(R.id.row_bottom);
        ivTop = findViewById(R.id.ivTop);
        ivButtom = findViewById(R.id.ivbButtom);
        btnRefresh = findViewById(R.id.btnRefresh);

        // נתונים מה-Intent
        temperature = getIntent().getDoubleExtra("TEMPERATURE", 20);
        isMale = getIntent().getBooleanExtra("IS_MALE", true);

        loadClothes();

        btnRefresh.setOnClickListener(v -> setRandomCentralImages());
    }

    private void loadClothes() {
        DatabaseService.getInstance().getClotheList(new DatabaseService.DatabaseCallback<List<Clothe>>() {
            @Override
            public void onCompleted(List<Clothe> clothes) {
                if (clothes != null) {
                    filteredClothes = filterClothes(clothes, temperature, isMale);
                    separateTopBottom(filteredClothes);
                    populateRows();
                    setRandomCentralImages(); // הצגה ראשונית של תמונות רנדומליות
                }
            }

            @Override
            public void onFailed(Exception e) {
                e.printStackTrace();
                Toast.makeText(user2Activity.this, "שגיאה בטעינת פריטים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // סינון לפי עונה ומגדר
    // סינון לפי עונה ומגדר
    private List<Clothe> filterClothes(List<Clothe> clothes, double temperature, boolean isMale) {
        List<Clothe> filtered = new ArrayList<>();
        String season;

        if (temperature >= 25) season = "קיץ";
        else if (temperature >= 20) season = "אביב";
        else if (temperature >= 19) season = "סתיו";
        else season = "חורף";

        boolean favoriteFlag = isMale;

        for (Clothe c : clothes) {
            // אם הפריט מסומן לכל העונות, הוא תמיד נכנס
            if (c.isFavorite() == favoriteFlag &&
                    (c.getSeason().equalsIgnoreCase(season) || c.getSeason().equalsIgnoreCase("All"))) {
                filtered.add(c);
            }
        }
        return filtered;
    }

    // הפרדה לשורה עליונה ותחתונה
    private void separateTopBottom(List<Clothe> clothes) {
        topClothes.clear();
        bottomClothes.clear();
        for (Clothe clothe : clothes) {
            switch (clothe.getType()) {
                case "חולצה קצרה":
                case "חולצה ארוכה":
                case "גופייה":
                case "סוודר":
                case "קפוצון":
                case "זקט":
                case "מעיל":
                case "עליונית":
                case "ווסט":
                case "חולצת פולו":
                    topClothes.add(clothe);
                    break;

                case "מכנסיים ארוכים":
                case "מכנסיים קצרים":
                case "גינס":
                case "חצאית":
                case "שמלה":
                case "טייטס":
                case "טרנינג":
                case "חליפת ספורט":
                case "סרבל":
                    bottomClothes.add(clothe);
                    break;

                default:
                    topClothes.add(clothe);
                    break;
            }
        }
    }

    // הצגת שורות העליונים והתחתונים
    private void populateRows() {
        rowTop.removeAllViews();
        rowBottom.removeAllViews();

        for (Clothe clothe : topClothes) {
            ImageView imageView = createImageView(clothe);
            imageView.setOnClickListener(v -> ivTop.setImageBitmap(ImageUtil.convertFrom64base(clothe.getImageUrl())));
            rowTop.addView(imageView);
        }

        for (Clothe clothe : bottomClothes) {
            ImageView imageView = createImageView(clothe);
            imageView.setOnClickListener(v -> ivButtom.setImageBitmap(ImageUtil.convertFrom64base(clothe.getImageUrl())));
            rowBottom.addView(imageView);
        }
    }

    private ImageView createImageView(Clothe clothe) {
        ImageView imageView = new ImageView(this);
        Bitmap bitmap = ImageUtil.convertFrom64base(clothe.getImageUrl());
        imageView.setImageBitmap(bitmap);

        int size = (int) getResources().getDimension(R.dimen.item_thumbnail);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(8, 8, 8, 8);
        imageView.setLayoutParams(params);

        imageView.setOnLongClickListener(v -> {
            v.startDragAndDrop(null, new View.DragShadowBuilder(v), v, 0);
            return true;
        });

        return imageView;
    }

    // הצגת שתי תמונות אקראיות במרכז
    private void setRandomCentralImages() {
        if (!topClothes.isEmpty()) {
            Clothe top = topClothes.get(random.nextInt(topClothes.size()));
            ivTop.setImageBitmap(ImageUtil.convertFrom64base(top.getImageUrl()));
        }

        if (!bottomClothes.isEmpty()) {
            Clothe bottom = bottomClothes.get(random.nextInt(bottomClothes.size()));
            ivButtom.setImageBitmap(ImageUtil.convertFrom64base(bottom.getImageUrl()));
        }
    }

    // אופציונלי: DragListener אם רוצים לגרור פריטים
    private static class DragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            LinearLayout container = (LinearLayout) v;
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                case DragEvent.ACTION_DRAG_ENTERED:
                case DragEvent.ACTION_DRAG_LOCATION:
                case DragEvent.ACTION_DRAG_EXITED:
                    return true;
                case DragEvent.ACTION_DROP:
                    View draggedView = (View) event.getLocalState();
                    ViewGroup owner = (ViewGroup) draggedView.getParent();
                    if (owner == container) {
                        owner.removeView(draggedView);
                        container.addView(draggedView);
                    }
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
                default:
                    return false;
            }
        }
    }
}