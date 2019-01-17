package ua.in.beroal.util;

import android.support.annotation.NonNull;
import android.support.v4.util.AtomicFile;

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
    public static <T> T readJsonFromStream(@NonNull InputStream in, @NonNull Type typeOfT)
            throws IOException {
        T r;
        try {
            try (InputStreamReader charIn = new InputStreamReader(
                    new BufferedInputStream(in), "UTF-8")) {
                r = new com.google.gson.Gson().fromJson(
                        new com.google.gson.stream.JsonReader(charIn), typeOfT);
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 must be supported.", e);
        }
        return r;
    }

    public static <T> T readJsonFromAtomicFile(@NonNull AtomicFile file, @NonNull Type typeOfT)
            throws IOException {
        T r;
        try (FileInputStream in = file.openRead()) {
            r = readJsonFromStream(in, typeOfT);
        }
        return r;
    }

    public static <T> void writeJsonToStream(T a, @NonNull Type typeOfT, @NonNull OutputStream out)
            throws IOException {
        try {
            try (OutputStreamWriter charOut = new OutputStreamWriter(
                    new BufferedOutputStream(out), "UTF-8")) {
                new com.google.gson.Gson().toJson(a, typeOfT, charOut);
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 must be supported.", e);
        }
    }

    public static <T> void writeJsonToAtomicFile(T a, @NonNull Type typeOfT, @NonNull AtomicFile file)
            throws IOException {
        final FileOutputStream out = file.startWrite();
        boolean written = false;
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
}
