package com.example.projectx;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class Adminpage extends AppCompatActivity {

    private TextView tvGreeting;
    private Button btnLogout, btnUserList, btnItemList, btnStats, btnManageApp;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminpage);

        tvGreeting = findViewById(R.id.tvGreeting);
        btnLogout = findViewById(R.id.btnLogout);
        btnUserList = findViewById(R.id.btnUserList);
        btnItemList = findViewById(R.id.itemlist);
        btnStats = findViewById(R.id.btnStats);
        btnManageApp = findViewById(R.id.btnManageApp);
        btnBack = findViewById(R.id.btnBack);

        String adminName = getIntent().getStringExtra("USER_NAME");
        tvGreeting.setText("שלום " + (adminName != null ? adminName : "מנהל"));

        // ניווט לסטטיסטיקה
        btnStats.setOnClickListener(v -> startActivity(new Intent(this, AdminStatsActivity.class)));

        // ניווט לניהול קטגוריות
        btnManageApp.setOnClickListener(v -> startActivity(new Intent(this, AdminManageActivity.class)));

        // ניווט לרשימת משתמשים
        btnUserList.setOnClickListener(v -> startActivity(new Intent(this, Userlist.class)));

        // ניווט לכל הבגדים
        btnItemList.setOnClickListener(v -> startActivity(new Intent(this, itemlist.class)));

        btnBack.setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}