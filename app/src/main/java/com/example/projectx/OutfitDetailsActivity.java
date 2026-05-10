package com.example.projectx;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectx.model.Outfit;
import com.example.projectx.services.DatabaseService;
import com.example.projectx.util.ImageUtil;

// מסך המציג את הפריטים הספציפיים שמרכיבים אאוטפיט שלם (חולצה, מכנס, ופריט עליון אם יש)
public class OutfitDetailsActivity extends AppCompatActivity {

    private ImageView ivTop, ivOuter, ivBottom;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_details);

        // קישור רכיבי התצוגה לקוד
        ivTop = findViewById(R.id.ivSelectedTop);
        ivOuter = findViewById(R.id.ivSelectedOuter);
        ivBottom = findViewById(R.id.ivSelectedBottom);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // שולפים את ה-ID של האאוטפיט שהמסך הקודם העביר לנו כדי שנדע מה להציג
        String outfitId = getIntent().getStringExtra("outfitId");

        // בדיקת הגנה: אם מסיבה כלשהי לא קיבלנו ID, מציגים שגיאה וסוגרים את המסך למניעת קריסה
        if (outfitId == null) {
            Toast.makeText(this, "Missing outfit", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // פנייה לפיירבייס: "תביא לי בבקשה את האאוטפיט המלא שזה ה-ID שלו"
        DatabaseService.getInstance().getOutfit(outfitId,
                new DatabaseService.DatabaseCallback<Outfit>() {

                    @Override
                    public void onCompleted(Outfit o) {

                        if (o == null) return;

                        // אם יש חולצה (Top), ממירים אותה ממחרוזת טקסט ארוכה (Base64) חזרה לתמונה ומציגים
                        if (o.getTop() != null)
                            ivTop.setImageBitmap(ImageUtil.convertFrom64base(o.getTop()));

                        // לוגיקה חכמה לפריט עליון (ז'קט/מעיל):
                        // בגלל שלא בכל אאוטפיט יש מעיל, אנחנו בודקים אם הוא קיים.
                        if (o.getOuter() != null && !o.getOuter().isEmpty()) {
                            // אם קיים - מוודאים שתיבת התמונה גלויה (VISIBLE) ומציגים את התמונה
                            ivOuter.setVisibility(View.VISIBLE);
                            ivOuter.setImageBitmap(ImageUtil.convertFrom64base(o.getOuter()));
                        } else {
                            // אם אין מעיל - מעלימים לחלוטין את התיבה (GONE) כדי שלא תתפוס מקום ריק על המסך
                            ivOuter.setVisibility(View.GONE);
                        }

                        // כנ"ל למכנס (Bottom)
                        if (o.getBottom() != null)
                            ivBottom.setImageBitmap(ImageUtil.convertFrom64base(o.getBottom()));
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(OutfitDetailsActivity.this,
                                "Error loading outfit",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}