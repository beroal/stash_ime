package ua.in.beroal.util;

import android.support.v4.util.AtomicFile;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

public class Gson {
    public static <T> T readJsonFromStream(InputStream in, Type typeOfT) throws IOException {
        T r;
        try {
            final InputStreamReader charIn = new InputStreamReader(new BufferedInputStream(in), "UTF-8");
            try {
                r = new com.google.gson.Gson().fromJson(new com.google.gson.stream.JsonReader(charIn), typeOfT);
            } finally {
                charIn.close();
            }
        } catch (UnsupportedEncodingException e) {
            Log.e("Search", "UTF-8 must be supported.", e);
            System.exit(200);
            r = null;
        }
        return r;
    }

    public static <T> T readJsonFromAtomicFile(AtomicFile file, Type typeOfT) throws IOException {
        T r;
        final FileInputStream in = file.openRead();
        try {
            r = readJsonFromStream(in, typeOfT);
        } finally {
            in.close();
        }
        return r;
    }

    public static <T> void writeJsonToAtomicFile(T a, Type typeOfT, AtomicFile file) throws IOException {
        boolean written = false;
        final FileOutputStream out = file.startWrite();
        try {
            writeJsonToStream(a, typeOfT, out);
            written = true;
        } finally {
            if (written) {
                file.finishWrite(out);
            } else {
                file.failWrite(out);
            }
        }
    }

    private static <T> void writeJsonToStream(T a, Type typeOfT, OutputStream out) throws IOException {
        try {
            final OutputStreamWriter charOut = new OutputStreamWriter(new BufferedOutputStream(out), "UTF-8");
            try {
                new com.google.gson.Gson().toJson(a, typeOfT, charOut);
            } finally {
                charOut.close();
            }
        } catch (UnsupportedEncodingException e) {
            Log.e("Search", "UTF-8 must be supported.", e);
            System.exit(200);
        }
    }
}
