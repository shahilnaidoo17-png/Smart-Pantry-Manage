package com.example.smartpantry.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.data.PantryItem;
import com.example.smartpantry.logic.DateUtils;

import java.util.ArrayList;
import java.util.List;

/** Custom RecyclerView adapter that binds PantryItem rows from the database to cards. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.Holder> {

    public interface Listener {
        void onClick(PantryItem item);      // tap  -> edit
        void onLongClick(PantryItem item);  // hold -> delete
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final Listener listener;

    public PantryAdapter(Listener listener) { this.listener = listener; }

    public void setItems(List<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        PantryItem item = items.get(position);
        h.name.setText(item.name);
        h.qty.setText(trim(item.quantity) + " " + item.unit);

        Integer days = DateUtils.daysUntil(item.expiryDate);
        if (days == null) {
            h.expiry.setText("No expiry date");
            h.expiry.setTextColor(Color.GRAY);
        } else if (days < 0) {
            h.expiry.setText("Expired " + item.expiryDate);
            h.expiry.setTextColor(Color.RED);
        } else if (days <= 3) {
            h.expiry.setText("Expires soon: " + item.expiryDate);
            h.expiry.setTextColor(Color.parseColor("#E65100"));
        } else {
            h.expiry.setText("Expires " + item.expiryDate);
            h.expiry.setTextColor(Color.GRAY);
        }
        h.itemView.setOnClickListener(v -> listener.onClick(item));
        h.itemView.setOnLongClickListener(v -> { listener.onLongClick(item); return true; });
    }

    @Override public int getItemCount() { return items.size(); }

    static String trim(double d) {
        return d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name, qty, expiry;
        Holder(View v) {
            super(v);
            name = v.findViewById(R.id.tvName);
            qty = v.findViewById(R.id.tvQty);
            expiry = v.findViewById(R.id.tvExpiry);
        }
    }
}
