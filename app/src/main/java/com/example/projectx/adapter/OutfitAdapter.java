package com.example.projectx.adapter;

import android.content.Context;
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

public class OutfitAdapter extends RecyclerView.Adapter<OutfitAdapter.ViewHolder> {

    private List<Outfit> list;
    private Context context;

    public OutfitAdapter(Context context, List<Outfit> list) {
        this.context = context;
        this.list = list;
    }

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
            cardRoot = v.findViewById(R.id.cardRoot);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_outfit, parent, false);

        return new ViewHolder(v);
    }

    private Bitmap safeDecode(String base64) {
        try {
            if (base64 == null || base64.isEmpty()) return null;
            return ImageUtil.convertFrom64base(base64);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int position) {

        Outfit o = list.get(position);

        // ================= DELETE =================
        h.btnDelete.setOnClickListener(v -> {

            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            Outfit outfitToDelete = list.get(pos);

            DatabaseService.getInstance().deleteOutfit(
                    outfitToDelete.getOutfitId(),
                    new DatabaseService.DatabaseCallback<Void>() {

                        @Override
                        public void onCompleted(Void result) {

                            list.remove(pos);
                            notifyItemRemoved(pos);

                            Toast.makeText(context, "נמחק בהצלחה", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Toast.makeText(context, "מחיקה נכשלה", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });

        // ================= IMAGES =================
        Bitmap top = safeDecode(o.getTop());
        Bitmap bottom = safeDecode(o.getBottom());

        if (top != null) h.imgTop.setImageBitmap(top);
        else h.imgTop.setImageResource(android.R.color.darker_gray);

        if (bottom != null) h.imgBottom.setImageBitmap(bottom);
        else h.imgBottom.setImageResource(android.R.color.darker_gray);

        // ================= OUTER =================
        boolean isWinter = o.getOuter() != null && !o.getOuter().isEmpty();

        if (isWinter) {
            Bitmap outer = safeDecode(o.getOuter());

            if (outer != null) {
                h.imgOuter.setVisibility(View.VISIBLE);
                h.imgOuter.setImageBitmap(outer);
            } else {
                h.imgOuter.setVisibility(View.GONE);
            }
        } else {
            h.imgOuter.setVisibility(View.GONE);
        }

        // ================= STYLE =================
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(30f);

        if (isWinter) {
            bg.setColor(0xFFBBDEFB);
            bg.setStroke(3, 0xFF64B5F6);
        } else {
            bg.setColor(0xFFFFF9C4);
            bg.setStroke(3, 0xFFFFEE58);
        }

        h.cardRoot.setBackground(bg);

        // ================= CLICK =================
        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OutfitDetailsActivity.class);
            intent.putExtra("top", o.getTop());
            intent.putExtra("outer", o.getOuter());
            intent.putExtra("bottom", o.getBottom());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}