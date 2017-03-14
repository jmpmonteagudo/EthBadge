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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


class StorageData implements Serializable {
    private final static long serialVersionUID = 1;
    public Map<String, byte[]> keys = new HashMap<>();
}

public class KeyManager {
    private static KeyManager instance;
    private StorageData storageData;
    private static final String FILE_NAME = "config.cfg";
    private Context context;
    private Serializer serializer;

    public static KeyManager getInstance() {
        if(instance == null) {
            instance = new KeyManager();
        }
        return instance;
    }

    protected KeyManager() {
        serializer = new Serializer();
    }

    public void initialize(Context context) throws IOException, ClassNotFoundException {
        this.context = context;
        FileInputStream inputStream;
        context.deleteFile(FILE_NAME);
        try {
            inputStream = this.context.openFileInput(FILE_NAME);
        } catch (FileNotFoundException e) {
            storageData = new StorageData();
            saveFile();
            inputStream = this.context.openFileInput(FILE_NAME);
        }
        byte[] data = new byte[(int) inputStream.getChannel().size()];
        inputStream.read(data);
        storageData = (StorageData) serializer.deserialize(data);
    }

    public byte[] getKey(String alias) {
        return storageData.keys.get(alias);
    }

    public Set<String> getKeys() {
        return storageData.keys.keySet();
    }

    public void setKey(String alias, byte[] key) throws IOException {
        storageData.keys.put(alias, key);
        saveFile();
    }

    public void deleteKey(String alias) throws IOException {
        // TODO
    }

    private void saveFile() throws IOException {
        byte[] data = serializer.serialize(storageData);
        FileOutputStream outputStream = context.openFileOutput(FILE_NAME, context.MODE_PRIVATE);
        outputStream.write(data);
        outputStream.close();
    }

}



