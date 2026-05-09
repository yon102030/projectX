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

public class OutfitAdapter extends RecyclerView.Adapter<OutfitAdapter.ViewHolder> {

    private List<Outfit> list;

    public OutfitAdapter(List<Outfit> list) {
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

        // ================= IMAGES =================
        h.imgTop.setImageBitmap(safeDecode(o.getTop()));
        h.imgBottom.setImageBitmap(safeDecode(o.getBottom()));

        if (o.getOuter() != null && !o.getOuter().isEmpty()) {
            h.imgOuter.setVisibility(View.VISIBLE);
            h.imgOuter.setImageBitmap(safeDecode(o.getOuter()));
        } else {
            h.imgOuter.setVisibility(View.GONE);
        }

        // ================= STYLE =================
        boolean isWinter = o.getOuter() != null && !o.getOuter().isEmpty();

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

        // ================= OPEN DETAILS (FIXED) =================
        View.OnClickListener open = v -> {
            Intent intent = new Intent(v.getContext(), OutfitDetailsActivity.class);
            intent.putExtra("outfitId", o.getOutfitId()); // 🔥 רק ID
            v.getContext().startActivity(intent);
        };

        h.itemView.setOnClickListener(open);
        h.cardRoot.setOnClickListener(open);

        // ================= DELETE =================
        h.btnDelete.setOnClickListener(v -> {

            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            String id = list.get(pos).getOutfitId();

            DatabaseService.getInstance().deleteOutfit(id,
                    new DatabaseService.DatabaseCallback<Void>() {
                        @Override
                        public void onCompleted(Void object) {
                            list.remove(pos);
                            notifyItemRemoved(pos);
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