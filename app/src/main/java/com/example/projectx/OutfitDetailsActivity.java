package com.example.projectx;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectx.model.Outfit;
import com.example.projectx.services.DatabaseService;
import com.example.projectx.util.ImageUtil;

public class OutfitDetailsActivity extends AppCompatActivity {

    private ImageView ivTop, ivOuter, ivBottom;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_details);

        ivTop = findViewById(R.id.ivSelectedTop);
        ivOuter = findViewById(R.id.ivSelectedOuter);
        ivBottom = findViewById(R.id.ivSelectedBottom);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        String outfitId = getIntent().getStringExtra("outfitId");

        if (outfitId == null) {
            Toast.makeText(this, "Missing outfit", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseService.getInstance().getOutfit(outfitId,
                new DatabaseService.DatabaseCallback<Outfit>() {

                    @Override
                    public void onCompleted(Outfit o) {

                        if (o == null) return;

                        if (o.getTop() != null)
                            ivTop.setImageBitmap(ImageUtil.convertFrom64base(o.getTop()));

                        if (o.getOuter() != null && !o.getOuter().isEmpty()) {
                            ivOuter.setVisibility(View.VISIBLE);
                            ivOuter.setImageBitmap(ImageUtil.convertFrom64base(o.getOuter()));
                        } else {
                            ivOuter.setVisibility(View.GONE);
                        }

                        if (o.getBottom() != null)
                            ivBottom.setImageBitmap(ImageUtil.convertFrom64base(o.getBottom()));
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(OutfitDetailsActivity.this,
                                "Error loading outfit",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}