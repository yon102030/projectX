package com.example.projectx;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectx.model.Clothe;
import com.example.projectx.services.DatabaseService;
import com.example.projectx.util.ImageUtil;
import com.google.firebase.auth.FirebaseAuth;

// מחלקה זו מנהלת את מסך "הוספת פריט". היא יורשת מ-AppCompatActivity שזה הבסיס לכל מסך באנדרואיד.
public class AddClothe extends AppCompatActivity {

    // הצהרה על המשתנים שייצגו את רכיבי המסך (UI) ואת שירותי מסד הנתונים
    private Spinner spinnerType, spinnerColor, spinnerSeason;
    private RadioGroup radioGenderGroup;
    private ImageView itemImage;
    private Button btnGallery, btnCamera, btnAdd;
    private ImageButton btnBack;
    private DatabaseService databaseService; // מחלקה אישית שעוזרת לנו לתקשר עם פיירבייס
    private ActivityResultLauncher<Intent> cameraLauncher; // רכיב שאחראי על הפעלת המצלמה וקבלת התמונה חזרה

    int SELECT_PICTURE = 200; // קוד זיהוי לבקשת פתיחת הגלריה

    // הפונקציה הראשונה שרצה ברגע שהמסך נוצר ונפתח
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // חיבור קובץ העיצוב (XML) למחלקת ה-Java הזו
        setContentView(R.layout.activity_addclothe);

        // קריאה לפונקציה שמקשרת בין המשתנים בקוד לבין ה-ID שלהם ב-XML
        initViews();

        // בקשת הרשאות מהמשתמש (למשל, גישה למצלמה ולגלריה) בעזרת מחלקת עזר
        ImageUtil.requestPermission(this);

        // קבלת מופע (Instance) של מסד הנתונים כדי שנוכל לשמור נתונים בהמשך
        databaseService = DatabaseService.getInstance();

        // הגדרת ה"מאזין" למצלמה - מה קורה אחרי שהמשתמש צילם תמונה
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // אם הצילום עבר בהצלחה ויש מידע (תמונה)
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // חילוץ התמונה (Bitmap) מתוך התוצאה
                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                        // הצגת התמונה שצולמה בתוך ה-ImageView שבמסך
                        itemImage.setImageBitmap(bitmap);
                    }
                });

        // הגדרת לחיצות (Click Listeners) על הכפתורים השונים:

        // בלחיצה על "גלריה" -> הפעלת הפונקציה שפותחת את הגלריה
        btnGallery.setOnClickListener(v -> imageChooser());

        // בלחיצה על "מצלמה" -> יצירת כוונה (Intent) לפתיחת המצלמה והפעלתה דרך ה-Launcher
        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(intent);
        });

        // בלחיצה על "הוסף פריט" -> הפעלת פונקציית השמירה
        btnAdd.setOnClickListener(v -> addClothe());

        btnBack = findViewById(R.id.btnBack);

        // בלחיצה על כפתור "חזור" -> סגירת העמוד הנוכחי (finish) וחזרה לעמוד הקודם בהיסטוריה
        btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    // פונקציה שעושה סדר: מחברת את כל משתני ה-Java לרכיבי ה-XML לפי ה-ID שלהם
    private void initViews() {
        spinnerType = findViewById(R.id.spinner_type);
        spinnerColor = findViewById(R.id.spinner_color);
        spinnerSeason = findViewById(R.id.spinner_season);

        radioGenderGroup = findViewById(R.id.radio_gender_group);

        itemImage = findViewById(R.id.item_image);

        btnGallery = findViewById(R.id.button_choose_gallery);
        btnCamera = findViewById(R.id.button_take_photo);
        btnAdd = findViewById(R.id.button_add);
    }

    // הפונקציה המרכזית שאוספת את הנתונים, בודקת תקינות, ושומרת את הפריט בפיירבייס
    private void addClothe() {
        // משיכת הערכים שהמשתמש בחר מהתפריטים הנגללים (Spinners)
        String type = spinnerType.getSelectedItem().toString();
        String color = spinnerColor.getSelectedItem().toString();
        String season = spinnerSeason.getSelectedItem().toString();

        // 🔥 התיקון שלנו: הופך את הטקסט "כל העונות" בעברית ל-"All" באנגלית כדי שהסינון יעבוד נכון מול מסד הנתונים
        if (season.equals("כל העונות")) {
            season = "All";
        }

        // בדיקה האם נבחר כפתור רדיו (גבר או אישה)
        int selectedId = radioGenderGroup.getCheckedRadioButtonId();
        if (selectedId == -1) { // 1- אומר ששום דבר לא נבחר
            Toast.makeText(this, "נא לבחור גבר או אישה", Toast.LENGTH_SHORT).show();
            return; // עצירת הפונקציה כדי לא לשמור פריט חסר
        }

        // קביעת משתנה בוליאני (אמת/שקר) לפי מה שנבחר. (הערה: נראה שהשדה משמש להעדפה/מועדף או למגדר)
        boolean isFavorite = selectedId == R.id.radio_male;

        // וידוא שהמשתמש בחר או צילם תמונה
        if (itemImage.getDrawable() == null) {
            Toast.makeText(this, "נא לבחור תמונה", Toast.LENGTH_SHORT).show();
            return;
        }

        // המרת התמונה ממצב של תצוגה (ImageView) למחרוזת טקסט ארוכה (Base64) כדי שאפשר יהיה לשמור אותה בפיירבייס
        String imageUrl = ImageUtil.convertTo64Base(itemImage);

        // משיכת ה-ID הייחודי של המשתמש שמחובר כרגע לאפליקציה (דרך מערכת ההזדהות של פיירבייס)
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // ייצור ID אקראי וחדש עבור הפריט הספציפי הזה
        String clotheId = databaseService.generateClotheId();

        // יצירת אובייקט Clothe חדש המאגד בתוכו את כל הנתונים שאספנו
        Clothe clothe = new Clothe(
                clotheId,
                userId,
                type,
                color,
                imageUrl,
                season,
                isFavorite,
                userId
        );

        // שליחת האובייקט לפונקציה השומרת במסד הנתונים, והאזנה לתוצאה (הצלחה או כישלון)
        databaseService.createNewClothe(clothe, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                // אם השמירה הצליחה: הודעה למשתמש וסגירת חלון ההוספה
                Toast.makeText(AddClothe.this, "הפריט נוסף בהצלחה", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                // אם הייתה שגיאה בתקשורת או בשמירה: הודעה למשתמש
                Toast.makeText(AddClothe.this, "שגיאה בהוספת הפריט", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // פונקציה לפתיחת גלריית התמונות של המכשיר
    private void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*"); // הגדרה שאנחנו מחפשים רק קבצי תמונה
        i.setAction(Intent.ACTION_GET_CONTENT); // בקשה לקבלת תוכן
        // פתיחת חלון הבחירה והמתנה לתוצאה עם הקוד המזהה שלנו (SELECT_PICTURE)
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    // פונקציה מובנית שמופעלת אוטומטית כאשר המשתמש חוזר ממסך אחר (במקרה שלנו - מהגלריה)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // אם החזרה הייתה תקינה (RESULT_OK)
        if (resultCode == RESULT_OK) {
            // אם הבקשה שחזרה תואמת לבקשת בחירת התמונה שלנו
            if (requestCode == SELECT_PICTURE) {
                // חילוץ הנתיב (URI) של התמונה שנבחרה מתוך הנתונים שחזרו
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    // הצגת התמונה שנבחרה על המסך
                    itemImage.setImageURI(selectedImageUri);
                }
            }
        }
    }
}