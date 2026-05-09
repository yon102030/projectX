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

    private EditText etEditFname, etEditLname, etEditPhone;
    private TextView tvEditEmail, tvEditPassword;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_user);

        // קישור רכיבים
        etEditFname = findViewById(R.id.etEditFname);
        etEditLname = findViewById(R.id.etEditLname);
        etEditPhone = findViewById(R.id.etEditPhone);

        tvEditEmail = findViewById(R.id.tvEditEmail);
        tvEditPassword = findViewById(R.id.tvEditPassword);

        // קבלת נתונים
        userId = getIntent().getStringExtra("userId");

        etEditFname.setText(getIntent().getStringExtra("fname"));
        etEditLname.setText(getIntent().getStringExtra("lname"));
        etEditPhone.setText(getIntent().getStringExtra("phone"));

        tvEditEmail.setText(getIntent().getStringExtra("email"));
        tvEditPassword.setText(getIntent().getStringExtra("password"));

        // ביטול
        findViewById(R.id.btnCancelEdit).setOnClickListener(v -> finish());

        // שמירה
        findViewById(R.id.btnSaveEdit).setOnClickListener(v -> saveUserUpdates());
    }

    private void saveUserUpdates() {

        String newFname = etEditFname.getText().toString().trim();
        String newLname = etEditLname.getText().toString().trim();
        String newPhone = etEditPhone.getText().toString().trim();

        if (newFname.isEmpty() || newLname.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("fName", newFname);
        updates.put("lName", newLname);
        updates.put("phone", newPhone);

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .updateChildren(updates)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "שגיאה בעדכון", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}