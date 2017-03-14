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

import android.util.Base64;

import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

class ProtocolMessageContainer  implements Serializable {
    private final static long serialVersionUID = 1;
    public Map<String, Object> data = new HashMap<>();
}

public class ProtocolHandler {

    //private int itsProtocolVersion; // TODO: use this to avoid version compatibility issues in the future
    private String itsDomain;
    private BluetoothChatService mChatService;
    private Serializer serializer = new Serializer();
    private String privateKeyHexStr, publicKeyHexStr;
    private BigInteger privateKey, publicKey, itsPublicKey;

    void JustConnected(BluetoothChatService mChatService) throws IOException {
        // TODO: get the name of the device we are connected to and display it so user knows
        GlobalAppState.getInstance().setConnected();
        this.mChatService = mChatService;
        privateKeyHexStr = (String) GlobalAppSettings.getInstance().getSettings().get("privateKey");
        publicKeyHexStr = GlobalAppSettings.getInstance().publicKey;
        privateKey = Numeric.toBigInt(privateKeyHexStr);
        publicKey = Numeric.toBigInt(publicKeyHexStr);
        ProtocolMessageContainer messageContainer = new ProtocolMessageContainer();
        Map data = messageContainer.data;
        data.put("publicKey", publicKeyHexStr);
        data.put("domain", (String) GlobalAppSettings.getInstance().getSettings().get("domain"));
        byte[] b = serializer.serialize(messageContainer);
        String message = Base64.encodeToString(b, Base64.NO_WRAP);
        byte[] b2 = Base64.decode(message, Base64.NO_WRAP);
        mChatService.sendMessage(message);
    }

    void NewMessageReceived(String message) throws IOException, ClassNotFoundException, SignatureException {
        ProtocolMessageContainer messageContainer = (ProtocolMessageContainer) serializer.deserialize(Base64.decode(message, Base64.NO_WRAP));
        Map data = messageContainer.data;
        String itsSignature = null;
        String itsDomain = null;
        String itsPublicKeyHexStr = null;

        if (data.get("publicKey") != null)
            itsPublicKeyHexStr = (String) data.get("publicKey");
        if (data.get("signature") != null)
            itsSignature = (String) data.get("signature");
        if (data.get("domain") != null) {
            itsDomain = (String) data.get("domain");
            this.itsDomain = itsDomain;
        }

        if (itsPublicKeyHexStr != null) {
            // send signature
            itsPublicKey = Numeric.toBigInt(itsPublicKeyHexStr);
            ECKeyPair keyPair = new ECKeyPair(privateKey, publicKey);
            Sign.SignatureData signatureData = Sign.signMessage(itsPublicKey.toByteArray(), keyPair);
            /*
            TODO: some apps might be used without a private key, meaning that the user cannot send a publickey.
            So, instead of signing itsPublicKey, a random number should be used instead
            */
            String signatureDataR = Hex.toHexString(signatureData.getR()); // string of length 64
            String signatureDataS = Hex.toHexString(signatureData.getS()); // string of length 64
            byte[] v = new byte[1];
            v[0] = signatureData.getV();
            String signatureDataV = Hex.toHexString(v); // string of length 2
            String signatureDataStr = signatureDataV + signatureDataR + signatureDataS ; // string of length 2 + 64 + 64 = 130

            messageContainer = new ProtocolMessageContainer();
            data = messageContainer.data;
            data.put("signature", signatureDataStr);
            message = Base64.encodeToString(serializer.serialize(messageContainer), Base64.NO_WRAP);
            mChatService.sendMessage(message);
        }

        if (itsSignature != null) {
            // if all ok...
            byte[] v = Hex.decode(itsSignature.substring(0, 2));
            byte[] r = Hex.decode(itsSignature.substring(2, 66));
            byte[] s = Hex.decode(itsSignature.substring(66, 130));
            Sign.SignatureData signatureData = new Sign.SignatureData(v[0], r, s);
            BigInteger someonesPublicKey = Sign.signedMessageToKey(publicKey.toByteArray(), signatureData);
            if (someonesPublicKey.compareTo(itsPublicKey) == 0) {
                String address = "0x" + Keys.getAddress(itsPublicKey);
                GlobalAppState.getInstance().publicPrivateKeyPairIsValidated(address, this.itsDomain);
            } else {
                GlobalAppState.getInstance().publicPrivateKeyPairValidationFailed();
            }
            mChatService.stop();
        }
    }
}



