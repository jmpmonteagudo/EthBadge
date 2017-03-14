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

import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SimpleCrypter {
    private byte[] IV = "9a3cc5b7731c53b7".getBytes(); // some arbitrary salt
    private Cipher cipher;

    public SimpleCrypter() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
    }


    public byte[] encrypt(byte[] toEncrypt, byte[] encryptionKey) throws Exception {
        SecretKeySpec key = new SecretKeySpec(encryptionKey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV));
        return cipher.doFinal(toEncrypt);
    }

    public byte[] decrypt(byte[] toDecrypt, byte[] encryptionKey) throws Exception{
        SecretKeySpec key = new SecretKeySpec(encryptionKey, "AES");
        cipher.init(Cipher.DECRYPT_MODE, key,(AlgorithmParameterSpec) new IvParameterSpec(IV));
        return cipher.doFinal(toDecrypt);
    }
}
