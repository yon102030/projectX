package com.example.projectx;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectx.util.ImageUtil;

public class OutfitDetailsActivity extends AppCompatActivity {

    private ImageView ivTop, ivOuter, ivBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_details);

        ivTop = findViewById(R.id.ivSelectedTop);
        ivOuter = findViewById(R.id.ivSelectedOuter);
        ivBottom = findViewById(R.id.ivSelectedBottom);

        String top = getIntent().getStringExtra("top");
        String outer = getIntent().getStringExtra("outer");
        String bottom = getIntent().getStringExtra("bottom");

        // ================= SAFE TOP =================
        if (top != null && !top.isEmpty()) {
            Bitmap topBmp = ImageUtil.convertFrom64base(top);
            if (topBmp != null) ivTop.setImageBitmap(topBmp);
        }

        // ================= SAFE OUTER =================
        if (outer != null && !outer.isEmpty()) {
            Bitmap outerBmp = ImageUtil.convertFrom64base(outer);
            if (outerBmp != null) {
                ivOuter.setVisibility(View.VISIBLE);
                ivOuter.setImageBitmap(outerBmp);
            } else {
                ivOuter.setVisibility(View.GONE);
            }
        } else {
            ivOuter.setVisibility(View.GONE);
        }

        // ================= SAFE BOTTOM =================
        if (bottom != null && !bottom.isEmpty()) {
            Bitmap bottomBmp = ImageUtil.convertFrom64base(bottom);
            if (bottomBmp != null) ivBottom.setImageBitmap(bottomBmp);
        }
    }
}