package com.example.projectx;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectx.adapter.UserAdapter;
import com.example.projectx.model.User;
import com.example.projectx.services.DatabaseService;

import java.util.ArrayList;
import java.util.List;

public class Userlist extends AppCompatActivity {

    private RecyclerView rvUsers;
    private DatabaseService databaseService;
    private UserAdapter adapter;
    private List<User> users;
private ImageButton btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_userlist);

        rvUsers = findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            finish(); // סוגר את העמוד הנוכחי וחוזר אחורה
        });
        databaseService = DatabaseService.getInstance();

        // 🔥 אתחול רשימה
        users = new ArrayList<>();

        // 🔥 יצירת adapter עם מחיקה
        adapter = new UserAdapter(users, (user, position) -> {

            databaseService.deleteUser(user.getUserId(), new DatabaseService.DatabaseCallback<Void>() {
                @Override
                public void onCompleted(Void object) {

                    users.remove(user); // ✅ לא לפי position
                    adapter.notifyDataSetChanged();

                    Toast.makeText(Userlist.this,
                            "User deleted",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailed(Exception e) {

                    Toast.makeText(Userlist.this,
                            "Delete failed",
                            Toast.LENGTH_SHORT).show();
                }
            });
        });


        rvUsers.setAdapter(adapter);

        // 🔥 טעינת משתמשים מהדאטהבייס
        databaseService.getUserList(new DatabaseService.DatabaseCallback<List<User>>() {

            @Override
            public void onCompleted(List<User> usersFromFirebase) {

                if (usersFromFirebase == null) {
                    Toast.makeText(Userlist.this,
                            "No users found",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                users.clear();
                users.addAll(usersFromFirebase);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Exception e) {

                Toast.makeText(Userlist.this,
                        "Failed to load users",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}