package com.example.projectx;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class splash extends AppCompatActivity {

    private ImageView logo;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final int[] images = {
            R.drawable.blacktshirt,
            R.drawable.blackzarajacket,
            R.drawable.bluejeans,
            R.drawable.brownzarajeans,
            R.drawable.grayzarajeans
    };

    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logo = findViewById(R.id.logo);

        logo.setImageResource(images[0]);
        logo.setAlpha(1f);

        handler.postDelayed(changeImage, 1000);

        handler.postDelayed(() -> {
            startActivity(new Intent(splash.this, MainActivity.class));
            finish();
        }, 4000);
    }

    private final Runnable changeImage = new Runnable() {
        @Override
        public void run() {

            index = (index + 1) % images.length;

            logo.animate()
                    .alpha(0f)
                    .setDuration(250)
                    .withEndAction(() -> {
                        logo.setImageResource(images[index]);

                        logo.animate()
                                .alpha(1f)
                                .setDuration(250)
                                .start();
                    })
                    .start();

            handler.postDelayed(this, 900);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}