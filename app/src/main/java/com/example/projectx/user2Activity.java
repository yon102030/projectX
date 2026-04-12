package com.example.projectx;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectx.model.Outfit;
import com.example.projectx.services.DatabaseService;
import com.example.projectx.util.ImageUtil;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class user2Activity extends AppCompatActivity {

    private ImageView ivTop, ivOuter, ivBottom;
    private Button btnRefresh, btnSaveLook;

    private final Random random = new Random();

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

        btnRefresh = findViewById(R.id.btnRefresh);
        btnSaveLook = findViewById(R.id.btnSaveLook);

        btnRefresh.setOnClickListener(v -> setRandomLook());
        btnSaveLook.setOnClickListener(v -> saveLook());
    }

    private void setRandomLook() {

        if (!topClothes.isEmpty()) {
            Clothe top = topClothes.get(random.nextInt(topClothes.size()));
            ivTop.setImageBitmap(ImageUtil.convertFrom64base(top.getImageUrl()));
        }

        if (!outerClothes.isEmpty()) {
            Clothe outer = outerClothes.get(random.nextInt(outerClothes.size()));
            ivOuter.setVisibility(View.VISIBLE);
            ivOuter.setImageBitmap(ImageUtil.convertFrom64base(outer.getImageUrl()));
        } else {
            ivOuter.setVisibility(View.GONE);
        }

        if (!bottomClothes.isEmpty()) {
            Clothe bottom = bottomClothes.get(random.nextInt(bottomClothes.size()));
            ivBottom.setImageBitmap(ImageUtil.convertFrom64base(bottom.getImageUrl()));
        }
    }

    private void saveLook() {

        String outfitId = DatabaseService.getInstance().generateOutfitId();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String top = ImageUtil.convertTo64Base(ivTop);
        String outer = ivOuter.getVisibility() == View.VISIBLE
                ? ImageUtil.convertTo64Base(ivOuter)
                : null;
        String bottom = ImageUtil.convertTo64Base(ivBottom);

        Outfit outfit = new Outfit(outfitId, userId, top, outer, bottom);

        DatabaseService.getInstance().createNewOutfit(outfit,
                new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void object) {
                        Toast.makeText(user2Activity.this,
                                "Saved!",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(user2Activity.this,
                                "Error saving",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}