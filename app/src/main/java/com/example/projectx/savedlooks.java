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

// מסך "לוקים שמורים": מציג למשתמש את כל האאוטפיטים שהוא יצר ושמר,
// מחולקים אוטומטית לעונות (קיץ וחורף) ומסוננים לפי מגדר.
public class savedlooks extends AppCompatActivity {

    private RecyclerView recyclerSummer, recyclerWinter;
    private ImageButton btnBack;
    private TextView tvTitle;
    private boolean isMale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savedlooks);

        // משיכת נתון המגדר שהועבר מהמסך הקודם (כדי לדעת אם להציג בגדי גברים או נשים).
        // אם לא הועבר כלום מסיבה כלשהי, ברירת המחדל תהיה גבר (true).
        isMale = getIntent().getBooleanExtra("IS_MALE", true);

        recyclerSummer = findViewById(R.id.recyclerSummer);
        recyclerWinter = findViewById(R.id.recyclerWinter);
        btnBack = findViewById(R.id.btnBack);

        // שינוי הכותרת למעלה בהתאם למגדר שנבחר
        tvTitle = findViewById(R.id.title_saved);
        if(tvTitle != null) {
            tvTitle.setText(isMale ? "לוקים שמורים - גבר" : "לוקים שמורים - אישה");
        }

        btnBack.setOnClickListener(v -> finish());

        // הגדרת צורת התצוגה של הרשימות:
        // במקום רשימה רגילה (אחד מתחת לשני), אנחנו מגדירים "רשת" (Grid) עם 2 עמודות.
        recyclerSummer.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerWinter.setLayoutManager(new GridLayoutManager(this, 2));

        // יצירת "חוקי ריווח" אישיים: מגדירים כמה רווח יהיה בין כל תמונה לתמונה בגריד,
        // כדי שהאאוטפיטים לא יהיו דבוקים אחד לשני וייראו אסתטיים.
        RecyclerView.ItemDecoration spacing = new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = 24; // רווח מלמטה
                outRect.left = 8;    // רווח משמאל
                outRect.right = 8;   // רווח מימין
            }
        };

        // החלת חוקי הריווח שיצרנו על שתי הרשימות
        recyclerSummer.addItemDecoration(spacing);
        recyclerWinter.addItemDecoration(spacing);

        // קריאה לפונקציה שתביא את הנתונים מהשרת ותמיין אותם
        loadLooks();
    }

    // הפונקציה שאחראית להביא את הנתונים ולחלק אותם לקיץ וחורף
    private void loadLooks() {
        // שליפת ה-ID של המשתמש המחובר כרגע כדי להביא רק את הלוקים שלו
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // פנייה לפיירבייס לקבלת כל רשימת האאוטפיטים של המשתמש
        DatabaseService.getInstance().getUserOutfitList(uid,
                new DatabaseService.DatabaseCallback<List<Outfit>>() {
                    @Override
                    public void onCompleted(List<Outfit> outfits) {
                        if (outfits == null) return;

                        // יצירת שתי רשימות ריקות שיקלטו את הנתונים אחרי המיון
                        List<Outfit> summerLooks = new ArrayList<>();
                        List<Outfit> winterLooks = new ArrayList<>();

                        // לוגיקת המיון: עוברים אאוטפיט-אאוטפיט ובודקים לאן הוא שייך
                        for (Outfit outfit : outfits) {

                            // סינון ראשון: בודקים אם המגדר של האאוטפיט תואם למה שהמשתמש ביקש לראות
                            if (outfit.isMale() == isMale) {

                                // סינון שני (עונות): אם יש לאאוטפיט פריט עליון (מעיל/ז'קט) הוא הולך לרשימת החורף.
                                // אם אין לו פריט עליון - הוא הולך לרשימת הקיץ.
                                if (outfit.getOuter() != null && !outfit.getOuter().isEmpty()) {
                                    winterLooks.add(outfit);
                                } else {
                                    summerLooks.add(outfit);
                                }
                            }
                        }

                        // אחרי שהרשימות מוינו, מחברים אותן למסך דרך המתאמים (Adapters) כדי שיוצגו בפועל
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