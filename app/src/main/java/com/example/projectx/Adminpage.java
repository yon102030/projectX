package com.example.projectx;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projectx.model.User;
import com.example.projectx.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

public class Adminpage extends AppCompatActivity {

    private TextView tvGreeting;
    private Button btnLogout, btnUserList, itemlist, additem;
    private DatabaseService databaseService;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_adminpage);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseService = DatabaseService.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvGreeting = findViewById(R.id.tvGreeting);
        btnLogout = findViewById(R.id.btnLogout);
        btnUserList = findViewById(R.id.btnUserList);
        itemlist=findViewById(R.id.itemlist);
        additem=findViewById(R.id.additem);

        // קבלת שם המנהל מה-Intent
        String adminName = getIntent().getStringExtra("USER_NAME");
        tvGreeting.setText("שלום " + adminName);

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(Adminpage.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        btnUserList.setOnClickListener(v -> {
            Intent intent = new Intent(Adminpage.this, Userlist.class);
            startActivity(intent);
        });
        itemlist.setOnClickListener(v -> {
            Intent intent = new Intent(Adminpage.this, itemlist.class);
            startActivity(intent);
        });
        additem.setOnClickListener(v -> {
            Intent intent = new Intent(Adminpage.this, AddClothe.class);
            startActivity(intent);
        });

    }
}
