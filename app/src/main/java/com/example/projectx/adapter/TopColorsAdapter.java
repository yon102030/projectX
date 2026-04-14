package com.example.projectx.adapter;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TopColorsAdapter extends RecyclerView.Adapter<TopColorsAdapter.Holder> {

    private List<String> colors;

    public TopColorsAdapter(List<String> colors) {
        this.colors = colors;
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tv;

        public Holder(TextView v) {
            super(v);
            tv = v;
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        TextView tv = new TextView(parent.getContext());

        tv.setTextSize(15);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(27, 16, 27, 16);

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        params.setMargins(10, 6, 10, 6);
        tv.setLayoutParams(params);

        return new Holder(tv);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {

        String colorName = colors.get(position);
        h.tv.setText(colorName);

        int colorValue = getColorValue(colorName);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(colorValue);
        bg.setCornerRadius(50); // 🔥 עגול יפה

        bg.setStroke(3, 0xFF0D47A1);
        h.tv.setBackground(bg);

        int r = (colorValue >> 16) & 0xFF;
        int g = (colorValue >> 8) & 0xFF;
        int b = colorValue & 0xFF;

        double brightness = (0.299 * r + 0.587 * g + 0.114 * b);

        h.tv.setTextColor(brightness < 128 ? 0xFFFFFFFF : 0xFF000000);
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    private int getColorValue(String colorName) {

        switch (colorName) {
            case "שחור": return 0xFF000000;
            case "לבן": return 0xFFFFFFFF;
            case "אפור": return 0xFF808080;
            case "כחול": return 0xFF2196F3;
            case "כחול כהה": return 0xFF1565C0;
            case "אדום": return 0xFFF44336;
            case "ירוק": return 0xFF4CAF50;
            case "חום": return 0xFF795548;
            case "בז": return 0xFFEEE8AA;
            case "צהוב": return 0xFFFFEB3B;
            case "כתום": return 0xFFFF9800;
            case "סגול": return 0xFF9C27B0;
            case "ורוד": return 0xFFE91E63;
            case "טורקיז": return 0xFF00BCD4;
            case "זית": return 0xFF808000;
        }
        return 0xFF9E9E9E;
    }
}