package com.example.projectx;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projectx.model.User;
import com.example.projectx.services.DatabaseService;

// מסך ההרשמה של האפליקציה: אוסף נתונים מהמשתמש, יוצר לו חשבון חדש ושומר את הפרטים.
public class register extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    private EditText etEmail, etPassword, etFName, etLName, etPhone;
    private Button btnRegister, tvLogin;
    private ImageButton btnBack;

    DatabaseService databaseService;

    // הגדרת קובץ הזיכרון המקומי (כדי לשמור בו את האימייל והסיסמה בסוף ההרשמה)
    public static final String MyPREFERENCES = "MyPrefs";
    SharedPreferences sharedpreferences;

    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // התאמת התצוגה למסכים מודרניים (כדי שהטקסט לא יוסתר מתחת לשורת המצב של הטלפון)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseService = DatabaseService.getInstance();
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        // קישור הרכיבים מהעיצוב לקוד
        etEmail = findViewById(R.id.Email);
        etPassword = findViewById(R.id.Password);
        etFName = findViewById(R.id.Fname);
        etLName = findViewById(R.id.Lname);
        etPhone = findViewById(R.id.Phone);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tblogin);
        btnBack = findViewById(R.id.btnBack);

        // הגדרת מאזיני הלחיצות
        btnRegister.setOnClickListener(this);
        tvLogin.setOnClickListener(this);

        // מעבר למסך התחברות (במקרה שלמשתמש כבר יש חשבון)
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(register.this, Login.class);
            startActivity(intent);
        });

        // כפתור חזרה למסך הפתיחה תוך מחיקת היסטוריית המסכים (מונע קריסות בחזרה לאחור)
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(register.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    // פונקציה שמרכזת את הטיפול בלחיצות במסך (מופעלת בזכות ה-implements למעלה)
    @Override
    public void onClick(View v) {

        // אם לחצו על "הרשמה"
        if (v.getId() == btnRegister.getId()) {
            Log.d(TAG, "onClick: Register button clicked");

            // שאיבת כל הטקסטים שהמשתמש הקליד
            email = etEmail.getText().toString().trim();
            password = etPassword.getText().toString().trim();
            String fName = etFName.getText().toString().trim();
            String lName = etLName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            // הפעלת בדיקות תקינות קלט - אם הבדיקה נכשלה, נעצור כאן ולא נמשיך ברישום
            if (!validateInput(fName, lName, phone, email, password)) {
                return;
            }

            Log.d(TAG, "onClick: Registering user...");

            // שליחת הנתונים לפונקציית העזר שמכינה אותם לשמירה
            registerUser(fName, lName, phone, email, password);

        } else if (v.getId() == tvLogin.getId()) {
            finish(); // סגירת מסך ההרשמה וחזרה אחורה
        }
    }

    // פונקציה חכמה לבדיקת תקינות הקלט שהזין המשתמש
    private boolean validateInput(String fName, String lName, String phone, String email, String password) {

        if (fName.isEmpty()) {
            etFName.setError("חובה להזין שם פרטי");
            return false;
        }

        if (lName.isEmpty()) {
            etLName.setError("חובה להזין שם משפחה");
            return false;
        }

        // 🔥 בדיקת תקינות מספר הטלפון
        if (phone.isEmpty()) {
            etPhone.setError("חובה להזין מספר טלפון");
            return false;
        }
        // בדיקה שהטלפון מכיל רק ספרות, באורך 10 בדיוק, ומתחיל ב-05
        if (!phone.matches("^05\\d{8}$")) {
            etPhone.setError("מספר טלפון לא תקין (חייב להכיל 10 ספרות ולהתחיל ב-05)");
            return false;
        }

        if (email.isEmpty()) {
            etEmail.setError("חובה להזין אימייל");
            return false;
        }
        // בדיקת מבנה אימייל בסיסי
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("כתובת אימייל לא תקינה");
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("חובה להזין סיסמה");
            return false;
        }
        if (password.length() < 6) {
            etPassword.setError("הסיסמה חייבת להכיל לפחות 6 תווים");
            return false;
        }

        return true; // כל השדות תקינים!
    }

    // פונקציה שמכינה את אובייקט המשתמש (User) לקראת שמירה
    private void registerUser(String fname, String lname, String phone, String email, String password) {
        Log.d(TAG, "registerUser: Registering user...");

        // יצירת אובייקט חדש של משתמש.
        User user = new User("4545", fname, lname, phone, email, password, false);

        // קריאה לפונקציה שמדברת בפועל עם פיירבייס
        createUserInDatabase(user);
    }

    // פונקציה שפונה לשרת (DatabaseService) ויוצרת את המשתמש גם במערכת האימות וגם במסד הנתונים
    private void createUserInDatabase(User user) {
        databaseService.createNewUser(user, new DatabaseService.DatabaseCallback<String>() {

            // מה קורה אם ההרשמה עברה בהצלחה? (מקבלים חזרה את ה-ID הייחודי של המשתמש: uid)
            @Override
            public void onCompleted(String uid) {
                Log.d(TAG, "createUserInDatabase: User created successfully");

                // עדכון ה-ID האמיתי של המשתמש שקיבלנו מהשרת
                user.setUserId(uid);

                // פועלה חכמה: שומרים את האימייל והסיסמה בזיכרון הטלפון
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("email", email);
                editor.putString("password", password);
                editor.apply();

                Log.d(TAG, "createUserInDatabase: Redirecting to MainActivity");

                // מעבר למסך הראשי אחרי הרשמה מוצלחת ומחיקת מסך ההרשמה מההיסטוריה
                Intent mainIntent = new Intent(register.this, MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
            }

            // מה קורה אם הייתה שגיאה?
            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "createUserInDatabase: Failed to create user", e);
                Toast.makeText(register.this, "ההרשמה נכשלה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}