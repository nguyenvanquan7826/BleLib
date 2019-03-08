package com.iot.nima.blelib;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class BleDeviceAdapter extends RecyclerView.Adapter<BleDeviceAdapter.ViewHolder> {
    private final String TAG = BleDeviceAdapter.class.getSimpleName();
    private Context context;
    private List<BluetoothDevice> deviceList;
    private OnItemRVClickListener onItemRVClickListener;

    public BleDeviceAdapter(Context context, List<BluetoothDevice> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
    }

    public BleDeviceAdapter setOnItemRVClickListener(OnItemRVClickListener onItemRVClickListener) {
        this.onItemRVClickListener = onItemRVClickListener;
        return this;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_device_ble, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int pos) {
        BluetoothDevice device = deviceList.get(pos);
        String name = device.getName();
        if (TextUtils.isEmpty(name)) name = context.getString(R.string.unknown_device);
        viewHolder.tvName.setText(name);
        viewHolder.tvAddress.setText(device.getAddress());
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);

            itemView.setOnClickListener(v -> {
                if (onItemRVClickListener != null) {
                    int pos = getAdapterPosition();
                    if (pos >= 0 && pos < deviceList.size()) {
                        onItemRVClickListener.onClick(pos);
                    } else {
                        Log.e(TAG, "Error posision:" + pos);
                    }
                }else {
                    Log.e(TAG, "Error Listener null");
                }
            });
        }
    }

    public void clear() {
        deviceList.clear();
    }

    public interface OnItemRVClickListener {
        void onClick(int pos);
    }
}
