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
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;


public class Utils {

    public static boolean contextFileExists(String filename, Context context) {
        try {
            context.openFileInput(filename);
        } catch (FileNotFoundException e) {
            return false;
        }
        return true;
    }

    public static boolean deleteFile(String filename, Context context) {
        File dir = context.getFilesDir();
        File file = new File(dir, filename);
        boolean deleted = file.delete();
        return deleted;
    }

    public static byte[] getFileContents(Context context, String filePath) {
        byte[] output = null;
        try {
            FileInputStream inputStreamFile = context.openFileInput(filePath);
            return getFileContents(inputStreamFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    public static byte[] getFileContents(InputStream inputStream) {
        byte[] output = null;
        try {
            ArrayList<Byte> buffer = new ArrayList<>();
            int i;
            while ((i = inputStream.read()) != -1) {
                buffer.add((byte) i);
            }
            output = Utils.toArray(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    public static void putFileContents(Context context, String filePath, byte[] contents) throws IOException {
        FileOutputStream fileOutputStream = context.openFileOutput(filePath, 0);
        try {
            fileOutputStream.write(contents);
        } finally {
            fileOutputStream.close();
        }
    }


    public static byte[] toArray(ArrayList<Byte> input) {
        byte[] output = new byte[input.size()];
        for(int i = 0; i < output.length; i++) {
            output[i] = input.get(i).byteValue();
        }
        return output;
    }

    public static void ShowAlertDialog(Context context, String text, final List<Object> dialogResults) {
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setTitle("Please enter a password");
        final EditText input = new EditText(context);
        b.setView(input);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dialogResults.add(input.getText().toString());
            }
        });
        b.setNegativeButton("CANCEL", null);
        b.show();
    }

    public static void YesNoDialog(final Context context, final Exception feedbackException) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.dialogTheme));
            builder.setMessage("Oops. An unexpected error happened. Do you want to send an email with ").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        } catch (Exception e) {
            Toast.makeText(context, e.getStackTrace().toString(), Toast.LENGTH_LONG).show();
        }
    }

    public static void sendFeedbackEmail(final Context context, Exception e) {

        class RetrieveFeedTask extends AsyncTask<String, Void, Void> {

            protected Void doInBackground(String... exceptionInfo) {
                try {
                    String stackInfo = exceptionInfo[0];
                    String manufacturer = Build.MANUFACTURER;
                    String model = Build.MODEL;
                    String androidVersionRelease = Build.VERSION.RELEASE;
                    int androidVersionSdk = android.os.Build.VERSION.SDK_INT;
                    String appName = context.getResources().getText(
                            context.getResources().getIdentifier("app_name",
                            "string", context.getPackageName())).toString();
                    String appVersionName = BuildConfig.VERSION_NAME;

                    GMailSender sender = new GMailSender(DoNotCommit.gmailAddress, DoNotCommit.gmailPassword);
                    sender.sendMail("[App: " + appName +"] Exception report",
                                    stackInfo + "\n" +
                                    "appVersionName: " + appVersionName + "\n" +
                                    "manufacturer: " + manufacturer + "\n" +
                                    "model: " + model + "\n" +
                                    "androidVersionRelease: " + androidVersionRelease + "\n" +
                                    "androidVersionSdk: " + androidVersionSdk + "\n"
                            ,
                            DoNotCommit.gmailAddress,
                            DoNotCommit.gmailAddress);
                } catch (Exception e) {
                    Log.e("SendMail", e.getMessage(), e);
                }
                return null;
            }

            protected void onPostExecute(Void nothing) {
                int i = 5;
            }
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        new RetrieveFeedTask().execute(stackTrace);
    }

    public static byte[] createRandomNumber(int numBytes) {
        SecureRandom random = new SecureRandom();
        byte someNumber[] = new byte[numBytes];
        random.nextBytes(someNumber);
        return someNumber;
    }
}
