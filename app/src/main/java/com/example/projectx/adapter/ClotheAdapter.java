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

public class ClotheAdapter extends RecyclerView.Adapter<ClotheAdapter.ClotheViewHolder> {

    public interface OnClotheClickListener {
        void onClotheClick(Clothe clothe);
        void onLongClotheClick(Clothe clothe);
        void onDeleteClothe(Clothe clothe);
    }

    private List<Clothe> clotheList;
    private final OnClotheClickListener listener;

    public ClotheAdapter(List<Clothe> clotheList, OnClotheClickListener listener) {
        this.clotheList = clotheList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClotheViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item_clothe, parent, false);

        return new ClotheViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClotheViewHolder holder, int position) {

        Clothe clothe = clotheList.get(position);

        holder.textType.setText(clothe.getType());
        holder.textColor.setText(clothe.getColor());
        holder.textSeason.setText(clothe.getSeason());

        // 🔥 הגדרת טקסט המגדר לפי הערך של isFavorite
        if (clothe.isFavorite()) {
            holder.textGender.setText("מגדר: גבר");
        } else {
            holder.textGender.setText("מגדר: אישה");
        }

        holder.imageClothe.setImageBitmap(
                ImageUtil.convertFrom64base(clothe.getImageUrl())
        );

        // 🔥 לחיצה רגילה
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClotheClick(clothe);
            }
        });

        // 🔥 לחיצה ארוכה
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onLongClotheClick(clothe);
            }
            return true;
        });

        // 🔥 כפתור מחיקה
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClothe(clothe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return clotheList.size();
    }

    public static class ClotheViewHolder extends RecyclerView.ViewHolder {

        ImageView imageClothe;
        // הוספנו את textGender להצהרת המשתנים
        TextView textType, textColor, textSeason, textGender;
        Button btnDelete;

        public ClotheViewHolder(@NonNull View itemView) {
            super(itemView);

            imageClothe = itemView.findViewById(R.id.image_clothe);
            textType = itemView.findViewById(R.id.text_type);
            textColor = itemView.findViewById(R.id.text_color);
            textSeason = itemView.findViewById(R.id.text_season);

            // חיבור למזהה שיצרנו בקובץ ה-XML
            textGender = itemView.findViewById(R.id.text_gender);

            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}