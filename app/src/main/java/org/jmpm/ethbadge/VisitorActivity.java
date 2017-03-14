/*
 * Copyright (C) 2014 The Android Open Source Project
 * Modifications copyright (C) 2017 github.com/js0p/EthBadge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jmpm.ethbadge;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class VisitorActivity extends AppCompatActivity {

    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private BluetoothChatService mChatService = null;
    private TextView textViewStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor);

        textViewStatus = (TextView) findViewById(R.id.textViewStatus);

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(); //this, mHandler);

        // Find and set up the ListView for newly discovered devices
        ListView deviceList = (ListView) findViewById(R.id.deviceList);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        deviceList.setAdapter(mNewDevicesArrayAdapter);
        deviceList.setOnItemClickListener(mDeviceClickListener);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);


        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
        textViewStatus.setText("Tap device to show badge");
        Toast.makeText(getApplicationContext(), "Searching for devices around...", Toast.LENGTH_LONG).show();
    }

    public void buttonBack(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        // When discovery finds a device
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            // Get the BluetoothDevice object from the Intent
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());// TODO: check if device has already been added (there are duplicates dometimes)
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            if (mNewDevicesArrayAdapter.getCount() == 0) {
                Toast.makeText(getApplicationContext(), "Could not find any device", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(getApplicationContext(), "These are all available devices", Toast.LENGTH_LONG).show(); // not needed anyways...
            }

        }
        }
    };

    /**
     * The on-click listener for all devices in the ListViews
     */
    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
            // Attempt to connect to the device
            mChatService.connect(device, false);

            Toast.makeText(getApplicationContext(), "Connected. Showing badge...", Toast.LENGTH_LONG).show();
        }
    };

}
