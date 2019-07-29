/*
 * Vechain Wallet SDK is licensed under the MIT LICENSE, also included in LICENSE file in the repository.
 *
 * Copyright (c) 2019 VeChain support@vechain.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.vechain.walletdemo.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    public final static String KEYSTORE_NAME = "my.keystore";

    /**
     * Write text files in Android system and save them in / data / data / PACKAGE_NAME/files directory
     */
    public static void writeCache(Context context, String fileName, String content) {
        if (content == null)
            content = "";
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            byte[] data = content.getBytes("utf-8");
            fos.write(data);
            data = null;
            fos.close();
            fos = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Read files in / data / data / PACKAGE_NAME/files directory
     */
    public static String readCache(Context context, String fileName) {
        String content = "";
        try {
            File dir = context.getFilesDir();
            File file = new File(dir, fileName);
            if (!file.exists())
                return content;
            FileInputStream in = context.openFileInput(fileName);
            if (in != null)
                content = readInStream(in);
        } catch (Exception e) {
            e.printStackTrace();
            content = "";
        }
        return content;
    }

    private static String readInStream(FileInputStream inStream) {
        String content = "";
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int length = -1;
            while ((length = inStream.read(buffer)) > -1) {
                outStream.write(buffer, 0, length);
            }

            outStream.close();
            inStream.close();

            byte[] data = outStream.toByteArray();
            content = new String(data, "utf-8");
        } catch (IOException e) {
            Log.i("FileTest", e.getMessage());
        }

        return content;
    }


    public static void save(InputStream inputStream, File toFile) {
        try {
            FileOutputStream outputStream = new FileOutputStream(toFile);

            int byteCount = 0;
            byte[] buffer = new byte[2048];
            while ((byteCount = inputStream.read(buffer)) > -1) {
                outputStream.write(buffer, 0, byteCount);
            }
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
        }
    }


    public static String readFile(File file) {
        String content = "";
        if (!file.exists()) return content;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            FileInputStream input = new FileInputStream(file);
            byte[] buffer = new byte[2048];
            int len = 0;
            while ((len = input.read(buffer)) > -1) {
                out.write(buffer, 0, len);
            }
            input.close();
            input = null;

            byte[] arry = out.toByteArray();
            out.close();
            out = null;

            content = new String(arry, "utf-8");

        } catch (Exception e) {
        }
        return content;
    }

    public static void save(String msg, File destfile) {

        if (TextUtils.isEmpty(msg)) return;
        if (destfile == null) return;
        try {
            if (!destfile.exists())
                destfile.createNewFile();

            FileOutputStream outputStream = new FileOutputStream(destfile);
            byte[] data = msg.getBytes("utf-8");
            outputStream.write(data);
            data = null;
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}