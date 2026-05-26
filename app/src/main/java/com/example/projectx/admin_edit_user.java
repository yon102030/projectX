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

// מחלקה זו מנהלת את המסך שבו המנהל (Admin) יכול לערוך פרטים של משתמש קיים
public class admin_edit_user extends AppCompatActivity {

    private EditText etEditFname, etEditLname, etEditPhone;
    private TextView tvEditEmail, tvEditPassword;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_user);

        // קישור רכיבים מהעיצוב לקוד
        etEditFname = findViewById(R.id.etEditFname);
        etEditLname = findViewById(R.id.etEditLname);
        etEditPhone = findViewById(R.id.etEditPhone);

        tvEditEmail = findViewById(R.id.tvEditEmail);
        tvEditPassword = findViewById(R.id.tvEditPassword);

        // קבלת הנתונים שהמסך הקודם "שלח" לנו, כדי להציג אותם ישר בתיבות הטקסט
        userId = getIntent().getStringExtra("userId");
        etEditFname.setText(getIntent().getStringExtra("fname"));
        etEditLname.setText(getIntent().getStringExtra("lname"));
        etEditPhone.setText(getIntent().getStringExtra("phone"));
        tvEditEmail.setText(getIntent().getStringExtra("email"));
        tvEditPassword.setText(getIntent().getStringExtra("password"));

        // כפתור ביטול פשוט סוגר את המסך (finish) ומחזיר אותנו אחורה
        findViewById(R.id.btnCancelEdit).setOnClickListener(v -> finish());

        // כפתור שמירה מפעיל את פונקציית העדכון שיצרנו למטה
        findViewById(R.id.btnSaveEdit).setOnClickListener(v -> saveUserUpdates());
    }

    // הפונקציה שאוספת את הטקסט המעודכן ושומרת אותו בפיירבייס
    private void saveUserUpdates() {

        // שאיבת הטקסט החדש שהמנהל הקליד
        String newFname = etEditFname.getText().toString().trim();
        String newLname = etEditLname.getText().toString().trim();
        String newPhone = etEditPhone.getText().toString().trim();

        // 1. בדיקה בסיסית שאף שדה לא נשאר ריק
        if (newFname.isEmpty()) {
            etEditFname.setError("חובה להזין שם פרטי");
            return;
        }

        if (newLname.isEmpty()) {
            etEditLname.setError("חובה להזין שם משפחה");
            return;
        }

        if (newPhone.isEmpty()) {
            etEditPhone.setError("חובה להזין מספר טלפון");
            return;
        }

        // 2. 🔥 בדיקת תקינות מספר הטלפון בעזרת ביטוי רגולרי (Regex)
        // מוודא שהקלט מתחיל ב-05 ומכיל בדיוק עוד 8 ספרות (סה"כ 10 ספרות)
        if (!newPhone.matches("^05\\d{8}$")) {
            etEditPhone.setError("מספר טלפון לא תקין (חייב להכיל 10 ספרות ולהתחיל ב-05)");
            return;
        }

        // יצירת "מילון" (Map) שמכיל רק את השדות שאנחנו רוצים לעדכן
        Map<String, Object> updates = new HashMap<>();
        updates.put("fName", newFname);
        updates.put("lName", newLname);
        updates.put("phone", newPhone);

        // פנייה לפיירבייס ועדכון נקודתי בעזרת updateChildren
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .updateChildren(updates)
                .addOnCompleteListener(task -> {

                    // בדיקה אם העדכון בשרת עבר בהצלחה
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                        finish(); // סוגר את המסך אחרי שמירה מוצלחת
                    } else {
                        Toast.makeText(this, "שגיאה בעדכון", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}