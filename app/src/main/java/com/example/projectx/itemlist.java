package com.example.projectx;

import android.os.Bundle;
import android.widget.ImageView;
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
import java.util.List;

public class itemlist extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ClotheAdapter adapter;
    private List<Clothe> clotheList;

    private ImageView ivTop, ivButtom;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemlist);

        // RecyclerView
        recyclerView = findViewById(R.id.recycler_view_clothes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ivButtom = findViewById(R.id.ivbButtom);
        ivTop = findViewById(R.id.ivTop);

        clotheList = new ArrayList<>();

        // Adapter עם כל הפעולות כולל מחיקה
        adapter = new ClotheAdapter(clotheList, new ClotheAdapter.OnClotheClickListener() {

            @Override
            public void onClotheClick(Clothe clothe) {
                ivTop.setImageBitmap(
                        ImageUtil.convertFrom64base(clothe.getImageUrl())
                );
            }

            @Override
            public void onLongClotheClick(Clothe clothe) {
                ivButtom.setImageBitmap(
                        ImageUtil.convertFrom64base(clothe.getImageUrl())
                );
            }

            @Override
            public void onDeleteClothe(Clothe clothe) {

                DatabaseService.getInstance()
                        .deleteClothe(clothe.getItemId(),
                                new DatabaseService.DatabaseCallback<Void>() {

                                    @Override
                                    public void onCompleted(Void object) {

                                        clotheList.remove(clothe);
                                        adapter.notifyDataSetChanged();

                                        Toast.makeText(itemlist.this,
                                                "Clothe deleted",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailed(Exception e) {

                                        Toast.makeText(itemlist.this,
                                                "Delete failed",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
            }
        });

        recyclerView.setAdapter(adapter);

        loadClothes();
    }

    // טעינת נתונים מה־Firebase
    private void loadClothes() {

        DatabaseService.getInstance()
                .getClotheList(new DatabaseService.DatabaseCallback<List<Clothe>>() {

                    @Override
                    public void onCompleted(List<Clothe> clothes) {

                        if (clothes == null) {
                            Toast.makeText(itemlist.this,
                                    "No clothes found",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        clotheList.clear();
                        clotheList.addAll(clothes);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        e.printStackTrace();
                        Toast.makeText(itemlist.this,
                                "Error loading clothes",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}