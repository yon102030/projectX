package com.example.projectx;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

// מסך טעינה (Splash Screen) שמוצג בעת פתיחת האפליקציה.
// המסך מציג מצגת תמונות מתחלפת (אנימציה) ולאחר מספר שניות מעביר את המשתמש למסך ההתחברות.
public class splash extends AppCompatActivity {

    private ImageView logo;

    // ה-Handler נועד כדי לתזמן פעולות מושהות (כמו טיימר).
    // שימוש ב-Looper.getMainLooper() מבטיח שהשינויים יקרו על השרשור הראשי כדי שנוכל לעדכן את התצוגה (UI).
    private final Handler handler = new Handler(Looper.getMainLooper());

    // מערך שמכיל את התמונות שיתחלפו במהלך הטעינה (מתוך תיקיית ה-drawable)
    private final int[] images = {
            R.drawable.blacktshirt,
            R.drawable.blackzarajacket,
            R.drawable.bluejeans,
            R.drawable.brownzarajeans,
            R.drawable.grayzarajeans
    };

    private int index = 0; // שומר את המיקום של התמונה הנוכחית במערך

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logo = findViewById(R.id.logo);

        // קביעת התמונה הראשונה שתוצג עם פתיחת המסך ווידוא שהיא בולטת (אטימות 100%)
        logo.setImageResource(images[0]);
        logo.setAlpha(1f);

        // הפעלת האנימציה: אומר ל-Handler להפעיל את הפעולה changeImage בעוד שנייה אחת (1000 מילי-שניות)
        handler.postDelayed(changeImage, 1000);

        // תזמון המעבר: בעוד 4 שניות (4000 מילי-שניות), נעבור למסך הבא ונסגור את מסך הטעינה
        handler.postDelayed(() -> {
            startActivity(new Intent(splash.this, MainActivity.class));
            finish(); // חשוב: סוגר את מסך הטעינה כדי שהמשתמש לא יוכל לחזור אליו עם כפתור החזור (Back)
        }, 4000);
    }

    // משימה (Runnable) שרצה שוב ושוב במרווחי זמן קבועים כדי להחליף תמונות
    private final Runnable changeImage = new Runnable() {
        @Override
        public void run() {

            // קידום המונה ב-1. השימוש במודולו (%) מבטיח שכאשר נגיע לסוף המערך, נחזור להתחלה (אינדקס 0)
            index = (index + 1) % images.length;

            // התחלת שרשרת האנימציות על ה-ImageView
            logo.animate()
                    .alpha(0f) // אנימציית העלמה (Fade Out) - יורד לאטימות 0
                    .setDuration(250) // זמן ההעלמה: רבע שנייה
                    .withEndAction(() -> {
                        // הפעולה הזו רצה בדיוק כשאנימציית ההעלמה מסתיימת (כשהתמונה לגמרי שקופה)

                        // מחליפים את התמונה הבלתי-נראית לתמונה הבאה בתור
                        logo.setImageResource(images[index]);

                        // מיד לאחר מכן, מתחילים אנימציית חזרה (Fade In)
                        logo.animate()
                                .alpha(1f) // עולה חזרה לאטימות 100%
                                .setDuration(250) // זמן ההופעה: רבע שנייה
                                .start();
                    })
                    .start();

            // המשימה מזמנת את עצמה שוב בעוד 900 מילי-שניות (לולאה אינסופית כל עוד המסך פתוח)
            handler.postDelayed(this, 900);
        }
    };

    // פעולה זו מופעלת רגע לפני שהמסך נסגר (נהרס על ידי מערכת ההפעלה)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // פעולה קריטית למניעת זליגת זיכרון (Memory Leak):
        // מנקה את כל המשימות (Runnable) שה-Handler תזמן בעתיד, כדי שלא ינסו לרוץ אחרי שהמסך כבר לא קיים.
        handler.removeCallbacksAndMessages(null);
    }
}