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

public class itemlist extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ClotheAdapter adapter;

    private List<Clothe> fullClotheList; // 🔥 רשימה שתשמור את כל הנתונים המקוריים
    private List<Clothe> clotheList;     // 🔥 הרשימה המסוננת שמוצגת בפועל

    private ImageView btnBack;
    private ImageView ivTop, ivButtom;

    private Spinner spinnerTypeFilter, spinnerGenderFilter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemlist);

        // RecyclerView
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

        // Adapter עם כל הפעולות כולל מחיקה
        adapter = new ClotheAdapter(clotheList, new ClotheAdapter.OnClotheClickListener() {

            @Override
            public void onClotheClick(Clothe clothe) {
                if(ivTop != null) {
                    ivTop.setImageBitmap(ImageUtil.convertFrom64base(clothe.getImageUrl()));
                }
            }

            @Override
            public void onLongClotheClick(Clothe clothe) {
                if(ivButtom != null) {
                    ivButtom.setImageBitmap(ImageUtil.convertFrom64base(clothe.getImageUrl()));
                }
            }

            @Override
            public void onDeleteClothe(Clothe clothe) {
                DatabaseService.getInstance().deleteClothe(clothe.getItemId(),
                        new DatabaseService.DatabaseCallback<Void>() {
                            @Override
                            public void onCompleted(Void object) {
                                fullClotheList.remove(clothe); // מחיקה מהרשימה המלאה
                                applyFilters(); // עדכון הרשימה המוצגת
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

        setupSpinners();
        loadClothes();
    }

    // ================= הגדרת תפריטי הסינון =================
    private void setupSpinners() {
        // הגדרת נתוני מגדר
        String[] genderOptions = {"כל המגדרים", "גבר", "אישה"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genderOptions);
        spinnerGenderFilter.setAdapter(genderAdapter);

        // הגדרת נתוני סוגי בגדים מתוך ה-XML
        List<String> typeOptions = new ArrayList<>();
        typeOptions.add("כל הסוגים");
        typeOptions.addAll(Arrays.asList(getResources().getStringArray(R.array.typeArr)));

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, typeOptions);
        spinnerTypeFilter.setAdapter(typeAdapter);

        // מאזינים לשינוי בחירה
        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };

        spinnerGenderFilter.setOnItemSelectedListener(filterListener);
        spinnerTypeFilter.setOnItemSelectedListener(filterListener);
    }

    // ================= החלת הסינון =================
    private void applyFilters() {
        if (fullClotheList == null || fullClotheList.isEmpty()) return;

        String selectedGender = spinnerGenderFilter.getSelectedItem().toString();
        String selectedType = spinnerTypeFilter.getSelectedItem().toString();

        clotheList.clear();

        for (Clothe c : fullClotheList) {

            // סינון מגדר (isFavorite = true משמעו גבר, false משמעו אישה)
            boolean matchGender = true;
            if (selectedGender.equals("גבר")) {
                matchGender = c.isFavorite();
            } else if (selectedGender.equals("אישה")) {
                matchGender = !c.isFavorite();
            }

            // סינון סוג
            boolean matchType = true;
            if (!selectedType.equals("כל הסוגים")) {
                matchType = c.getType() != null && c.getType().equals(selectedType);
            }

            // אם הפריט תואם את שני הסינונים - נוסיף אותו לרשימה המוצגת
            if (matchGender && matchType) {
                clotheList.add(c);
            }
        }

        adapter.notifyDataSetChanged();
    }

    // ================= טעינת נתונים מה־Firebase =================
    private void loadClothes() {
        DatabaseService.getInstance().getClotheList(new DatabaseService.DatabaseCallback<List<Clothe>>() {
            @Override
            public void onCompleted(List<Clothe> clothes) {
                if (clothes == null) {
                    Toast.makeText(itemlist.this, "No clothes found", Toast.LENGTH_SHORT).show();
                    return;
                }

                fullClotheList.clear();
                fullClotheList.addAll(clothes); // שומרים את המידע המקורי

                applyFilters(); // מסננים ומציגים בהתאם למה שנבחר כרגע
            }

            @Override
            public void onFailed(Exception e) {
                e.printStackTrace();
                Toast.makeText(itemlist.this, "Error loading clothes", Toast.LENGTH_SHORT).show();
            }
        });
    }
}