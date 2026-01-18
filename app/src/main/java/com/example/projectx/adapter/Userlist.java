package com.example.projectx.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.projectx.model.User;

import java.util.List;

public class Userlist extends ArrayAdapter<User> {

    public Userlist(@NonNull Context context, @NonNull List<User> users) {
        super(context, android.R.layout.simple_list_item_1, users);
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // משתמשים בעיצוב המובנה של אנדרואיד
        View view = super.getView(position, convertView, parent);

        // לוקחים את המשתמש הנוכחי
        User user = getItem(position);

        // משנים את הטקסט שמוצג בשורה
        if (user != null) {
            android.widget.TextView tv = view.findViewById(android.R.id.text1);
            tv.setText(
                    user.getfName() + " " +
                            user.getlName() + " - " +
                            user.getEmail()
            );
        }

        return view;
    }
}
