/*
 * Copyright (C) 2017 github.com/js0p/EthBadge
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
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class HostActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService = null;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        textView = (TextView) findViewById(R.id.textView);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    class IdActivityTimer implements Runnable {
        private Handler timerHandler;
        private AppCompatActivity activity;
        private boolean wasConnected;

        public IdActivityTimer(Handler timerHandler, AppCompatActivity activity) {
            this.timerHandler = timerHandler;
            this.activity = activity;
            wasConnected = false;
        }

        @Override
        public void run() {
            if (GlobalAppState.getInstance().getItsPublicPrivateKeyPairIsValidated() ==  true) {
                Intent intent = new Intent(activity, IdActivity.class);
                startActivity(intent);
            } else if (GlobalAppState.getInstance().getPublicPrivateKeyPairValidationFailed() ==  true) {
                Toast.makeText(getApplicationContext(), "Key validation failed", Toast.LENGTH_LONG).show();
                finish();
            } else {
                if (!wasConnected && GlobalAppState.getInstance().isConnected()) {
                    wasConnected = true;
                    textView.setText("Verifying badge...");
                }
                timerHandler.postDelayed(this, 200);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        textView.setText("Waiting for badge...");
        GlobalAppState.getInstance().reset();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Error: Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, BluetoothConstants.REQUEST_ENABLE_BT);
        } else {
            if (mBluetoothAdapter.getScanMode() !=
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 30);
                startActivity(discoverableIntent);
            }

            if (mChatService == null) {
                // Initialize the BluetoothChatService to perform bluetooth connections
                mChatService = new BluetoothChatService();
            }

            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();

                Handler timerHandler = new Handler();
                Runnable timerRunnable = new IdActivityTimer(timerHandler, this);
                timerHandler.postDelayed(timerRunnable, 200);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    public void buttonBack(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
