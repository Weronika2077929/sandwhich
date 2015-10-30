package com.example.wera.ciscoapp.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public final class FileManager {

    private static LruCache<String, Bitmap> bitmapCache;
    private static boolean cacheBitmaps;

    public static void enableBitmapCaching(Context context) {
        cacheBitmaps = true;

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int memClass = am.getMemoryClass();
        int cacheSize = 1024 * 1024 * memClass / 8;
        bitmapCache = new LruCache<>(cacheSize);
    }

    public static Bitmap readBitmapFile(File file) {
        Bitmap bitmap = null;

        if (cacheBitmaps) {
            bitmap = bitmapCache.get(file.getName());
            if (bitmap != null) return bitmap;
        }

        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }

        bitmap = BitmapFactory.decodeStream(fis);
        if (bitmap != null) bitmapCache.put(file.getName(), bitmap);

        return bitmap;
    }

    public static Bitmap readBitmapFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            return readBitmapFile(file);
        }
        return null;
    }

    /**
     * Gets the list of the all files into a directory
     */
    public static String[] getDirectoryFiles(String dir, AssetManager manager) throws IOException {
        StringBuilder sb;
        String[] files = manager.list(dir);

        for (int i = 0; i < files.length; i++) {
            sb = new StringBuilder();
            sb.append(dir);
            sb.append("/");
            sb.append(files[i]);
            files[i] = sb.toString();
            sb = null;
        }

        return files;
    }

    public static String readTextFile(File file) {
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static String readTextFile(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + fileName);
        }
        return readTextFile(file);
    }

    /**
     * Gets the name of a file without the dot, suffix, prefix and directory bar, from an absolute
     * URI path.
     */
    public static String getFileBaseName(String fileName) {
        if (fileName.contains("/")) {
            return fileName.substring(fileName.indexOf("/")+1, fileName.indexOf("."));
        } else {
            return fileName.substring(0, fileName.indexOf("."));
        }
    }

    /**
     * Gets the name of a file without the dot, suffix, prefix and directory bar, from an absolute
     * URI path.
     */
    public static String getFileExtension(String fileName) {
        return fileName.substring(fileName.indexOf("."), fileName.length());
    }

    /**
     * Returns true if the file exists, false if it doesn't.
     */
    public static boolean fileExists(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }
}
