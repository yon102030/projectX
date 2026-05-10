package com.example.projectx.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.projectx.R;
import com.example.projectx.admin_edit_user;
import com.example.projectx.model.User;

import java.util.List;

// מחלקה זו משמשת כ"מתאם" (Adapter) בין רשימת המשתמשים בזיכרון לבין התצוגה שלהם על המסך (RecyclerView)
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<User> list;
    private OnUserDeleteListener listener;

    // 🔥 ממשק למחיקה: מאפשר לאדפטר "לצעוק" למסך הראשי (Userlist) מתי לחצו על מחיקה, כדי שהמסך יטפל במחיקה מהשרת
    public interface OnUserDeleteListener {
        void onDelete(User user, int position);
    }

    public UserAdapter(List<User> list, OnUserDeleteListener listener) {
        this.list = list;
        this.listener = listener;
    }

    // מחלקה פנימית ש"שומרת" את הרכיבים העיצוביים של שורה אחת.
    // זה משפר ביצועים כי אנדרואיד לא צריך לחפש את ה-ID בכל פעם שגוללים את המסך.
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvEmail;
        Button btnDelete;

        public ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvEmail = v.findViewById(R.id.tvEmail);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }

    // פונקציה זו רצה כשחסרות שורות על המסך, והיא פשוט "מנפחת" (Inflate) שורה חדשה וריקה מתוך קובץ ה-XML
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);

        return new ViewHolder(v);
    }

    // הפונקציה הכי חשובה באדפטר: לוקחת משתמש ספציפי מהרשימה ו"שותלת" את הנתונים שלו בתוך השורה
    @Override
    public void onBindViewHolder(ViewHolder h, int position) {

        User u = list.get(position); // שליפת המשתמש הנכון לפי המיקום שלו ברשימה

        // עדכון הטקסטים במסך (שם פרטי + משפחה, ואימייל)
        h.tvName.setText(u.getfName() + " " + u.getlName());
        h.tvEmail.setText(u.getEmail());

        // 🔥 הגדרת לחיצה על כפתור המחיקה: מפעיל את ה-Listener שהגדרנו למעלה כדי למחוק את המשתמש
        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(u, position);
            }
        });

        // 🔥 מעבר לעריכה: לחיצה על השורה עצמה (לא על כפתור המחיקה)
        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), admin_edit_user.class);

            // טריק חכם לחסוך קריאות לשרת: אנחנו "אורזים" את כל הפרטים של המשתמש שאנחנו כבר יודעים
            // ושולחים אותם למסך העריכה כדי שיוצגו ישר בתיבות הטקסט.
            intent.putExtra("userId", u.getUserId());
            intent.putExtra("fname", u.getfName());
            intent.putExtra("lname", u.getlName());
            intent.putExtra("email", u.getEmail());
            intent.putExtra("phone", u.getPhone());
            intent.putExtra("password", u.getPassword());

            // מעבר למסך העריכה
            v.getContext().startActivity(intent);
        });
    }

    // מחזירה לאנדרואיד כמה שורות סך הכל הוא צריך להציג (לפי גודל הרשימה שלנו)
    @Override
    public int getItemCount() {
        return list.size();
    }
}