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

// מסך "מחולל האאוטפיטים". מקבל את העדפות המשתמש, מסנן את המלתחה ומציג שילובים חכמים.
public class user2Activity extends AppCompatActivity {

    private ImageView ivTop, ivOuter, ivBottom;
    private Button btnRefresh, btnSaveLook, btnSaved,btnhome;

    private LinearLayout rowTop, rowBottom;

    private final Random random = new Random();

    private double temperature;
    private boolean isMale;

    private List<String> topColors, bottomColors;
    private ImageButton btnBack;

    // רשימות עזר לניהול חכם של הבגדים אחרי הסינון
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
        btnBack = findViewById(R.id.btnBack);
        btnhome=findViewById(R.id.btnhome);

        // משיכת כל הנתונים שהעברנו מהמסך הקודם (טמפרטורה, מגדר, ורשימות הצבעים לעליונים ותחתונים)
        temperature = getIntent().getDoubleExtra("TEMPERATURE", 20);
        isMale = getIntent().getBooleanExtra("IS_MALE", true);
        topColors = getIntent().getStringArrayListExtra("TOP_COLORS");
        bottomColors = getIntent().getStringArrayListExtra("BOTTOM_COLORS");

        // מתחילים בטעינת הבגדים של המשתמש מפיירבייס
        loadClothes();

        btnBack.setOnClickListener(v -> finish());

        // כפתור רענון - מגריל לוק חדש מתוך הרשימה המסוננת
        btnRefresh.setOnClickListener(v -> setRandomLook());

        // כפתור שמירה - לוקח את מה שרואים עכשיו ושומר את זה כאאוטפיט חדש
        btnSaveLook.setOnClickListener(v -> saveLook());

