package com.example.projectx;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class splash extends AppCompatActivity {

    ImageView logo;
    Handler handler = new Handler();

    // 👈 כאן מוסיפים את כל התמונות שיתחלפו
    int[] images = {
            R.drawable.blacktshirt,
            R.drawable.blackzarajacket,
            R.drawable.bluejeans,
            R.drawable.brownzarajeans,
            R.drawable.grayzarajeans

    };

    int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logo = findViewById(R.id.logo);

        // תמונה ראשונה
        logo.setImageResource(images[0]);

        // החלפת תמונה כל 800ms
        handler.postDelayed(changeImage, 500);

        // מעבר למסך הראשי אחרי 3 שניות
        handler.postDelayed(() -> {
            startActivity(new Intent(splash.this, MainActivity.class));
            finish();
        }, 3000);
    }

    Runnable changeImage = new Runnable() {
        @Override
        public void run() {
            index = (index + 1) % images.length;
            logo.setImageResource(images[index]);
            handler.postDelayed(this, 800);
        }
    };
}
