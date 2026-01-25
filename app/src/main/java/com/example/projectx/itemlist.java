package com.example.projectx;

import android.os.Bundle;
import android.util.Log;
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


    ImageView ivTop, ivButtom;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemlist);

        // 1. אתחול ה‑RecyclerView
        recyclerView = findViewById(R.id.recycler_view_clothes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ivButtom=findViewById(R.id.ivbButtom);
        ivTop=findViewById(R.id.ivTop);

        // 2. אתחול רשימת פריטים ו‑Adapter עם Context
        clotheList = new ArrayList<>();


        adapter = new ClotheAdapter( clotheList,  new ClotheAdapter.OnClotheClickListener()  {
            @Override
            public void onClotheClick(Clothe clothe) {

               // if(clothe.getType().contains())

             ivTop.setImageBitmap(ImageUtil.convertFrom64base(clothe.getImageUrl()));

            }

            @Override
            public void onLongClotheClick(Clothe clothe) {

                ivButtom.setImageBitmap(ImageUtil.convertFrom64base(clothe.getImageUrl()));

                  // Log.d("Clothe long clicked: " +"clothe.toString()" );

            }



        });








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