        // מעבר למסך "לוקים שמורים", תוך העברת המגדר כדי שהמסך הבא ידע מה להציג
        btnSaved.setOnClickListener(v -> {
            Intent intent = new Intent(this, savedlooks.class);
            intent.putExtra("IS_MALE", isMale);
            startActivity(intent);
        });
        btnhome.setOnClickListener(v -> {
            Intent intent = new Intent(this, userpage.class);
            startActivity(intent);
        });
    }

    // ================= LOAD =================
    // מושך את כל הבגדים של המשתמש הנוכחי מהשרת
    private void loadClothes() {

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseService.getInstance().getUserClothes(userId,
                new DatabaseService.DatabaseCallback<List<Clothe>>() {

                    @Override
                    public void onCompleted(List<Clothe> clothes) {

                        if (clothes == null) return;

                        // שרשרת הטיפול בבגדים:
                        // 1. מסננים מה לא רלוונטי
                        filteredClothes = filterClothes(clothes);
                        // 2. מפצלים לעליונים, תחתונים ומעילים
                        separateClothes(filteredClothes);
                        // 3. מציגים את הקוביות הקטנות למטה (שורות הגלילה)
                        populateRows();
                        // 4. מגרילים לוק ראשון ישר כשנכנסים למסך
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
    // הפונקציה המרכזית שמסננת את המלתחה לפי עונה, מגדר וצבעים
    private List<Clothe> filterClothes(List<Clothe> clothes) {

        List<Clothe> filtered = new ArrayList<>();

        // המרת הטמפרטורה המספרית לעונת השנה הרלוונטית
        String season;
        if (temperature >= 25) season = "קיץ";
        else if (temperature >= 20) season = "אביב";
        else if (temperature >= 15) season = "סתיו";
        else season = "חורף";

        for (Clothe c : clothes) {

            if (c == null) continue;

            // 🔥 סינון מגדר: מוודא שהבגד תואם למגדר שהמשתמש בחר (isFavorite = true אומר שזה גבר)
            boolean genderMatch;
            if (isMale) {
                genderMatch = c.isFavorite();
            } else {
                genderMatch = !c.isFavorite();
            }

            // סינון עונה: בודק אם הבגד הוגדר ל"כל העונות" (All) או לעונה הנוכחית שחישבנו קודם
            boolean seasonMatch =
                    c.getSeason() == null ||
                            c.getSeason().equalsIgnoreCase("All") ||
                            c.getSeason().contains(season);

            // הפעלת בדיקת הצבע מול מה שהמשתמש סימן
            boolean colorMatch = isColorMatch(c);

            // רק אם הפריט עבר את כל 3 הסינונים (עונה + צבע + מגדר), נכניס אותו לרשימה הסופית
            if (seasonMatch && colorMatch && genderMatch) {
                filtered.add(c);
            }
        }

        return filtered;
    }

    // ================= COLOR =================
    // בודק אם הצבע של הפריט הספציפי תואם לבחירות של המשתמש ממסך הצבעים
    private boolean isColorMatch(Clothe c) {

        String color = c.getColor();
        if (color == null) return true;

        // מחליטים איזו רשימת צבעים לבדוק (עליונים או תחתונים) לפי סוג הפריט
        List<String> list =
                (isTop(c.getType()) || isOuter(c.getType()))
                        ? topColors
                        : bottomColors;

        if (list == null || list.isEmpty()) return true; // אם לא נבחר כלום, מאשרים הכל

        // בודקים אם הצבע של הפריט נמצא בתוך הרשימה שנבחרה
        for (String s : list) {
            if (s != null && s.equalsIgnoreCase(color.trim())) {
                return true;
            }
        }
        return false; // אם הגענו לפה, הצבע לא ברשימה
    }

    // ================= POPULATE =================
    // מייצרת את סרטי הגלילה של הפריטים הקטנים (Thumbnails) בתחתית המסך
    private void populateRows() {

        // מנקים את השורות מתוצאות קודמות
        rowTop.removeAllViews();
        rowBottom.removeAllViews();

        for (Clothe c : filteredClothes) {

            // יצירת תמונה (ImageView) חדשה לכל פריט באופן דינמי
            ImageView img = new ImageView(this);
            int size = (int) getResources().getDimension(R.dimen.item_thumbnail);
            if (size == 0) size = 140;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(10, 10, 10, 10);
            img.setLayoutParams(params);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // המרת התמונה ושיוכה למשבצת הקטנה
            Bitmap bmp = ImageUtil.convertFrom64base(c.getImageUrl());
            img.setImageBitmap(bmp);

            // הגדרת לחיצה על תמונה קטנה: אם נלחץ עליה, היא תוצג בגדול בתצוגה המרכזית המתאימה לה
            img.setOnClickListener(v -> {
                if (isTop(c.getType())) ivTop.setImageBitmap(bmp);
                else if (isOuter(c.getType())) ivOuter.setImageBitmap(bmp);
                else ivBottom.setImageBitmap(bmp);
            });

            // מיון לתצוגה בשורות - עליונים למעלה, תחתונים למטה
            if (isTop(c.getType()) || isOuter(c.getType())) {
                rowTop.addView(img);
            } else {
                rowBottom.addView(img);
            }
        }
    }

    // ================= SEPARATE =================
    // מחלקת את הרשימה המאוחדת המסוננת ל-3 רשימות שונות כדי שיהיה קל להגריל מהן
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
    // פונקציית הקסם שמרכיבה לוק הגיוני
    private void setRandomLook() {
        Clothe selectedTop = null;

        // 1. בחירת חולצה באקראי
        if (!topClothes.isEmpty()) {
            selectedTop = topClothes.get(random.nextInt(topClothes.size()));
            ivTop.setImageBitmap(ImageUtil.convertFrom64base(selectedTop.getImageUrl()));
        }

        // 2. בחירת שכבה עליונה: נבחר מעיל או ז'קט רק אם הטמפרטורה מתחת ל-20 מעלות
        if (!outerClothes.isEmpty() && temperature < 20) {
            Clothe outer = outerClothes.get(random.nextInt(outerClothes.size()));
            ivOuter.setVisibility(View.VISIBLE);
            ivOuter.setImageBitmap(ImageUtil.convertFrom64base(outer.getImageUrl()));
        } else {
            // אם חם מספיק, מסתירים את השכבה העליונה לגמרי
            ivOuter.setVisibility(View.GONE);
        }

        // 3. בחירת מכנס (עם התאמה חכמה לספורט)
        if (!bottomClothes.isEmpty()) {
            Clothe selectedBottom = null;

            // חוק ספורט: אם נבחר למעלה "טופ ספורט", אנחנו ננסה למצוא למטה "מכנס ספורט"
            if (selectedTop != null && selectedTop.getType() != null && selectedTop.getType().equals("טופ ספורט")) {

                // מאתרים את כל מכנסי הספורט מהרשימה התחתונה
                List<Clothe> sportBottoms = new ArrayList<>();
                for (Clothe b : bottomClothes) {
                    if ("מכנס ספורט".equals(b.getType())) {
                        sportBottoms.add(b);
                    }
                }

                // אם מצאנו מכנסי ספורט - נגריל אחד מהם
                if (!sportBottoms.isEmpty()) {
                    selectedBottom = sportBottoms.get(random.nextInt(sportBottoms.size()));
                } else {
                    // גיבוי: אם אין לו מכנסי ספורט בכלל, נגריל מכנס רגיל למרות הטופ ספורט
                    selectedBottom = bottomClothes.get(random.nextInt(bottomClothes.size()));
                }

            } else {
                // ללוק רגיל - פשוט מגרילים מכנס באקראי מתוך כל המכנסיים הזמינים
                selectedBottom = bottomClothes.get(random.nextInt(bottomClothes.size()));
            }

            // מציגים את המכנס הנבחר
            if (selectedBottom != null) {
                ivBottom.setImageBitmap(ImageUtil.convertFrom64base(selectedBottom.getImageUrl()));
            }
        }
    }

    // ================= SAVE =================
    // לוקחת את התמונות המוצגות כרגע על המסך, מתרגמת אותן חזרה לטקסט ושומרת כאאוטפיט מחובר
    private void saveLook() {

        String outfitId = DatabaseService.getInstance().generateOutfitId();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // המרת התצוגות העכשוויות לפורמט Base64 שפיירבייס יודע לשמור
        String top = ImageUtil.convertTo64Base(ivTop);

        // שומרת מעיל רק אם הוא גלוי לעין (החלטנו קודם אם הוא גלוי לפי הטמפרטורה)
        String outer = ivOuter.getVisibility() == View.VISIBLE
                ? ImageUtil.convertTo64Base(ivOuter)
                : null;

        String bottom = ImageUtil.convertTo64Base(ivBottom);

        // יצירת אובייקט אאוטפיט חדש (כולל שמירת המגדר כדי שהמסך של "לוקים שמורים" ידע למיין אותו)
        Outfit outfit = new Outfit(outfitId, userId, top, outer, bottom, isMale);

        DatabaseService.getInstance().createNewOutfit(outfit,
                new DatabaseService.DatabaseCallback<Void>() {

                    @Override
                    public void onCompleted(Void object) {
                        Toast.makeText(user2Activity.this, "Saved!", Toast.LENGTH_SHORT).show();

                        // אחרי השמירה עוברים ישירות לראות את האאוטפיט במסך הלוקים השמורים
                        Intent intent = new Intent(user2Activity.this, savedlooks.class);
                        intent.putExtra("IS_MALE", isMale);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(user2Activity.this, "Error saving", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ================= TYPES =================
    // פונקציות עזר קטנות שעוזרות להחליט איזו מילה שייכת לאיזו קטגוריה (עליון או מעיל)
    private boolean isTop(String type) {
        return type != null && (
                type.equals("חולצה קצרה") ||
                        type.equals("חולצה ארוכה") ||
                        type.equals("גופייה") ||
                        type.equals("טופ ספורט")
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