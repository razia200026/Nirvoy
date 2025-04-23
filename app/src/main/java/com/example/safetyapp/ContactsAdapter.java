package com.example.safetyapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Random;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {
    private List<Contact> contacts;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDelete(String contactId);
    }

    public ContactsAdapter(List<Contact> contacts, OnDeleteClickListener listener) {
        this.contacts = contacts;
        this.onDeleteClickListener = listener;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.name.setText(contact.getName());
        holder.phone.setText(contact.getPhone());

        holder.profileIcon.setText(contact.getName().substring(0, 1).toUpperCase());
        holder.profileIcon.setBackgroundColor(getRandomColor());

        holder.btnDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog(v.getContext(), contact.getId());
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView name, phone, profileIcon;
        ImageView btnDelete;

        ContactViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_contact_name);
            phone = itemView.findViewById(R.id.tv_contact_phone);
            profileIcon = itemView.findViewById(R.id.tv_profile_icon);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    private void showDeleteConfirmationDialog(Context context, String contactId) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete this contact?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    onDeleteClickListener.onDelete(contactId);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private int getRandomColor() {
        int[] colors = {
                Color.parseColor("#D32F2F"), // Dark Red
                Color.parseColor("#7B1FA2"), // Dark Purple
                Color.parseColor("#303F9F"), // Dark Indigo
                Color.parseColor("#0288D1"), // Dark Cyan Blue
                Color.parseColor("#00796B"), // Dark Teal
                Color.parseColor("#388E3C"), // Dark Green
                Color.parseColor("#FBC02D"), // Dark Yellow
                Color.parseColor("#E64A19")  // Dark Orange
        };
        return colors[new Random().nextInt(colors.length)];
    }
}
