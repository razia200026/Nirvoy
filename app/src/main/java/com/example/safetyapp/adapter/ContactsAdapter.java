package com.example.safetyapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safetyapp.Contact;
import com.example.safetyapp.R;

import java.util.List;
import java.util.Random;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {
    private List<Contact> contacts;
    private OnDeleteClickListener onDeleteClickListener;
    private OnEditClickListener onEditClickListener;

    public interface OnDeleteClickListener {
        void onDelete(String contactId);
    }

    public interface OnEditClickListener {
        void onEdit(Contact contact);
    }

    public ContactsAdapter(List<Contact> contacts, OnDeleteClickListener deleteListener, OnEditClickListener editListener) {
        this.contacts = contacts;
        this.onDeleteClickListener = deleteListener;
        this.onEditClickListener = editListener;
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

        holder.btnDelete.setOnClickListener(v ->
                showDeleteConfirmationDialog(v.getContext(), contact.getId()));

        holder.btnEdit.setOnClickListener(v -> onEditClickListener.onEdit(contact)); // New
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView name, phone, profileIcon;
        ImageView btnDelete, btnEdit;

        ContactViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_contact_name);
            phone = itemView.findViewById(R.id.tv_contact_phone);
            profileIcon = itemView.findViewById(R.id.tv_profile_icon);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnEdit = itemView.findViewById(R.id.btn_edit); // New
        }
    }

    private void showDeleteConfirmationDialog(Context context, String contactId) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete this contact?")
                .setPositiveButton("Delete", (dialog, which) -> onDeleteClickListener.onDelete(contactId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private int getRandomColor() {
        int[] colors = {
                Color.parseColor("#D32F2F"), Color.parseColor("#7B1FA2"),
                Color.parseColor("#303F9F"), Color.parseColor("#0288D1"),
                Color.parseColor("#00796B"), Color.parseColor("#388E3C"),
                Color.parseColor("#FBC02D"), Color.parseColor("#E64A19")
        };
        return colors[new Random().nextInt(colors.length)];
    }
}
