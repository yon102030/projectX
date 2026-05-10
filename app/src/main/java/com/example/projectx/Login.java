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
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectx.model.User;
import com.example.projectx.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

// מסך ההתחברות לאפליקציה. מנהל את האימות מול פיירבייס ומנתב את המשתמש למסך המתאים (מנהל/משתמש רגיל)
public class Login extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ImageButton btnBack;

    private DatabaseService databaseService;
    private FirebaseAuth mAuth; // הרכיב של פיירבייס שאחראי על ניהול משתמשים וסיסמאות

    // קבוע לשם קובץ ההגדרות המקומי שבו נשמור את פרטי ההתחברות
    public static final String MyPREFERENCES = "MyPrefs";
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login2);

        databaseService = DatabaseService.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // פתיחת הגישה לקובץ הזיכרון המקומי של האפליקציה (MODE_PRIVATE אומר שרק האפליקציה שלנו יכולה לגשת לזה)
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        etEmail = findViewById(R.id.Emaillg);
        etPassword = findViewById(R.id.Passwordlg);
        btnLogin = findViewById(R.id.btnlogin);
        tvRegister = findViewById(R.id.registerpage);

        btnLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, MainActivity.class);

            // לוגיקה חשובה: מוחק את כל היסטוריית המסכים (הסטאק) לפני שחוזרים למסך הראשי
            // זה מונע באגים וקריסות אם המשתמש ילחץ הרבה פעמים "חזור"
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
        });

        // 🔥 טעינה אוטומטית של נתונים שמורים (מילוי אוטומטי של אימייל וסיסמה אם המשתמש כבר התחבר בעבר)
        loadSavedData();
    }

    // =========================
    // 🔥 טעינת אימייל וסיסמה
    // =========================
    private void loadSavedData() {
        // משיכת הערכים מתוך הזיכרון המקומי. אם אין כלום, הוא יחזיר מחרוזת ריקה ("")
        String email = sharedpreferences.getString("email", "");
        String password = sharedpreferences.getString("password", "");

        // אם יש מידע שמור, אנחנו שותלים אותו ישירות בתיבות הטקסט כדי לחסוך הקלדה
        if (!email.isEmpty()) {
            etEmail.setText(email);
        }
        if (!password.isEmpty()) {
            etPassword.setText(password);
        }
    }

    // פונקציה מרכזית אחת שמטפלת בכל הלחיצות במסך (מופעלת בזכות ה-implements View.OnClickListener למעלה)
    @Override
    public void onClick(View v) {

        if (v.getId() == btnLogin.getId()) {

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // קורא לפונקציה שבודקת שאין שגיאות הקלדה. אם יש שגיאה (return false), עוצרים פה.
            if (!checkInput(email, password)) return;

            loginUser(email, password);

        } else if (v.getId() == tvRegister.getId()) {
            // מעבר למסך הרשמה
            startActivity(new Intent(Login.this, register.class));
        }
    }

    // פונקציית הגנה: בודקת שהמשתמש הזין נתונים הגיוניים לפני שפונים לשרת
    private boolean checkInput(String email, String password) {

        // בודק גם אם התיבה ריקה וגם אם הפורמט אינו אימייל חוקי (כמו a@b.com) בעזרת Patterns המובנה באנדרואיד
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email");
            return false;
        }

        // פיירבייס דורש מינימום 6 תווים לסיסמה, אז אנחנו בודקים את זה מראש
        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Min 6 characters");
            return false;
        }

        return true;
    }

    // =========================
    // 🔥 התחברות לשרת (פיירבייס)
    // =========================
    private void loginUser(String email, String password) {

        // פנייה לשירות האימות של פיירבייס עם האימייל והסיסמה
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    // אם ההתחברות נכשלה (למשל סיסמה שגויה) - מציגים הודעת שגיאה ועוצרים
                    if (!task.isSuccessful()) {
                        etPassword.setError("Wrong email or password");
                        return;
                    }

                    // אם הצליח: שולפים את ה-ID הייחודי של המשתמש שזה עתה התחבר
                    String uid = mAuth.getCurrentUser().getUid();

                    // עכשיו פונים למסד הנתונים שלנו כדי לשלוף את כל הפרטים של המשתמש (כדי לדעת אם הוא מנהל)
                    databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
                        @Override
                        public void onCompleted(User user) {

                            // 🔥 אחרי שהתחברנו בהצלחה, אנחנו שומרים את האימייל והסיסמה בזיכרון המכשיר לפעם הבאה
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("email", email);
                            editor.putString("password", password);
                            editor.apply();

                            Intent intent;

                            // 🌟 ניתוב חכם: בודקים את המאפיין isAdmin של המשתמש שחזר ממסד הנתונים
                            if (user.isAdmin()) {
                                intent = new Intent(Login.this, Adminpage.class);
                            } else {
                                intent = new Intent(Login.this, userpage.class);
                            }

                            // מעבירים את שם המשתמש למסך הבא כדי להגיד לו "שלום [שם]"
                            intent.putExtra("USER_NAME", user.getfName());

                            // שוב, מנקים את היסטוריית המסכים כדי שבלחיצה על "חזור" המשתמש לא יחזור לעמוד ההתחברות
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