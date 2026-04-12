package com.example.projectx;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectx.model.Outfit;
import com.example.projectx.services.DatabaseService;
import com.example.projectx.util.ImageUtil;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class SavedLooks extends AppCompatActivity {

    private LinearLayout rowSavedLooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savedlooks);

        rowSavedLooks = findViewById(R.id.rowSavedLooks);

        loadLooks();
    }

    private void loadLooks() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseService.getInstance().getUserOutfitList(uid,
                new DatabaseService.DatabaseCallback<List<Outfit>>() {
                    @Override
                    public void onCompleted(List<Outfit> outfits) {

                        rowSavedLooks.removeAllViews();

                        for (Outfit o : outfits) {

                            ImageView img = new ImageView(SavedLooksActivity.this);

                            Bitmap bmp = ImageUtil.convertFrom64base(o.getTop());
                            img.setImageBitmap(bmp);

                            int size = 200;
                            LinearLayout.LayoutParams params =
                                    new LinearLayout.LayoutParams(size, size);

                            params.setMargins(12, 12, 12, 12);
                            img.setLayoutParams(params);

                            img.setOnClickListener(v -> openDetails(o));

                            rowSavedLooks.addView(img);
                        }
                    }

                    @Override
                    public void onFailed(Exception e) { }
                });
    }

    private void openDetails(Outfit outfit) {

        Intent intent = new Intent(this, OutfitDetailsActivity.class);
        intent.putExtra("top", outfit.getTop());
        intent.putExtra("outer", outfit.getOuter());
        intent.putExtra("bottom", outfit.getBottom());
        startActivity(intent);
    }
}