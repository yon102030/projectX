package com.example.projectx.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.projectx.R;
import com.example.projectx.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<User> list;
    private OnUserDeleteListener listener;

    // 🔥 ממשק למחיקה
    public interface OnUserDeleteListener {
        void onDelete(User user, int position);
    }

    public UserAdapter(List<User> list, OnUserDeleteListener listener) {
        this.list = list;
        this.listener = listener;
    }

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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int position) {

        User u = list.get(position);

        h.tvName.setText(u.getfName() + " " + u.getlName());
        h.tvEmail.setText(u.getEmail());

        // 🔥 כפתור מחיקה
        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(u, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}