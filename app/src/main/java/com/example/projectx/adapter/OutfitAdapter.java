package com.example.projectx.adapter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.projectx.OutfitDetailsActivity;
import com.example.projectx.R;
import com.example.projectx.model.Outfit;
import com.example.projectx.services.DatabaseService;
import com.example.projectx.util.ImageUtil;

import java.util.List;

// מחלקה זו אחראית לקחת את הרשימה של הלוקים השמורים ולצייר אותם בתור "כרטיסיות" על המסך
public class OutfitAdapter extends RecyclerView.Adapter<OutfitAdapter.ViewHolder> {

    private List<Outfit> list;

    public OutfitAdapter(List<Outfit> list) {
        this.list = list;
    }

    // מחזיק את הרכיבים של כל כרטיסיית אאוטפיט כדי שלא נצטרך לחפש אותם כל פעם מחדש
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgTop, imgOuter, imgBottom;
        LinearLayout cardRoot;
        Button btnDelete;

        public ViewHolder(View v) {
            super(v);

            imgTop = v.findViewById(R.id.imgTop);
            imgOuter = v.findViewById(R.id.imgOuter);
            imgBottom = v.findViewById(R.id.imgBottom);
            btnDelete = v.findViewById(R.id.btnDelete);
            cardRoot = v.findViewById(R.id.cardRoot); // הרקע של כל הכרטיסייה
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_outfit, parent, false);
        return new ViewHolder(v);
    }

    // פונקציית הגנה קטנה וחכמה: מונעת קריסה של האפליקציה במקרה שמשום מה התמונה חסרה או פגומה
    private Bitmap safeDecode(String base64) {
        try {
            if (base64 == null || base64.isEmpty()) return null;
            return ImageUtil.convertFrom64base(base64);
        } catch (Exception e) {
            return null; // מחזיר "כלום" במקום להקריס
        }
    }

    // חיבור הנתונים של הלוק הספציפי לכרטיסייה שלו במסך
    @Override
    public void onBindViewHolder(ViewHolder h, int position) {

        Outfit o = list.get(position);

        // ================= IMAGES (תמונות) =================
        // ממירים את קוד ה-Base64 של התמונות חזרה לתמונה אמיתית (Bitmap)
        h.imgTop.setImageBitmap(safeDecode(o.getTop()));
        h.imgBottom.setImageBitmap(safeDecode(o.getBottom()));

        // בדיקה: האם בלוק הזה יש בכלל פריט עליון (מעיל)?
        if (o.getOuter() != null && !o.getOuter().isEmpty()) {
            h.imgOuter.setVisibility(View.VISIBLE); // מציגים את תיבת המעיל
            h.imgOuter.setImageBitmap(safeDecode(o.getOuter()));
        } else {
            h.imgOuter.setVisibility(View.GONE); // מסתירים לגמרי את התיבה כדי שלא תתפוס מקום ריק
        }

        // ================= STYLE (עיצוב דינמי) =================
        // אם יש פריט עליון, אנחנו מחשיבים את זה כלוק של חורף
        boolean isWinter = o.getOuter() != null && !o.getOuter().isEmpty();

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(30f); // פינות עגולות לכרטיסייה

        // צובעים את רקע הכרטיסייה בהתאם לעונה:
        if (isWinter) {
            bg.setColor(0xFFBBDEFB); // תכלת לחורף
            bg.setStroke(3, 0xFF64B5F6);
        } else {
            bg.setColor(0xFFFFF9C4); // צהבהב לקיץ
            bg.setStroke(3, 0xFFFFEE58);
        }

        h.cardRoot.setBackground(bg); // מחילים את הרקע המעוצב על הכרטיסייה

        // ================= OPEN DETAILS (פתיחת פרטים) =================
        // מה קורה כשלוחצים על הכרטיסייה כדי לראות את הלוק בגדול
        View.OnClickListener open = v -> {
            Intent intent = new Intent(v.getContext(), OutfitDetailsActivity.class);

            // 🔥 פתרון מעולה למניעת קריסות:
            // אנחנו לא מעבירים את התמונות הענקיות למסך הבא, אלא רק את "תעודת הזהות" (ID) של הלוק!
            // המסך הבא כבר יידע לשלוף את התמונות בעצמו מפיירבייס.
            intent.putExtra("outfitId", o.getOutfitId());
            v.getContext().startActivity(intent);
        };

        // מגדירים את הלחיצה גם על הכרטיסייה עצמה וגם על הרווחים שבה
        h.itemView.setOnClickListener(open);
        h.cardRoot.setOnClickListener(open);

        // ================= DELETE (מחיקה) =================
        h.btnDelete.setOnClickListener(v -> {

            // מוודאים שאנחנו מוחקים את הלוק הנכון לפי המיקום העדכני שלו
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            String id = list.get(pos).getOutfitId();

            // פונים לשרת למחוק את הלוק
            DatabaseService.getInstance().deleteOutfit(id,
                    new DatabaseService.DatabaseCallback<Void>() {
                        @Override
                        public void onCompleted(Void object) {
                            // אם המחיקה בשרת עבדה - מוחקים מהרשימה המקומית ומעלימים מהמסך
                            list.remove(pos);
                            notifyItemRemoved(pos); // עושה אנימציית העלמה יפה
                            Toast.makeText(v.getContext(), "נמחק", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Toast.makeText(v.getContext(), "שגיאה במחיקה", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}