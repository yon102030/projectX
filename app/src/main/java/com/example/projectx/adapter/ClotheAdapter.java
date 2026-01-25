package com.example.projectx.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectx.R;
import com.example.projectx.model.Clothe;
import com.example.projectx.util.ImageUtil;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ClotheAdapter extends RecyclerView.Adapter<ClotheAdapter.ClotheViewHolder> {




    public interface OnClotheClickListener {
        void onClotheClick(Clothe user);
        void onLongClotheClick(Clothe user);
    }


    private List<Clothe> clotheList;
    private final OnClotheClickListener onClotcheClickListener;


    // קונסטרקטור עם Context ורשימת פריטים
    public ClotheAdapter( List<Clothe> clotheList, OnClotheClickListener onClotcheClickListener) {

        this.clotheList = clotheList;
        this.onClotcheClickListener = onClotcheClickListener;
    }

    @NonNull
    @Override
    public ClotheViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item_clothe, parent, false);
        return new ClotheViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClotheViewHolder holder, int position) {
        Clothe clothe = clotheList.get(position);

        holder.textType.setText(clothe.getType());
        holder.textColor.setText(clothe.getColor());
        holder.textSeason.setText(clothe.getSeason());

        // נקה קודם את ה-ImageView כדי למנוע טעינות ישנות בריבייסקלינג
        holder.imageClothe.setImageBitmap(ImageUtil.convertFrom64base(clothe.getImageUrl()));

        // הצגת תמונה מ-URL אם קיימת
     //   if (clothe.getImageUrl() != null && !clothe.getImageUrl().isEmpty()) {
     //       loadImageFromUrl(holder.imageClothe, clothe.getImageUrl());
    //   }

        // הצגת כוכב אם הפריט מועדף
      //  holder.imageFavorite.setVisibility(clothe.isFavorite() ? View.VISIBLE : View.GONE);


        holder.itemView.setOnClickListener(v -> {
            if (onClotcheClickListener != null) {
                onClotcheClickListener.onClotheClick(clothe);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if ( onClotcheClickListener!= null) {
                onClotcheClickListener.onLongClotheClick(clothe);
            }
            return true;
        });


    }

    @Override
    public int getItemCount() {
        return clotheList.size();
    }

    public static class ClotheViewHolder extends RecyclerView.ViewHolder {

        ImageView imageClothe, imageFavorite;
        TextView textType, textColor, textSeason;

        public ClotheViewHolder(@NonNull View itemView) {
            super(itemView);
            imageClothe = itemView.findViewById(R.id.image_clothe);
            imageFavorite = itemView.findViewById(R.id.image_favorite);
            textType = itemView.findViewById(R.id.text_type);
            textColor = itemView.findViewById(R.id.text_color);
            textSeason = itemView.findViewById(R.id.text_season);
        }
    }


    // עדכון רשימה
    public void updateList(List<Clothe> newList) {
        clotheList = newList;
        notifyDataSetChanged();
    }
}
