package com.example.projectx;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

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

    public static final String MyPREFERENCES = "MyPrefs";
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login2);

        databaseService = DatabaseService.getInstance();
        mAuth = FirebaseAuth.getInstance();

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        etEmail = findViewById(R.id.Emaillg);
        etPassword = findViewById(R.id.Passwordlg);
        btnLogin = findViewById(R.id.btnlogin);
        tvRegister = findViewById(R.id.registerpage);

        btnLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this);

        // 🔥 טעינה אוטומטית של נתונים שמורים
        loadSavedData();
    }

    // =========================
    // 🔥 טעינת אימייל וסיסמה
    // =========================
    private void loadSavedData() {
        String email = sharedpreferences.getString("email", "");
        String password = sharedpreferences.getString("password", "");

        if (!email.isEmpty()) {
            etEmail.setText(email);
        }

        if (!password.isEmpty()) {
            etPassword.setText(password);
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == btnLogin.getId()) {

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!checkInput(email, password)) return;

            loginUser(email, password);

        } else if (v.getId() == tvRegister.getId()) {
            startActivity(new Intent(Login.this, register.class));
        }
    }

    private boolean checkInput(String email, String password) {

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email");
            return false;
        }

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Min 6 characters");
            return false;
        }

        return true;
    }

    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        etPassword.setError("Wrong email or password");
                        return;
                    }

                    String uid = mAuth.getCurrentUser().getUid();

                    databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
                        @Override
                        public void onCompleted(User user) {

                            // 🔥 שמירה לפעם הבאה
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("email", email);
                            editor.putString("password", password);
                            editor.apply();

                            Intent intent;

                            if (user.isAdmin()) {
                                intent = new Intent(Login.this, Adminpage.class);
                            } else {
                                intent = new Intent(Login.this, userpage.class);
                            }

                            intent.putExtra("USER_NAME", user.getfName());
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Log.e(TAG, "getUser failed", e);
                        }
                    });
                });
    }
}