package com.iot.nima.blelib.core;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.iot.nima.blelib.R;

import java.util.Objects;

public class ScanBleFragment extends Fragment {
    private static final int REQUEST_ENABLE_BT = 43;
    private static final int REQUEST_CODE_LOCAL = 42;
    private static final long SCAN_PERIOD = 10 * 1000;

    private BluetoothAdapter bluetoothAdapter;
    private Handler mHandler;
    private boolean isScanning = false;

    private Context context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        context = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        checkPermission();
    }

    private void checkPermission() {
        // Use this check to determine whether BLE is supported on the device.
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            return;
        }

        // Prompt for permissions
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("BleActivity", "Location access not granted!");
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCAL);
        }

        // Initializes a Bluetooth bleListAdapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (bluetoothAdapter == null) {
            Toast.makeText(context, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            return;
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback bleScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            onFindDevice(device, rssi);
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        scanDevice(false);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!bluetoothAdapter.isEnabled()) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    // call after check permission in child Fragment or when click menu scan
    protected void scanDevice(boolean isScan) {
        if (isScan) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(() -> {
                if (isScanning) {
                    isScanning = false;
                    bluetoothAdapter.stopLeScan(bleScanCallback);
                    updateScanUI();
                } else {
                    Log.i("ScanBleFragment", "scan stopped");
                }
            }, SCAN_PERIOD);

            isScanning = true;
            bluetoothAdapter.startLeScan(bleScanCallback);
        } else {
            isScanning = false;
            bluetoothAdapter.stopLeScan(bleScanCallback);
        }
        updateScanUI();
    }

    protected boolean isScanning() {
        return isScanning;
    }

    protected void updateScanUI() {
    }

    protected void onFindDevice(BluetoothDevice device, int rssi) {
    }
}
