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

import android.content.Context;
import android.util.Log;

import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.util.HashMap;
import java.util.Map;

/**
 * To handle the app's global settings conveniently
 */

public class GlobalAppSettings {

    private static final String TAG = "GlobalAppSettings";

    private final static String APP_SETTINGS_FILE_NAME = "AppSettings";
    private final static String DEFAULT_SETTINGS_FILE_NAME = "DefaultSettings";
    private static GlobalAppSettings instance;
    private boolean isInitialized;
    private Context context;
    private CryptoKeyStore keyManager;
    private byte[] masterKey;
    private SimpleCrypter crypter;
    private Serializer serializer;
    private Map<String, Object> settings;
    private String extraPassword;
    private boolean extraPasswordWasCorrect;
    public String publicKey;

    // singleton
    private GlobalAppSettings() {}
    public static GlobalAppSettings getInstance() {
        if(instance == null) {
            instance = new GlobalAppSettings();
            instance.isInitialized = false;
        }
        return instance;
    }

    public void initialize(Context ctx) throws Exception {

        if (isInitialized != true) {
            Log.d(TAG, "Initializing global settings...");

            context = ctx;
            keyManager = CryptoKeyStore.getInstance();
            keyManager.initialize(context);
            masterKey = keyManager.getMasterKey();
            crypter = new SimpleCrypter();
            serializer = new Serializer();
            extraPassword = null;
            extraPasswordWasCorrect = false;

            //int versionCode = BuildConfig.VERSION_CODE; // not needed by now
            String versionName = BuildConfig.VERSION_NAME;

            if (!Utils.contextFileExists(DEFAULT_SETTINGS_FILE_NAME, context)) {
                saveSomeDefaultSettings();
                Log.d(TAG, "Created default global settings");
            }
            loadSettings();
            if (settings.get("versionName") == null || ((String) settings.get("versionName")).compareTo(versionName) != 0) {
                Log.d(TAG, "Settings was created in a different version. Restoring settings to default");
                Utils.deleteFile(DEFAULT_SETTINGS_FILE_NAME, context);
                Utils.deleteFile(APP_SETTINGS_FILE_NAME, context);
                saveSomeDefaultSettings();
            }
            isInitialized = true;
            Log.d(TAG, "Global settings initialized");
        }
    }

    private void saveSomeDefaultSettings() throws Exception {
        String versionName = BuildConfig.VERSION_NAME;
        Map<String, Object> settings = new HashMap<String, Object>();
        settings.put("versionName", versionName);
        settings.put("network", "Ropsten");
        settings.put("privateKey", DoNotCommit.DEFAULT_PRIVATE_KEY);
        settings.put("domain", DoNotCommit.DEFAULT_ENS_NAME);
        saveSettings(settings);
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public boolean wasPasswordCorrect() {
        return extraPasswordWasCorrect;
    }

    private static byte[] passwordStringToKey(String password) {
        return Hash.sha3(password.getBytes());

    }
    public void setPassword(String password) throws Exception {
        extraPassword = password;
    }

    public boolean isPasswordSet() {
        return extraPassword != null;
    }

    public boolean usingDefaultSettings() {
        return !Utils.contextFileExists(APP_SETTINGS_FILE_NAME, context);
    }

    public void loadSettings() throws Exception {
        String fileName;
        if (extraPassword == null) {
            fileName = DEFAULT_SETTINGS_FILE_NAME;
        } else {
            if (!Utils.contextFileExists(APP_SETTINGS_FILE_NAME, context)) {
                saveSomeDefaultSettings();
                Log.d(TAG, "Created default global settings");
            }
            fileName = APP_SETTINGS_FILE_NAME;
        }
        loadSettingsFromFile(fileName, extraPassword);
    }

    public void saveSettings(Map<String, Object> settings) throws CantContinueException {
        String fileName;
        if (extraPassword == null) {
            fileName = DEFAULT_SETTINGS_FILE_NAME;
        } else {
            fileName = APP_SETTINGS_FILE_NAME;
        }
        saveSettingsToFile(fileName, settings, extraPassword);
    }

    protected void loadSettingsFromFile(String fileName, String extraPassword) throws CantContinueException {
        try {
            byte[] buffer = Utils.getFileContents(context, fileName);
            buffer = crypter.decrypt(buffer, masterKey);
            SerializableContainer settingsWrapper = (SerializableContainer) serializer.deserialize(buffer);
            boolean isSettingsEncrypted = (boolean) settingsWrapper.data.get("isSettingsEncrypted");
            byte[] buffer2 = (byte[]) settingsWrapper.data.get("settings");
            SerializableContainer appConfig = null;
            if (isSettingsEncrypted) {
                try {
                    buffer2 = crypter.decrypt(buffer2, passwordStringToKey(extraPassword));
                    appConfig = (SerializableContainer) serializer.deserialize(buffer2);
                    extraPasswordWasCorrect = true;
                } catch (Exception e) {
                    // TODO: here we assume by the thrown exception (maybe wrongly) that extraPassword is not correct
                    extraPasswordWasCorrect = false; // wrong extraPassword
                    this.extraPassword = null;
                    return;
                }
            } else {
                appConfig = (SerializableContainer) serializer.deserialize(buffer2);
            }
            settings = appConfig.data;
            String privateKey = (String) settings.get("privateKey");
            publicKey = Hex.toHexString(Sign.publicKeyFromPrivate(Numeric.toBigInt(privateKey)).toByteArray());
            //ethereumAddress = Keys.getAddress(publicKey); // TODO: not needed by now but include later
        } catch (Exception e) {
            throw new CantContinueException(e);
        }
    }

    protected void saveSettingsToFile(String fileName, Map<String, Object> settings, String extraPassword) throws CantContinueException {
        try {
            SerializableContainer appConfig = new SerializableContainer();
            appConfig.data = settings;
            byte[] buffer = serializer.serialize(appConfig);
            boolean isSettingsEncrypted = (extraPassword != null);
            if (isSettingsEncrypted) {
                buffer = crypter.encrypt(buffer, passwordStringToKey(extraPassword));
            }
            SerializableContainer settingsWrapper = new SerializableContainer();
            settingsWrapper.data.put("isSettingsEncrypted", isSettingsEncrypted);
            settingsWrapper.data.put("settings", buffer);
            byte[] buffer2 = serializer.serialize(settingsWrapper);
            buffer2 = crypter.encrypt(buffer2, masterKey);
            Utils.putFileContents(context, fileName, buffer2);
        } catch (Exception e) {
            throw new CantContinueException(e);
        }
    }

}

