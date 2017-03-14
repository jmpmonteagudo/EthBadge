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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private RadioButton networkLive, networkRopsten, networkLocalhost;
    private EditText editTextPrivateKey, editTextDomain;
    private TextView textViewVersionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        networkLive = (RadioButton) findViewById(R.id.networkLive);
        networkRopsten = (RadioButton) findViewById(R.id.networkRopsten);
        networkLocalhost = (RadioButton) findViewById(R.id.networkLocalhost);
        editTextPrivateKey = (EditText) findViewById(R.id.editTextPrivateKey);
        editTextDomain = (EditText) findViewById(R.id.editTextDomain);
        textViewVersionName = (TextView) findViewById(R.id.textViewVersionName);

        String appVersionName = BuildConfig.VERSION_NAME;
        textViewVersionName.setText(appVersionName);

        /*
        editTextPrivateKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // TODO: calculate address from new private key and display in a textView
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });
        */
    }

    public void buttonSave(View view) throws Exception {
        saveSettings();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            loadSettings();
        } catch (Exception e) {
            Utils.sendFeedbackEmail(getApplicationContext(), e);
            Toast.makeText(getApplicationContext(), "Oops... an unexpected error happened", Toast.LENGTH_LONG).show();
        }
    }

    public void loadSettings() throws Exception {
        GlobalAppSettings.getInstance().loadSettings();
        Map<String, Object> settings = GlobalAppSettings.getInstance().getSettings();
        String network = (String) settings.get("network");
        switch (network) {
            case "Live":
                networkLive.setChecked(true);
                break;
            case "Ropsten":
                networkRopsten.setChecked(true);
                break;
            case "Localhost":
                networkLocalhost.setChecked(true);
                break;
            default:
                throw new CantContinueException("Unrecognized network setting: " + network);
        }

        String privateKey = (String) settings.get("privateKey");
        editTextPrivateKey.setText(privateKey);

        String domain = (String) settings.get("domain");
        editTextDomain.setText(domain);
    }

    public void saveSettings() throws Exception {
        GlobalAppSettings.getInstance().loadSettings();
        final Map<String, Object> settings = GlobalAppSettings.getInstance().getSettings();

        // TODO: user inputs should be validated !!!

        if (networkLive.isChecked())
            settings.put("network", "Live");
        if (networkRopsten.isChecked())
            settings.put("network", "Ropsten");
        if (networkLocalhost.isChecked())
            settings.put("network", "Localhost");

        settings.put("privateKey", editTextPrivateKey.getText().toString());
        settings.put("domain", editTextDomain.getText().toString());

        new Thread(new Runnable() {
            public void run() {
                try {
                    GlobalAppSettings.getInstance().saveSettings(settings);
                } catch (Exception e) {
                    Utils.sendFeedbackEmail(getApplicationContext(), e);
                    Toast.makeText(getApplicationContext(), "Oops... an unexpected error happened", Toast.LENGTH_LONG).show();
                }
            }
        }).start();

        finish();
    }
}
