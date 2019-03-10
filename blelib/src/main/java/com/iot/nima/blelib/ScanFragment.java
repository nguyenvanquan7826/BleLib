package com.iot.nima.blelib;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.iot.nima.blelib.core.ControlBleFragment;
import com.iot.nima.blelib.core.ScanBleFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScanFragment extends ScanBleFragment {

    private BleDeviceAdapter bleListAdapter;
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private Context context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(com.iot.nima.blelib.R.layout.scan_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvDevice = view.findViewById(com.iot.nima.blelib.R.id.rvDevice);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        rvDevice.setLayoutManager(layoutManager);
        bleListAdapter = new BleDeviceAdapter(context, deviceList).setOnItemRVClickListener(this::showControlDevice);
        rvDevice.setAdapter(bleListAdapter);

        // begin scan device
        scanDevice(true);
    }

    private void showControlDevice(int pos) {
        Intent i = new Intent(context, ControlBleActivity.class);
        i.putExtra(ControlBleFragment.EXTRAS_DEVICE_NAME, deviceList.get(pos).getName());
        i.putExtra(ControlBleFragment.EXTRAS_DEVICE_ADDRESS, deviceList.get(pos).getAddress());
        startActivity(i);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.scan_menu, menu);

        menu.findItem(R.id.menuScan).setVisible(!isScanning());
        menu.findItem(R.id.menuStop).setVisible(isScanning());
        menu.findItem(R.id.menuProgress).setVisible(isScanning());
        menu.findItem(R.id.menuProgress).setActionView(R.layout.layout_menu_progress);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuScan) scanDevice(true);
        if (item.getItemId() == R.id.menuStop) scanDevice(false);
        return super.onOptionsItemSelected(item);
    }

    protected void updateScanUI() {
        Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
    }

    @Override
    protected void onFindDevice(BluetoothDevice device, int rssi) {
        if (!deviceList.contains(device)) {
            deviceList.add(device);
            bleListAdapter.notifyDataSetChanged();
        }
    }
}
