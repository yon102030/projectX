package com.example.projectx;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectx.adapter.OutfitAdapter;
import com.example.projectx.model.Outfit;
import com.example.projectx.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class savedlooks extends AppCompatActivity {

    private RecyclerView recyclerSummer;
    private RecyclerView recyclerWinter;
    private OutfitAdapter adapter;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savedlooks);

        RecyclerView recyclerSummer = findViewById(R.id.recyclerSummer);
        RecyclerView recyclerWinter = findViewById(R.id.recyclerWinter);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            finish();
        });

// מעבר למסך userpage (בית בתוך האפליקציה)

        // Layouts
        recyclerSummer.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerWinter.setLayoutManager(new GridLayoutManager(this, 2));

        // רווח בין כרטיסים (לשניהם)
        RecyclerView.ItemDecoration spacing = new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent,
                                       @NonNull RecyclerView.State state) {
                outRect.bottom = 24;
                outRect.left = 8;
                outRect.right = 8;
            }
        };

        recyclerSummer.addItemDecoration(spacing);
        recyclerWinter.addItemDecoration(spacing);

        loadLooks(recyclerSummer, recyclerWinter);
    }


    private void loadLooks(RecyclerView recyclerSummer, RecyclerView recyclerWinter) {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseService.getInstance().getUserOutfitList(uid,
                new DatabaseService.DatabaseCallback<List<Outfit>>() {

                    @Override
                    public void onCompleted(List<Outfit> outfits) {

                        if (outfits == null) return;

                        List<Outfit> summerLooks = new java.util.ArrayList<>();
                        List<Outfit> winterLooks = new java.util.ArrayList<>();

                        for (Outfit outfit : outfits) {

                            if (outfit.getOuter() != null) {
                                winterLooks.add(outfit);
                            } else {
                                summerLooks.add(outfit);
                            }
                        }

                        OutfitAdapter summerAdapter =
                                new OutfitAdapter(savedlooks.this, summerLooks);

                        OutfitAdapter winterAdapter =
                                new OutfitAdapter(savedlooks.this, winterLooks);

                        RecyclerView recyclerSummer = findViewById(R.id.recyclerSummer);
                        RecyclerView recyclerWinter = findViewById(R.id.recyclerWinter);

                        recyclerSummer.setLayoutManager(new GridLayoutManager(savedlooks.this, 2));
                        recyclerWinter.setLayoutManager(new GridLayoutManager(savedlooks.this, 2));

                        recyclerSummer.setAdapter(summerAdapter);
                        recyclerWinter.setAdapter(winterAdapter);
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(savedlooks.this,
                                "Error loading outfits",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}