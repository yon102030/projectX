package com.example.projectx;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

// מחלקה זו מנהלת את מסך הראשי של המנהל (Admin), ומשמשת כתפריט ניווט לכל פעולות הניהול
public class Adminpage extends AppCompatActivity {

    private TextView tvGreeting;
    private Button btnLogout, btnUserList, btnItemList, btnStats, btnManageApp;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminpage);

        // קישור כל הכפתורים ותיבות הטקסט מהעיצוב (XML)
        tvGreeting = findViewById(R.id.tvGreeting);
        btnLogout = findViewById(R.id.btnLogout);
        btnUserList = findViewById(R.id.btnUserList);
        btnItemList = findViewById(R.id.itemlist);
        btnStats = findViewById(R.id.btnStats);
        btnManageApp = findViewById(R.id.btnManageApp);
        btnBack = findViewById(R.id.btnBack);

        // שליפת שם המנהל שהועבר לכאן ממסך ההתחברות כדי להציג לו הודעת ברכה אישית
        String adminName = getIntent().getStringExtra("USER_NAME");
        tvGreeting.setText("שלום " + (adminName != null ? adminName : "מנהל"));

        // ==========================================
        // הגדרת פעולות לחיצה (ניווט למסכים שונים)
        // ==========================================

        // מעבר למסך סטטיסטיקות
        btnStats.setOnClickListener(v -> startActivity(new Intent(this, AdminStatsActivity.class)));

        // מעבר למסך ניהול קטגוריות (למשל צבעים)
        btnManageApp.setOnClickListener(v -> startActivity(new Intent(this, AdminManageActivity.class)));

        // מעבר למסך רשימת כל המשתמשים באפליקציה
        btnUserList.setOnClickListener(v -> startActivity(new Intent(this, Userlist.class)));

        // מעבר למסך המציג את כל הפריטים (הבגדים) שהועלו למערכת
        btnItemList.setOnClickListener(v -> startActivity(new Intent(this, itemlist.class)));

        // סגירת המסך הנוכחי (חזרה אחורה)
        btnBack.setOnClickListener(v -> finish());

        // פעולת ההתנתקות (Logout)
        btnLogout.setOnClickListener(v -> {
            // ניתוק המשתמש הנוכחי ממערכת ההזדהות של Firebase
            FirebaseAuth.getInstance().signOut();

            // יצירת בקשת מעבר למסך הראשי (MainActivity - כנראה מסך ההתחברות)
            Intent intent = new Intent(this, MainActivity.class);

            // לוגיקה חשובה: הפקודות האלו מנקות את כל "היסטוריית" המסכים מהזיכרון.
            // זה מונע מצב שבו המנהל התנתק, אבל מישהו שלוקח לו את הטלפון ילחץ על הלחצן "חזור" של המכשיר ויכנס שוב לעמוד הניהול ללא סיסמה.
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
        });
    }
}