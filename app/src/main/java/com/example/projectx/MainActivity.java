package com.example.projectx;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

     Button signup, newacc, odot;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        signup = findViewById(R.id.signup);
        newacc = findViewById(R.id.newacc);
        odot = findViewById(R.id.odot);


        signup.setOnClickListener(this);
        newacc.setOnClickListener(this);
        odot.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.signup || id == R.id.newacc) {

            Intent intent = new Intent(MainActivity.this, register.class);
            startActivity(intent);
        } else if (id == R.id.odot) {

            Intent intent = new Intent(MainActivity.this, Odotp.class);
            startActivity(intent);
        }
    }
}
