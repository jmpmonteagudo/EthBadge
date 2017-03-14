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
import android.security.KeyPairGeneratorSpec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;

/*
 IMPORTANT: This class is used to manage a master encryption key stored using Android's KeyStore, which is
 only available if:
 android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
 Otherwise, it returns a trivial key, which is NOT secure.
*/

public class CryptoKeyStore {

    private static CryptoKeyStore instance;
    private KeyStore keyStore;
    private static final String keyStoreAlias = "EthBadge";
    private static final String cryptoKeyFileName = "cryptoKey";
    private Context context;

    // singleton
    private CryptoKeyStore() {
    }

    public static CryptoKeyStore getInstance() {
        if (instance == null) {
            instance = new CryptoKeyStore();
        }
        return instance;
    }

    public void initialize(Context context) {
        this.context = context;
    }

    public byte[] getMasterKey() throws Exception {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
            return "UNSECURE_BEWARE!".getBytes();

        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        boolean containsKey = keyStore.containsAlias(keyStoreAlias);
        boolean fileExist = Utils.contextFileExists(cryptoKeyFileName, context);

        if (!containsKey || !fileExist) {
            // Create new key
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 100);
            KeyPairGeneratorSpec spec = null;
            spec = new KeyPairGeneratorSpec.Builder(context)
                    .setAlias(keyStoreAlias)
                    .setSubject(new X500Principal("CN=Sample Name, O=Android Authority"))
                    .setSerialNumber(BigInteger.ONE)
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build();
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
            generator.initialize(spec);
            KeyPair keyPair = generator.generateKeyPair();

            // create crypto key and store it in the file system
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(keyStoreAlias, null);
            RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();
            byte someRandomCryptoKey[] = Utils.createRandomNumber(16); // 128 bits

            Cipher inCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            inCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(
                    outputStream, inCipher);
            cipherOutputStream.write(someRandomCryptoKey);
            cipherOutputStream.close();
            byte[] someRandomCryptoKeyEncrypted = outputStream.toByteArray();
            FileOutputStream outputStreamFile = context.openFileOutput(cryptoKeyFileName, context.MODE_PRIVATE);
            outputStreamFile.write(someRandomCryptoKeyEncrypted);
            outputStreamFile.close();
        }

        // retrieve crypto key from file and decrypt it using Android Key Store
        byte[] fileContents = Utils.getFileContents(context, cryptoKeyFileName);

        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(keyStoreAlias, null);
        Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        output.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());

        CipherInputStream cipherInputStream = new CipherInputStream(new ByteArrayInputStream(fileContents), output);

        ArrayList<Byte> buffer = new ArrayList<>();
        int nextByte;
        while ((nextByte = cipherInputStream.read()) != -1) {
            buffer.add((byte) nextByte);
        }

        byte[] someRandomCryptoKeyDecrypted = Utils.toArray(buffer);
        return someRandomCryptoKeyDecrypted;
    }

}