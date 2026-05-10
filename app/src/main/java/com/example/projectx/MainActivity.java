package com.example.projectx;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;

// מסך הפתיחה הראשי של האפליקציה. המחלקה משתמשת ב-OnClickListener כדי לטפל בלחיצות על כל הכפתורים במסך ממקום אחד.
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // הצהרה על הכפתורים שיופיעו במסך
    Button signup, newacc, odot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // מאפשר עיצוב על מסך מלא (מקצה לקצה)
        setContentView(R.layout.activity_main); // חיבור קובץ העיצוב (XML) לקוד

        // קישור בין משתני ה-Java שיצרנו למעלה לבין הרכיבים האמיתיים במסך (לפי ה-ID שלהם ב-XML)
        signup = findViewById(R.id.signup);
        newacc = findViewById(R.id.newacc);
        odot = findViewById(R.id.odot);

        // הגדרת "מאזינים" (Listeners) לכל כפתור.
        // המילה 'this' אומרת לאנדרואיד: "כאשר לוחצים על הכפתור, חפש את הפונקציה onClick בתוך המחלקה הזו ממש".
        signup.setOnClickListener(this);
        newacc.setOnClickListener(this);
        odot.setOnClickListener(this);
    }

    // פונקציה מרכזית אחת שמרכזת את כל הלחיצות במסך הזה
    @Override
    public void onClick(View v) {
        // שולפים את ה-ID של הרכיב הספציפי שעליו המשתמש לחץ עכשיו
        int id = v.getId();

        // בודקים איזה כפתור נלחץ, ומשתמשים ב-Intent ("כוונה") כדי לעבור למסך המתאים:

        if (id == R.id.signup) {  // כפתור התחברות (למשתמשים קיימים)
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent); // פותח את מסך ההתחברות בלי לסגור את מסך הפתיחה שמאחוריו

        } else if (id == R.id.newacc) {  // כפתור הרשמה (למשתמשים חדשים)
            Intent intent = new Intent(MainActivity.this, register.class);
            startActivity(intent);

        } else if (id == R.id.odot) {  // כפתור אודות (מידע על האפליקציה)
            Intent intent = new Intent(MainActivity.this, Odotp.class);
            startActivity(intent);
        }
    }
}