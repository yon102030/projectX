package com.example.projectx.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectx.R;
import com.example.projectx.model.Clothe;
import com.example.projectx.util.ImageUtil;

import java.util.List;

// מחלקה זו משמשת כמתאם (Adapter) בין רשימת הבגדים בזיכרון לבין התצוגה שלהם על המסך
public class ClotheAdapter extends RecyclerView.Adapter<ClotheAdapter.ClotheViewHolder> {

    // ממשק חכם המגדיר 3 פעולות שונות שאפשר לעשות על כל פריט,
    // ומאפשר למסך הראשי (itemlist) להחליט מה יקרה בכל לחיצה.
    public interface OnClotheClickListener {
        void onClotheClick(Clothe clothe);       // לחיצה רגילה (למשל: הצגת הבגד העליון)
        void onLongClotheClick(Clothe clothe);   // לחיצה ארוכה (למשל: הצגת הבגד התחתון)
        void onDeleteClothe(Clothe clothe);      // לחיצה על כפתור המחיקה
    }

    private List<Clothe> clotheList;
    private final OnClotheClickListener listener;

    public ClotheAdapter(List<Clothe> clotheList, OnClotheClickListener listener) {
        this.clotheList = clotheList;
        this.listener = listener;
    }

    // יצירת שורה חדשה וריקה מתוך העיצוב שהגדרנו בקובץ ה-XML
    @NonNull
    @Override
    public ClotheViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item_clothe, parent, false);

        return new ClotheViewHolder(view);
    }

    // הפונקציה שלוקחת את הנתונים של בגד ספציפי ו"שותלת" אותם בתוך השורה שלו
    @Override
    public void onBindViewHolder(@NonNull ClotheViewHolder holder, int position) {

        Clothe clothe = clotheList.get(position);

        holder.textType.setText(clothe.getType());
        holder.textColor.setText(clothe.getColor());
        holder.textSeason.setText(clothe.getSeason());

        // 🔥 תרגום הלוגיקה שבנינו בעבר: המשתנה isFavorite משמש אותנו לייצוג המגדר.
        // אם הוא true - מדובר בבגד של גבר, אחרת - בגד של אישה.
        if (clothe.isFavorite()) {
            holder.textGender.setText("מגדר: גבר");
        } else {
            holder.textGender.setText("מגדר: אישה");
        }

        // המרת התמונה מפורמט טקסטואלי ארוך (Base64) ששמור בפיירבייס, לתמונה אמתית (Bitmap) שמוצגת על המסך
        holder.imageClothe.setImageBitmap(
                ImageUtil.convertFrom64base(clothe.getImageUrl())
        );

        // 🔥 הגדרת פעולה ללחיצה רגילה על השורה
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClotheClick(clothe);
            }
        });

        // 🔥 הגדרת פעולה ללחיצה ארוכה על השורה
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onLongClotheClick(clothe);
            }
            // מחזירים true כדי לאותת למערכת שסיימנו לטפל בלחיצה הארוכה, ושלא תפעיל גם את הלחיצה הרגילה בטעות.
            return true;
        });

        // 🔥 הגדרת לחיצה על כפתור פח האשפה (מחיקה)
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClothe(clothe);
            }
        });
    }

    // מחזירה כמה פריטים סך הכל יש לנו ברשימה
    @Override
    public int getItemCount() {
        return clotheList.size();
    }

    // מחלקה פנימית ש"מחזיקה" את כל הרכיבים של השורה כדי שהאפליקציה לא תצטרך לחפש אותם
    // בזיכרון מחדש בכל פעם שהמשתמש גולל את המסך (משפר ביצועים מאוד!)
    public static class ClotheViewHolder extends RecyclerView.ViewHolder {

        ImageView imageClothe;
        TextView textType, textColor, textSeason, textGender;
        Button btnDelete;

        public ClotheViewHolder(@NonNull View itemView) {
            super(itemView);

            imageClothe = itemView.findViewById(R.id.image_clothe);
            textType = itemView.findViewById(R.id.text_type);
            textColor = itemView.findViewById(R.id.text_color);
            textSeason = itemView.findViewById(R.id.text_season);
            textGender = itemView.findViewById(R.id.text_gender);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}