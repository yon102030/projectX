package com.example.projectx;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectx.adapter.ClotheAdapter;
import com.example.projectx.model.Clothe;
import com.example.projectx.services.DatabaseService;
import com.example.projectx.util.ImageUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// מחלקה זו מציגה למנהל (או למשתמש) את רשימת כל הפריטים (בגדים) במערכת,
// ומאפשרת לסנן אותם לפי סוג ומגדר, וכן למחוק פריטים.
public class itemlist extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ClotheAdapter adapter;

    // 🔥 טריק הסינון: שימוש בשתי רשימות!
    // fullClotheList - שומרת תמיד את כל הנתונים שחזרו מפיירבייס (כדי שלא נצטרך למשוך אותם שוב מחדש).
    private List<Clothe> fullClotheList;
    // clotheList - הרשימה הזמנית שמוצגת על המסך. היא מתעדכנת בכל פעם שהמשתמש משנה את הסינון.
    private List<Clothe> clotheList;

    private ImageView btnBack;
    private ImageView ivTop, ivButtom; // תמונות תצוגה מקדימה לפריט שנבחר מהרשימה

    private Spinner spinnerTypeFilter, spinnerGenderFilter; // תפריטי הגלילה לסינון הנתונים

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemlist);

        // קישור הרכיבים והגדרת הרשימה כרשימה אנכית
        recyclerView = findViewById(R.id.recycler_view_clothes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ivButtom = findViewById(R.id.ivbButtom);
        ivTop = findViewById(R.id.ivTop);
        btnBack = findViewById(R.id.btnBack);

        spinnerTypeFilter = findViewById(R.id.spinnerTypeFilter);
        spinnerGenderFilter = findViewById(R.id.spinnerGenderFilter);

        btnBack.setOnClickListener(v -> finish());

        fullClotheList = new ArrayList<>();
        clotheList = new ArrayList<>();

        // יצירת המתאם (Adapter) שמקבל את רשימת הבגדים (clotheList)
        // והגדרת ה"מאזינים" לפעולות המשתמש על הפריטים ברשימה
        adapter = new ClotheAdapter(clotheList, new ClotheAdapter.OnClotheClickListener() {

            // לחיצה רגילה מציגה את הפריט בתיבת ה"חלק עליון"
            @Override
            public void onClotheClick(Clothe clothe) {
                if(ivTop != null) {
                    ivTop.setImageBitmap(ImageUtil.convertFrom64base(clothe.getImageUrl()));
                }
            }

            // לחיצה ארוכה מציגה את הפריט בתיבת ה"חלק תחתון"
            @Override
            public void onLongClotheClick(Clothe clothe) {
                if(ivButtom != null) {
                    ivButtom.setImageBitmap(ImageUtil.convertFrom64base(clothe.getImageUrl()));
                }
            }

            // לחיצה על כפתור המחיקה בשורה של הפריט
            @Override
            public void onDeleteClothe(Clothe clothe) {
                // פנייה לפיירבייס כדי למחוק את הפריט ממסד הנתונים
                DatabaseService.getInstance().deleteClothe(clothe.getItemId(),
                        new DatabaseService.DatabaseCallback<Void>() {
                            @Override
                            public void onCompleted(Void object) {
                                // אם המחיקה בשרת הצליחה, אנחנו מוחקים את הפריט גם מהרשימה המלאה שלנו
                                fullClotheList.remove(clothe);

                                // מפעילים מחדש את הסינון (שמעדכן גם את clotheList המוצגת)
                                applyFilters();
                                Toast.makeText(itemlist.this, "Clothe deleted", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailed(Exception e) {
                                Toast.makeText(itemlist.this, "Delete failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        recyclerView.setAdapter(adapter);

        // הפעלת פונקציות העזר שממלאות את התפריטים בנתונים ומושכות את הבגדים
        setupSpinners();
        loadClothes();
    }

    // ================= הגדרת תפריטי הסינון =================
    private void setupSpinners() {

        // 1. הגדרת נתוני הסינון של המגדר בתוך הספינר
        String[] genderOptions = {"כל המגדרים", "גבר", "אישה"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genderOptions);
        spinnerGenderFilter.setAdapter(genderAdapter);

        // 2. הגדרת נתוני הסינון של סוג הבגד
        List<String> typeOptions = new ArrayList<>();
        typeOptions.add("כל הסוגים");
        // משיכת כל סוגי הבגדים (חולצה, מכנס וכו') מתוך קובץ ה-String של האפליקציה (strings.xml)
        typeOptions.addAll(Arrays.asList(getResources().getStringArray(R.array.typeArr)));

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, typeOptions);
        spinnerTypeFilter.setAdapter(typeAdapter);

        // יצירת "מאזין" אחד משותף שיופעל בכל פעם שהמשתמש משנה את אחד מהסינונים
        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters(); // בכל בחירה חדשה - מפעילים את פונקציית הסינון מחדש
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };

        // חיבור המאזין לשני הספינרים
        spinnerGenderFilter.setOnItemSelectedListener(filterListener);
        spinnerTypeFilter.setOnItemSelectedListener(filterListener);
    }

    // ================= הלוגיקה של הסינון =================
    // הפונקציה לוקחת את הרשימה המלאה, בודקת אילו פריטים עומדים בתנאים, ושמה אותם ברשימה המוצגת
    private void applyFilters() {
        if (fullClotheList == null || fullClotheList.isEmpty()) return;

        // משיכת הבחירות הנוכחיות של המשתמש משני התפריטים
        String selectedGender = spinnerGenderFilter.getSelectedItem().toString();
        String selectedType = spinnerTypeFilter.getSelectedItem().toString();

        // מרוקנים את הרשימה המוצגת (clotheList) לפני שמתחילים למלא אותה מחדש בתוצאות הסינון
        clotheList.clear();

        // עוברים פריט-פריט על כל הרשימה המקורית המלאה
        for (Clothe c : fullClotheList) {

            // סינון 1: מגדר. (שימוש בשדה isFavorite כי ככה זה הוגדר במסך ההוספה: true=גבר, false=אישה)
            boolean matchGender = true; // כברירת מחדל מניחים שזה תואם (רלוונטי ל"כל המגדרים")
            if (selectedGender.equals("גבר")) {
                matchGender = c.isFavorite();
            } else if (selectedGender.equals("אישה")) {
                matchGender = !c.isFavorite();
            }

            // סינון 2: סוג הפריט
            boolean matchType = true;
            if (!selectedType.equals("כל הסוגים")) {
                // אם הסוג לא מוגדר כ"כל הסוגים", בודקים אם סוג הפריט שווה בדיוק לסוג שנבחר
                matchType = c.getType() != null && c.getType().equals(selectedType);
            }

            // התנאי הסופי: הפריט ייכנס לרשימה המוצגת אך ורק אם הוא תואם גם למגדר וגם לסוג
            if (matchGender && matchType) {
                clotheList.add(c);
            }
        }

        // מודיעים למתאם שהרשימה (clotheList) עודכנה, כדי שירענן את התצוגה הגרפית במסך
        adapter.notifyDataSetChanged();
    }

    // ================= טעינת נתונים ראשונית =================
    private void loadClothes() {
        // פנייה לפיירבייס בבקשה למשוך את כל הפריטים שיש במערכת
        DatabaseService.getInstance().getClotheList(new DatabaseService.DatabaseCallback<List<Clothe>>() {
            @Override
            public void onCompleted(List<Clothe> clothes) {
                if (clothes == null) {
                    Toast.makeText(itemlist.this, "No clothes found", Toast.LENGTH_SHORT).show();
                    return;
                }

                // שמירת כל הנתונים שקיבלנו מהרשת לתוך הרשימה המלאה והקבועה (fullClotheList)
                fullClotheList.clear();
                fullClotheList.addAll(clothes);

                // הפעלת פונקציית הסינון (כדי שאם במקרה תפריטי הסינון כבר מכוונים על משהו, התצוגה תתיישר לפיהם)
                applyFilters();
            }

            @Override
            public void onFailed(Exception e) {
                e.printStackTrace();
                Toast.makeText(itemlist.this, "Error loading clothes", Toast.LENGTH_SHORT).show();
            }
        });
    }
}