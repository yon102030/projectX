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

        // קבלת הנתונים (שם, אימייל וכו') שהמסך הקודם "שלח" לנו, כדי להציג אותם ישר בתיבות הטקסט
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

        // שאיבת הטקסט החדש שהמנהל הקליד (הפקודה trim מנקה רווחים מיותרים בהתחלה ובסוף)
        String newFname = etEditFname.getText().toString().trim();
        String newLname = etEditLname.getText().toString().trim();
        String newPhone = etEditPhone.getText().toString().trim();

        // בדיקה בסיסית שאף שדה לא נשאר ריק
        if (newFname.isEmpty() || newLname.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        // יצירת "מילון" (Map) שמכיל רק את השדות שאנחנו רוצים לעדכן.
        // אנחנו עושים את זה כדי לא לדרוס בטעות מידע אחר של המשתמש (כמו סיסמה או אימייל).
        Map<String, Object> updates = new HashMap<>();
        updates.put("fName", newFname);
        updates.put("lName", newLname);
        updates.put("phone", newPhone);

        // פנייה לפיירבייס: הולכים לטבלת "users", מחפשים את המשתמש לפי ה-ID שלו,
        // ומפעילים את updateChildren שמעדכן נקודתית רק את מה ששמנו ב"מילון" (updates).
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