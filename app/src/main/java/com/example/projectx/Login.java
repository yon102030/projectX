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

    public static final String MyPREFERENCES = "MyPrefs";

    SharedPreferences sharedpreferences;
    private String email,password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login2);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);


        databaseService = DatabaseService.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.Emaillg);
        etPassword = findViewById(R.id.Passwordlg);
        btnLogin = findViewById(R.id.btnlogin);
        tvRegister = findViewById(R.id.registerpage);

        email = sharedpreferences.getString("email", "");
        password = sharedpreferences.getString("password", "");
        etEmail.setText(email);
        etPassword.setText(password);


        btnLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btnLogin.getId()) {
             email = etEmail.getText().toString().trim();
             password = etPassword.getText().toString().trim();

            if (!checkInput(email, password)) return;

            loginUser(email, password);

        } else if (v.getId() == tvRegister.getId()) {
            startActivity(new Intent(Login.this, register.class));
        }
    }

    private boolean checkInput(String email, String password) {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("כתובת אימייל לא תקינה");
            return false;
        }

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("הסיסמה חייבת להכיל לפחות 6 תווים");
            return false;
        }

        return true;
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
                            @Override
                            public void onCompleted(User user) {
                                Log.d(TAG, "Login success: " + user.getUserId());

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
                                Log.e(TAG, "Failed to get user", e);
                            }
                        });

                    } else {
                        etPassword.setError("אימייל או סיסמה שגויים");
                    }
                });
    }
}
