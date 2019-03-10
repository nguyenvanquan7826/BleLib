package com.iot.nima.blelib;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.iot.nima.blelib.core.ControlBleFragment;

public class ControlBleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_ble);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        String deviceName = getIntent().getStringExtra(ControlBleFragment.EXTRAS_DEVICE_NAME);
        String deviceAddress = getIntent().getStringExtra(ControlBleFragment.EXTRAS_DEVICE_ADDRESS);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentMain, ControlBleFragment.newInstance(deviceName, deviceAddress))
                .commit();
    }

}
