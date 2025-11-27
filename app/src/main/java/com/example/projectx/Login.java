package com.example.projectx;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projectx.model.User;
import com.example.projectx.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private DatabaseService databaseService;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login2);

        // Handle system bars padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init Firebase and DatabaseService
        databaseService = DatabaseService.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.Emaillg);
        etPassword = findViewById(R.id.Passwordlg);
        btnLogin = findViewById(R.id.btnlogin);
        tvRegister = findViewById(R.id.registerpage);

        btnLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btnLogin.getId()) {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!checkInput(email, password)) return;

            loginUser(email, password);

        } else if (v.getId() == tvRegister.getId()) {
            Intent registerIntent = new Intent(Login.this, register.class);
            startActivity(registerIntent);
        }
    }

    private boolean checkInput(String email, String password) {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("כתובת אימייל לא תקינה");
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("הסיסמה חייבת להכיל לפחות 6 תווים");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get current user's UID
                        String uid = mAuth.getCurrentUser().getUid();

                        // Retrieve full user object from database
                        databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
                            @Override
                            public void onCompleted(User user) {
                                Log.d(TAG, "Login success, user: " + user.getUserId());

                                Intent BuildOIntent = new Intent(Login.this, BuildO.class);
                                BuildOIntent.putExtra("USER_ID", user.getUserId());
                                BuildOIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(BuildOIntent);
                            }

                            @Override
                            public void onFailed(Exception e) {
                                Log.e(TAG, "Failed to get user data", e);
                            }
                        });

                    } else {
                        etPassword.setError("אימייל או סיסמה שגויים");
                        etPassword.requestFocus();
                        Log.e(TAG, "Login failed", task.getException());
                    }
                });


    }
}
