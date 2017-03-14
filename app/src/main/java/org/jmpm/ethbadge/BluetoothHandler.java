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

import android.os.Handler;
import android.os.Message;

public class BluetoothHandler extends Handler {

    private BluetoothChatService mChatService;
    private ProtocolHandler protocolHandler;

    public BluetoothHandler(BluetoothChatService chatService) {
        super();
        mChatService = chatService;
        protocolHandler = new ProtocolHandler();
    }

    @Override
    public void handleMessage(Message msg) {
        try {
            switch (msg.what) {
                case BluetoothConstants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            protocolHandler.JustConnected(mChatService); // start the handshake
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            break;
                    }
                    break;
                case BluetoothConstants.MESSAGE_WRITE:
                    break;
                case BluetoothConstants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    protocolHandler.NewMessageReceived(readMessage); // pass the message to the handshake protocol
                    break;
                case BluetoothConstants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    //mConnectedDeviceName = msg.getData().getString(BluetoothConstants.DEVICE_NAME); // TODO: might use this later
                    break;
                case BluetoothConstants.MESSAGE_TOAST:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}