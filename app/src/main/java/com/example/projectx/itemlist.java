package com.example.projectx;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectx.adapter.ClotheAdapter;
import com.example.projectx.model.Clothe;
import com.example.projectx.services.DatabaseService;

import java.util.ArrayList;
import java.util.List;

public class itemlist extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ClotheAdapter adapter;
    private List<Clothe> clotheList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemlist);

        // 1. אתחול ה‑RecyclerView
        recyclerView = findViewById(R.id.recycler_view_clothes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 2. אתחול רשימת פריטים ו‑Adapter עם Context
        clotheList = new ArrayList<>();
        adapter = new ClotheAdapter(this, clotheList); // ← חייב להעביר Context
        recyclerView.setAdapter(adapter);

        // 3. טעינת פריטים מה‑Firebase
        loadClothes();
    }

    private void loadClothes() {
        DatabaseService.getInstance().getClotheList(new DatabaseService.DatabaseCallback<List<Clothe>>() {
            @Override
            public void onCompleted(List<Clothe> clothes) {
                if (clothes != null) {
                    clotheList.clear();
                    clotheList.addAll(clothes);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailed(Exception e) {
                e.printStackTrace();
                Toast.makeText(itemlist.this, "שגיאה בטעינת פריטים", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
