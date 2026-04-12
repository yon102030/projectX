package com.example.projectx.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TopColorsAdapter extends RecyclerView.Adapter<TopColorsAdapter.Holder> {

    private List<String> colors;

    public TopColorsAdapter(List<String> colors) {
        this.colors = colors;
    }

    static class Holder extends RecyclerView.ViewHolder {
        Button btn;

        public Holder(View v) {
            super(v);
            btn = (Button) v;
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Button b = new Button(parent.getContext());
        b.setAllCaps(false);
        b.setEnabled(false);
        return new Holder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        h.btn.setText(colors.get(position));
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }
}