package com.example.projectx;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectx.model.Clothe;
import com.example.projectx.services.DatabaseService;
import com.example.projectx.util.ImageUtil;

import java.util.List;

public class user2Activity extends AppCompatActivity {

    private LinearLayout rowTop, rowBottom;
    private ImageView ivTop, ivButtom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user2);

        rowTop = findViewById(R.id.row_top);
        rowBottom = findViewById(R.id.row_bottom);
        ivTop = findViewById(R.id.ivTop);
        ivButtom = findViewById(R.id.ivbButtom);

        loadClothes();
    }

    private void loadClothes() {
        DatabaseService.getInstance().getClotheList(new DatabaseService.DatabaseCallback<List<Clothe>>() {
            @Override
            public void onCompleted(List<Clothe> clothes) {
                if (clothes != null) {
                    populateRows(clothes);
                }
            }

            @Override
            public void onFailed(Exception e) {
                e.printStackTrace();
                Toast.makeText(user2Activity.this, "שגיאה בטעינת פריטים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateRows(List<Clothe> clothes) {
        rowTop.removeAllViews();
        rowBottom.removeAllViews();

        for (Clothe clothe : clothes) {
            ImageView imageView = new ImageView(this);
            Bitmap bitmap = ImageUtil.convertFrom64base(clothe.getImageUrl());
            imageView.setImageBitmap(bitmap);

            int size = (int) getResources().getDimension(R.dimen.item_thumbnail);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(8, 8, 8, 8);
            imageView.setLayoutParams(params);

            // DragListener לכל שורה
            DragListener dragListener = new DragListener();

            switch (clothe.getType()) {
                // חלק עליון
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
                    rowTop.addView(imageView);
                    rowTop.setOnDragListener(dragListener);

                    // 🔹 לחיצה רגילה תשנה רק את התמונה העליונה
                    imageView.setOnClickListener(v ->
                            ivTop.setImageBitmap(ImageUtil.convertFrom64base(clothe.getImageUrl()))
                    );

                    // 🔹 גרירה בתוך אותה שורה
                    imageView.setOnLongClickListener(v -> {
                        v.startDragAndDrop(null, new View.DragShadowBuilder(v), v, 0);
                        return true;
                    });
                    break;

                // חלק תחתון
                case "מכנסיים ארוכים":
                case "מכנסיים קצרים":
                case "גינס":
                case "חצאית":
                case "שמלה":
                case "טייטס":
                case "טרנינג":
                case "חליפת ספורט":
                case "סרבל":
                    rowBottom.addView(imageView);
                    rowBottom.setOnDragListener(dragListener);

                    // 🔹 לחיצה רגילה תשנה רק את התמונה התחתונה
                    imageView.setOnClickListener(v ->
                            ivButtom.setImageBitmap(ImageUtil.convertFrom64base(clothe.getImageUrl()))
                    );

                    // 🔹 גרירה בתוך אותה שורה
                    imageView.setOnLongClickListener(v -> {
                        v.startDragAndDrop(null, new View.DragShadowBuilder(v), v, 0);
                        return true;
                    });
                    break;

                // ברירת מחדל - למעלה
                default:
                    rowTop.addView(imageView);
                    rowTop.setOnDragListener(dragListener);

                    imageView.setOnClickListener(v ->
                            ivTop.setImageBitmap(ImageUtil.convertFrom64base(clothe.getImageUrl()))
                    );
                    imageView.setOnLongClickListener(v -> {
                        v.startDragAndDrop(null, new View.DragShadowBuilder(v), v, 0);
                        return true;
                    });
                    break;
            }
        }
    }

    // DragListener מאפשר גרירה בתוך אותה שורה בלבד
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
