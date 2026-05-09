package com.example.projectx;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectx.adapter.OutfitAdapter;
import com.example.projectx.model.Outfit;
import com.example.projectx.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class savedlooks extends AppCompatActivity {

    private RecyclerView recyclerSummer, recyclerWinter;
    private ImageButton btnBack;
    private TextView tvTitle;
    private boolean isMale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savedlooks);

        // קבלת המגדר מה-Intent (אם לא הועבר, ברירת המחדל היא גבר - true)
        isMale = getIntent().getBooleanExtra("IS_MALE", true);

        recyclerSummer = findViewById(R.id.recyclerSummer);
        recyclerWinter = findViewById(R.id.recyclerWinter);
        btnBack = findViewById(R.id.btnBack);

        // נסה למצוא את הכותרת ב-XML, אם קיימת
        tvTitle = findViewById(R.id.title_saved);
        if(tvTitle != null) {
            tvTitle.setText(isMale ? "לוקים שמורים - גבר" : "לוקים שמורים - אישה");
        }

        btnBack.setOnClickListener(v -> finish());

        // הגדרת תצוגת רשת ל-2 עמודות
        recyclerSummer.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerWinter.setLayoutManager(new GridLayoutManager(this, 2));

        // הגדרת רווחים בין הפריטים ברשימה
        RecyclerView.ItemDecoration spacing = new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = 24;
                outRect.left = 8;
                outRect.right = 8;
            }
        };
        recyclerSummer.addItemDecoration(spacing);
        recyclerWinter.addItemDecoration(spacing);

        // קריאה לפונקציה שתטען ותסנן את הנתונים מהפיירבייס
        loadLooks();
    }

    private void loadLooks() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseService.getInstance().getUserOutfitList(uid,
                new DatabaseService.DatabaseCallback<List<Outfit>>() {
                    @Override
                    public void onCompleted(List<Outfit> outfits) {
                        if (outfits == null) return;

                        List<Outfit> summerLooks = new ArrayList<>();
                        List<Outfit> winterLooks = new ArrayList<>();

                        for (Outfit outfit : outfits) {
                            // 🔥 הסינון הקריטי: מוסיפים רק לוקים שמתאימים למגדר שנבחר
                            if (outfit.isMale() == isMale) {
                                // חלוקה לחורף וקיץ (אם יש מעיל או אין)
                                if (outfit.getOuter() != null && !outfit.getOuter().isEmpty()) {
                                    winterLooks.add(outfit);
                                } else {
                                    summerLooks.add(outfit);
                                }
                            }
                        }

                        // הגדרת המתאמים (Adapters) עם הרשימות המסוננות
                        recyclerSummer.setAdapter(new OutfitAdapter( summerLooks));
                        recyclerWinter.setAdapter(new OutfitAdapter( winterLooks));
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(savedlooks.this, "שגיאה בטעינת לוקים", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}