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
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class IdActivity extends AppCompatActivity {

    public String JSdata = null;

    private WebView webView1;
    private TextView textViewDomain, textViewAddress, textViewTitle;
    private ImageView imageViewBadge;

    protected void copyAssetFiles(String fileName) throws IOException {
         Utils.putFileContents(getApplicationContext(), fileName, Utils.getFileContents(getAssets().open(fileName)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id);

        textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        textViewDomain = (TextView) findViewById(R.id.textViewDomain);
        textViewAddress = (TextView) findViewById(R.id.textViewAddress);
        imageViewBadge = (ImageView) findViewById(R.id.imageViewBadge);
        webView1 = (WebView) findViewById(R.id.webView1);

        try {
            String contents = null;
            String baseUrl2 = null;
            contents = new String(Utils.getFileContents(getAssets().open("ethTest.html")));

            String network = (String) GlobalAppSettings.getInstance().getSettings().get("network");
            String networkServerAddress = null;
            switch (network) {
                case "Live":
                    networkServerAddress = GlobalAppConstants.ADDRESS_INFURA_LIVE;
                    break;
                case "Ropsten":
                    networkServerAddress = GlobalAppConstants.ADDRESS_INFURA_TEST;
                    break;
                case "Localhost":
                    networkServerAddress = GlobalAppConstants.ADDRESS_LOCALHOST;
                    break;
                default:
                    throw new Exception("Unrecognized network setting: " + network);
            }
            contents = contents.replaceAll("TEMPLATE_TAG_SERVER", networkServerAddress);
            contents = contents.replaceAll("TEMPLATE_TAG_ADDRESS", GlobalAppState.getInstance().getItsAddress());
            contents = contents.replaceAll("TEMPLATE_TAG_NAME", GlobalAppState.getInstance().getItsDomain());
            Utils.putFileContents(getApplicationContext(), "tmp.html", contents.getBytes());

            String contents2 = new String(Utils.getFileContents(getApplicationContext(), "tmp.html"));

            copyAssetFiles("web3.js"); // TODO: copy only if doesnt exist
            copyAssetFiles("ensutils.js");

            Context context = getApplicationContext();
            File rootDirectory = context.getFilesDir();
            baseUrl2 = "file://" + rootDirectory.getAbsolutePath() + File.separator + "tmp.html";

            final String baseUrl = baseUrl2;
            final JSInterface myJavaScriptInterface = new JSInterface();
            //webView1.setWebViewClient(new WebViewClient()); // not needed?
            webView1.getSettings().setLightTouchEnabled(true);
            webView1.getSettings().setJavaScriptEnabled(true);
            webView1.addJavascriptInterface(myJavaScriptInterface, "AndroidFunction");

            textViewTitle.setText("Connecting to blockchain...");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView1.loadUrl(baseUrl);
                }
            });

            Handler timerHandler = new Handler();
            Runnable timerRunnable = new IdActivity.IdActivityTimer(timerHandler, this);
            timerHandler.postDelayed(timerRunnable, 200);

        } catch (Exception e) {
            Utils.sendFeedbackEmail(getApplicationContext(), (Exception) e);
            Toast.makeText(getApplicationContext(), GlobalAppConstants.UNEXPECTED_FATAL_EXCEPTION_MESSAGE, Toast.LENGTH_LONG).show();
        }

    }

    public void buttonOk(View view) throws Exception {
        finish();
    }

    class IdActivityTimer implements Runnable {
        //runs without a timer by reposting this handler at the end of the runnable
        private Handler timerHandler;
        private AppCompatActivity activity;

        public IdActivityTimer(Handler timerHandler, AppCompatActivity activity) {
            this.timerHandler = timerHandler;
            this.activity = activity;
        }

        @Override
        public void run() {
            if (JSdata != null) {
                String ownerAddress = new String(JSdata);
                textViewDomain.setText(GlobalAppState.getInstance().getItsDomain());
                textViewAddress.setText(GlobalAppState.getInstance().getItsAddress());
                if (ownerAddress.equalsIgnoreCase(GlobalAppState.getInstance().getItsAddress())) {
                    textViewTitle.setText("Badge belongs to");
                    imageViewBadge.setVisibility(View.VISIBLE);
                } else {
                    textViewTitle.setText("Could NOT verify identity of");
                }
            } else {
                timerHandler.postDelayed(this, 200);
            }
        }
    }

    public class JSInterface {
        @JavascriptInterface
        public void setDataFromJavaScript(String data){
            try {
                JSdata = new String(data);
            } catch (Exception e) {
                // ignore exception "Only the original thread that created a view hierarchy can touch its views."
                // TODO: manage this exception properly
            }
        }
    }

}
