package com.example.projectx;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projectx.model.User;
import com.example.projectx.services.DatabaseService;

import java.util.List;

public class Userlist extends AppCompatActivity {

    private TextView tvUsers;
    private DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_userlist);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvUsers = findViewById(R.id.tvUsers);
        databaseService = DatabaseService.getInstance();

        databaseService.getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                StringBuilder sb = new StringBuilder();
                for (User u : users) {
                    sb.append(u.getfName())
                            .append(" ")
                            .append(u.getlName())
                            .append(" - ")
                            .append(u.getEmail())
                            .append("\n");
                }
                tvUsers.setText(sb.toString());
            }

            @Override
            public void onFailed(Exception e) {
                tvUsers.setText("Failed to load users");
            }
        });
    }
}
