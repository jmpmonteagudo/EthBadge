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

import android.Manifest;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements DialogPassword.DialogPasswordListener {

    private static final String TAG = "MainActivity";

    private Intent intentToGo;

    private Thread.UncaughtExceptionHandler handleAppCrash =
            new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable e) {
                    Utils.sendFeedbackEmail(getApplicationContext(), (Exception) e);
                    Toast.makeText(getApplicationContext(), GlobalAppConstants.UNEXPECTED_FATAL_EXCEPTION_MESSAGE, Toast.LENGTH_LONG).show();
                    try {
                        Thread.sleep(10000); // wait for email thread to send the email
                    } catch (InterruptedException e2) {
                    }
                }

            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(handleAppCrash);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            GlobalAppSettings.getInstance().initialize(getApplicationContext());

            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, BluetoothConstants.REQUEST_ENABLE_BT);
            }

        } catch (Exception e) {
            Utils.sendFeedbackEmail(getApplicationContext(), (Exception) e);
            Toast.makeText(getApplicationContext(), GlobalAppConstants.UNEXPECTED_FATAL_EXCEPTION_MESSAGE, Toast.LENGTH_LONG).show();
        }

        try {
            final AppCompatActivity myself = this;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // Only ask for these permissions on runtime when running Android 6.0 or higher
                switch (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    case PackageManager.PERMISSION_DENIED:
                        ((TextView) new AlertDialog.Builder(this)
                                .setTitle("IMPORTANT")
                                .setMessage(Html.fromHtml("<p>This app requires Bluetooth permissions to work. In addition, some devices require access to device's location permissions to find nearby Bluetooth devices. Please click \"Allow\" on the following runtime permissions popup.</p>" +
                                        "<p>For more info see <a href=\"http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id\">here</a>.</p>"))
                                .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                            int i = 5;
                                            ActivityCompat.requestPermissions(myself,
                                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                                    1);
                                        }
                                    }
                                })
                                .show()
                                .findViewById(android.R.id.message))
                                .setMovementMethod(LinkMovementMethod.getInstance());  // Make the link clickable. Needs to be called after show(), in order to generate hyperlinks
                        break;
                    case PackageManager.PERMISSION_GRANTED:
                        break;
                }
            }
        } catch (Exception e) {
            Utils.sendFeedbackEmail(getApplicationContext(), (Exception) e);
            Toast.makeText(getApplicationContext(), "Error: Could not set permissions", Toast.LENGTH_LONG).show();
        }
    }

    public void buttonHost(View view) {
        Intent intent = new Intent(this, HostActivity.class);
        startActivity(intent);
    }

    public void buttonVisitor(View view) {
        if (!GlobalAppSettings.getInstance().isPasswordSet()) {
            intentToGo = new Intent(this, VisitorActivity.class);
            runDialogPassword();
        } else {
            Intent intent = new Intent(this, VisitorActivity.class);
            startActivity(intent);
        }
    }

    public void buttonSettings(View view) {
        if (!GlobalAppSettings.getInstance().isPasswordSet()) {
            intentToGo = new Intent(this, SettingsActivity.class);
            runDialogPassword();
        } else {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
    }

    public void runDialogPassword() {
        DialogFragment dialog = new DialogPassword();
        Bundle args = new Bundle();
        if (GlobalAppSettings.getInstance().usingDefaultSettings()) {
            args.putString("message", "WARNING: Your private key will be stored using this password. Choose a robust one!");
        }
        dialog.setArguments(args);
        dialog.show(getFragmentManager(), "DialogPassword");
    }

    public void buttonTest(View view) throws Exception {
        Log.i(TAG, "Test button tapped");
        GlobalAppState.getInstance().publicPrivateKeyPairIsValidated("0x2e93eBc28408B05BaA74A207092f5074C141757d", "jose.test");
        Intent intent = new Intent(this, IdActivity.class);
        startActivity(intent);
        //DialogFragment dialog = new DialogPassword();
        //dialog.show(getFragmentManager(), "DialogPassword");
    }

    @Override
    public void onDialogPasswordOk(String password) {
        try {
            GlobalAppSettings.getInstance().setPassword(password);
            GlobalAppSettings.getInstance().loadSettings();
            if (GlobalAppSettings.getInstance().wasPasswordCorrect()) {
                startActivity(intentToGo);
            } else {
                Toast.makeText(getApplicationContext(), "Wrong password. Please try again", Toast.LENGTH_SHORT).show();
                DialogFragment dialog = new DialogPassword();
                dialog.show(getFragmentManager(), "DialogPassword");
            }
        } catch(Exception e) {
            Utils.sendFeedbackEmail(getApplicationContext(), (Exception) e);
            Toast.makeText(getApplicationContext(), "Oops... An error happened", Toast.LENGTH_LONG).show();
        }
    }
}

