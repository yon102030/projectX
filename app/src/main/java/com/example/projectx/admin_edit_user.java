package com.example.projectx;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class admin_edit_user extends AppCompatActivity {

    private EditText etEditFname, etEditLname, etEditPhone, etEditPassword;
    private TextView tvEditEmail;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_user);

        // 1. קישור רכיבים מה-XML
        etEditFname = findViewById(R.id.etEditFname);
        etEditLname = findViewById(R.id.etEditLname);
        etEditPhone = findViewById(R.id.etEditPhone);
        etEditPassword = findViewById(R.id.etEditPassword);
        tvEditEmail = findViewById(R.id.tvEditEmail);

        // 2. קבלת הנתונים מה-Intent (מהמסך הקודם)
        userId = getIntent().getStringExtra("userId");
        etEditFname.setText(getIntent().getStringExtra("fname"));
        etEditLname.setText(getIntent().getStringExtra("lname"));
        etEditPhone.setText(getIntent().getStringExtra("phone"));
        etEditPassword.setText(getIntent().getStringExtra("password"));
        tvEditEmail.setText(getIntent().getStringExtra("email")); // מופיע כטקסט שלא ניתן לעריכה

        // 3. כפתור ביטול
        findViewById(R.id.btnCancelEdit).setOnClickListener(v -> finish());

        // 4. כפתור שמירה
        findViewById(R.id.btnSaveEdit).setOnClickListener(v -> saveUserUpdates());
    }

    private void saveUserUpdates() {
        String newFname = etEditFname.getText().toString().trim();
        String newLname = etEditLname.getText().toString().trim();
        String newPhone = etEditPhone.getText().toString().trim();
        String newPassword = etEditPassword.getText().toString().trim();

        // בדיקות תקינות הקלט
        if (newFname.isEmpty() || newLname.isEmpty() || newPhone.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "הסיסמה חייבת להכיל לפחות 6 תווים", Toast.LENGTH_SHORT).show();
            return;
        }

        // יצירת מפת העדכונים (שומרים לפי שמות המשתנים המדויקים במחלקה User)
        Map<String, Object> updates = new HashMap<>();
        updates.put("fName", newFname);  // שים לב לאות הגדולה ב-fName לפי המודל שלך
        updates.put("lName", newLname);  // שים לב לאות הגדולה ב-lName לפי המודל שלך
        updates.put("phone", newPhone);
        updates.put("password", newPassword);

        // שליחה ל-Firebase
        FirebaseDatabase.getInstance().getReference("users").child(userId)
                .updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(admin_edit_user.this, "פרטי המשתמש עודכנו בהצלחה!", Toast.LENGTH_SHORT).show();
                        finish(); // חזרה לרשימת המשתמשים
                    } else {
                        Toast.makeText(admin_edit_user.this, "שגיאה בעדכון הנתונים", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}