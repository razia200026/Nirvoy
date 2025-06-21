package com.example.safetyapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safetyapp.R;
import com.example.safetyapp.service.EmergencyService;

import java.util.List;

public class EmergencyServiceAdapter extends RecyclerView.Adapter<EmergencyServiceAdapter.ViewHolder> {

    public interface OnCallClickListener {
        void onCallClick(EmergencyService service);
    }

    private List<EmergencyService> services;
    private OnCallClickListener listener;

    public EmergencyServiceAdapter(List<EmergencyService> services, OnCallClickListener listener) {
        this.services = services;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name, phone;
        Button btnCall;

        public ViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.iv_service_icon);
            name = view.findViewById(R.id.tv_service_name);
            phone = view.findViewById(R.id.tv_service_phone);
            btnCall = view.findViewById(R.id.btn_call_service);
        }
    }

    @NonNull
    @Override
    public EmergencyServiceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmergencyServiceAdapter.ViewHolder holder, int position) {
        EmergencyService service = services.get(position);
        holder.name.setText(service.getName());
        holder.phone.setText(service.getPhone());
        holder.icon.setImageResource(service.getIconResId());

        holder.btnCall.setOnClickListener(v -> listener.onCallClick(service));
    }

    @Override
    public int getItemCount() {
        return services.size();
    }
}
