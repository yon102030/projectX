package com.example.projectx;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectx.adapter.UserAdapter;
import com.example.projectx.model.User;
import com.example.projectx.services.DatabaseService;

import java.util.ArrayList;
import java.util.List;

// מסך המציג את רשימת כל המשתמשים באפליקציה (עבור המנהל), ומאפשר למחוק אותם או לצפות בהם
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

        // הגדרת הרשימה כרשימה אנכית (אחד מתחת לשני)
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish(); // סוגר את העמוד הנוכחי וחוזר אחורה
        });

        databaseService = DatabaseService.getInstance();

        // 🔥 אתחול רשימה ריקה כדי למנוע קריסות לפני שהנתונים מגיעים מהשרת
        users = new ArrayList<>();

        // 🔥 יצירת המתאם (Adapter) שמחבר בין הנתונים למסך, והגדרת פעולת המחיקה
        adapter = new UserAdapter(users, (user, position) -> {

            // פנייה לפיירבייס בבקשה למחוק את המשתמש לפי ה-ID שלו
            databaseService.deleteUser(user.getUserId(), new DatabaseService.DatabaseCallback<Void>() {

                @Override
                public void onCompleted(Void object) {
                    // אם המחיקה בשרת הצליחה - מסירים את המשתמש גם מהרשימה שמוצגת כרגע במסך
                    users.remove(user); // ✅ מחיקה בטוחה ישירות לפי האובייקט ולא לפי המיקום (position)

                    // מודיעים למתאם שהרשימה השתנתה כדי שייעלים את השורה מהמסך
                    adapter.notifyDataSetChanged();
                    Toast.makeText(Userlist.this, "User deleted", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(Userlist.this, "Delete failed", Toast.LENGTH_SHORT).show();
                }
            });
        });

        rvUsers.setAdapter(adapter);
    }

    // ==========================================
    // טריק חכם: שימוש ב-onResume לרענון אוטומטי
    // ==========================================
    // הפעולה הזו מופעלת אוטומטית על ידי אנדרואיד בכל פעם שהמסך הזה מופיע מחדש בחזית.
    // זה מעולה למקרה שהמנהל לחץ על משתמש, עבר למסך "עריכה", שינה לו את השם, וחזר לכאן.
    // הפונקציה תוודא שהרשימה תטען את הנתונים המעודכנים מיד.
    @Override
    protected void onResume() {
        super.onResume();
        loadUsers(); // קריאה לפונקציית טעינת הנתונים
    }

    // פונקציה ייעודית שפונה לשרת כדי לשלוף את כל המשתמשים
    private void loadUsers() {
        databaseService.getUserList(new DatabaseService.DatabaseCallback<List<User>>() {

            @Override
            public void onCompleted(List<User> usersFromFirebase) {
                if (usersFromFirebase == null) {
                    Toast.makeText(Userlist.this, "No users found", Toast.LENGTH_SHORT).show();
                    return;
                }

                // מנקים את הרשימה הישנה שמוצגת כרגע
                users.clear();

                // מכניסים את כל הנתונים הטריים שקיבלנו הרגע מפיירבייס
                users.addAll(usersFromFirebase);

                // מרעננים את התצוגה הגרפית
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(Userlist.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }
}