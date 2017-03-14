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

import android.app.Application;

public class GlobalAppState extends Application {

    private static GlobalAppState singleton;
    private boolean connected;
    private boolean itsPublicPrivateKeyPairIsValidated;
    private boolean publicPrivateKeyPairValidationFailed;
    private String itsAddress;
    private String itsDomain;
    private boolean isBadgeAuthentic;

    private GlobalAppState() {}

    public static GlobalAppState getInstance() {
        if(singleton == null) {
            singleton = new GlobalAppState();
            singleton.reset();
        }
        return singleton;
    }

    public void reset() {
        connected = false;
        itsPublicPrivateKeyPairIsValidated = false;
        publicPrivateKeyPairValidationFailed = false;
        itsAddress = "";
        itsDomain = "";
        isBadgeAuthentic  = false;
    }

    public void setConnected() {
        connected = true;
    }

    public boolean isConnected() {
        return connected;
    }

    public void publicPrivateKeyPairIsValidated(String itsAddress, String itsDomain) {
        this.itsAddress = itsAddress;
        this.itsDomain = itsDomain;
        itsPublicPrivateKeyPairIsValidated = true;
    }

    public void publicPrivateKeyPairValidationFailed() {
        publicPrivateKeyPairValidationFailed = true;
    }

    public boolean getItsPublicPrivateKeyPairIsValidated() {
        return itsPublicPrivateKeyPairIsValidated;
    }

    public boolean getPublicPrivateKeyPairValidationFailed() {
        return publicPrivateKeyPairValidationFailed;
    }

    public String getItsAddress() {
        return itsAddress;
    }

    public String getItsDomain() {
        return itsDomain;
    }

}

