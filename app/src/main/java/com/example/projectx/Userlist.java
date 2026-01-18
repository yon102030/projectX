package com.example.projectx;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import android.widget.ArrayAdapter;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projectx.model.User;
import com.example.projectx.services.DatabaseService;

import java.util.List;

public class Userlist extends AppCompatActivity {

    private ListView lvUsers;
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

        lvUsers = findViewById(R.id.lvUsers);
        databaseService = DatabaseService.getInstance();

        databaseService.getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {

                ArrayList<String> userList = new ArrayList<>();

                for (User u : users) {
                    userList.add(
                            u.getfName() + " " +
                                    u.getlName() + " - " +
                                    u.getEmail()
                    );
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        Userlist.this,
                        android.R.layout.simple_list_item_1,
                        userList
                );

                lvUsers.setAdapter(adapter);
            }

            @Override
            public void onFailed(Exception e) {
                ArrayList<String> error = new ArrayList<>();
                error.add("Failed to load users");

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        Userlist.this,
                        android.R.layout.simple_list_item_1,
                        error
                );

                lvUsers.setAdapter(adapter);
            }
        });
    }
}

