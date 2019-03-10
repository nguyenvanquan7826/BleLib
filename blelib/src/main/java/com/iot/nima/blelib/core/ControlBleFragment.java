package com.iot.nima.blelib.core;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iot.nima.blelib.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ControlBleFragment extends Fragment {
    private final static String TAG = ControlBleFragment.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String deviceName;
    private String deviceAddress;

    private BluetoothLeService bluetoothLeService;
    private boolean isConnected = false;

    private TextView tvResult;
    private int idButtons[] = {R.id.btnKey, R.id.btnClose, R.id.btnGet, R.id.btnLock, R.id.btnOpen, R.id.btnStop, R.id.btnUnLock};

    private View rootView;

    public ControlBleFragment() {
    }

    public static ControlBleFragment newInstance(String deviceName, String deviceAddress) {
        ControlBleFragment fragment = new ControlBleFragment();
        Bundle args = new Bundle();
        args.putString(EXTRAS_DEVICE_NAME, deviceName);
        args.putString(EXTRAS_DEVICE_ADDRESS, deviceAddress);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deviceName = getArguments().getString(EXTRAS_DEVICE_NAME);
            deviceAddress = getArguments().getString(EXTRAS_DEVICE_ADDRESS);
        }
        setHasOptionsMenu(true);
        Intent gattServiceIntent = new Intent(getContext(), BluetoothLeService.class);
        Objects.requireNonNull(getActivity()).bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        getActivity().setTitle(deviceName);
        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setSubtitle(deviceAddress);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.control_ble_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rootView = view;

        tvResult = view.findViewById(R.id.tvResult);
        for (int id : idButtons) {
            view.findViewById(id).setOnClickListener(clickBtnCmd);
        }
        enableControl(false);
    }

    private View.OnClickListener clickBtnCmd = view -> {
        if (view.getId() == R.id.btnKey) sendCmd("{\"key\":\"0123456\"}");
        if (view.getId() == R.id.btnOpen) sendCmd("{\"rq\":\"open\"}");
        if (view.getId() == R.id.btnClose) sendCmd("{\"rq\":\"close\"}");
        if (view.getId() == R.id.btnStop) sendCmd("{\"rq\":\"stop\"}");
        if (view.getId() == R.id.btnLock) sendCmd("{\"rq\":\"lock\"}");
        if (view.getId() == R.id.btnUnLock) sendCmd("{\"rq\":\"unlock\"}");
        if (view.getId() == R.id.btnGet) sendCmd("{\"rq\":\"get\"}");
    };

    private void sendCmd(String cmd) {
        if (bluetoothLeService != null) {
            bluetoothLeService.writeCustomCharacteristic(
                    LedButtonGattAttributes.SERVICE_UUID,
                    LedButtonGattAttributes.WRITE_UUID,
                    cmd);
        } else {
            Log.e(TAG, "sendCmd fail, bluetoothLeService null");
        }
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!bluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
//                finish();
                return;
            }
            // Automatically connects to the device upon successful start-up initialization.
            connectDeviceWithKey();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.e(TAG, "action:" + action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                isConnected = true;
                updateConnectionState(R.string.connected);
                updateMenu();
                enableControl(false);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                isConnected = false;
                updateConnectionState(R.string.disconnected);
                updateMenu();
                enableControl(false);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                enableControl(true);
                sendKey();
                displayGattServices(bluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                processBleData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA_FROM_DEVICE));
            }
        }
    };

    private void updateConnectionState(final int resourceId) {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
//                mConnectionState.setText(resourceId);
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.control_device_menu, menu);

        menu.findItem(R.id.menuConnect).setVisible(!isConnected);
        menu.findItem(R.id.menuDisconnect).setVisible(isConnected);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuConnect) connectDeviceWithKey();
        if (item.getItemId() == R.id.menuDisconnect) disconnectDevice();
        return super.onOptionsItemSelected(item);
    }

    private void updateMenu() {
        Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
    }

    private void disconnectDevice() {
        bluetoothLeService.disconnect();
    }

    private void sendKey() {
        sendCmd("{\"key\":\"0123456\"}");
    }

    private void connectDeviceWithKey() {
        bluetoothLeService.connect(deviceAddress);
    }

    private void enableControl(boolean enable) {
        for (int id : idButtons) {
            rootView.findViewById(id).setEnabled(enable);
        }

        if (enable) {
            if (bluetoothLeService != null) {
                bluetoothLeService.notifyCustomCharacteristic(LedButtonGattAttributes.SERVICE_UUID,
                        LedButtonGattAttributes.NOTIFY_UUID, true);
            }
        } else {
            tvResult.setText("");
        }
    }

    /*
     *  @brief      Thủ tục xử lý dữ liệu nhận được từ BLE Device (VBLUno51)
     */
    public void processBleData(String data) {
        tvResult.setText(String.format("Value: %s", data));
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getActivity()).registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bluetoothLeService != null) {
            connectDeviceWithKey();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Objects.requireNonNull(getActivity()).unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(getActivity()).unbindService(serviceConnection);
        bluetoothLeService = null;
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        String LIST_NAME = "NAME";
        String LIST_UUID = "UUID";

        String uuid;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<>();
        ArrayList<ArrayList<BluetoothGattCharacteristic>> gattCharacteristics = new ArrayList<>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, LedButtonGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            Log.e(TAG, "Name:" + LedButtonGattAttributes.lookup(uuid, unknownServiceString));
            Log.e(TAG, "UUID:" + uuid);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<>();
            List<BluetoothGattCharacteristic> gattCharacteristicList = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristicList) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, LedButtonGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
                Log.e(TAG, "--Name:" + LedButtonGattAttributes.lookup(uuid, unknownServiceString));
                Log.e(TAG, "--UUID:" + uuid);
            }
            gattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }
}
