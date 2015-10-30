package com.example.wera.ciscoapp.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadFileTask extends AsyncTask<String, Integer, File> {

    private Context context;

    private DownloadOptions options;

    public DownloadFileTask(Context context, DownloadOptions options) {
        this.context = context;
        this.options = options;
    }

    @Override
    protected File doInBackground(String... params) {
        File outputFile = null;
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            Log.d("DownloadFileTask", "Filename: " + options.fileName);

            // If a file path is not provided, uses internal file storage
            File outputDir;
            if (options.filePath != null) {
                outputDir = new File(options.filePath);
            } else {
                outputDir = context.getFilesDir();
            }

            // Creates the directory the file is held in if it doesn't exist
            if (!outputDir.exists()) {
                boolean dirBuilt = outputDir.mkdirs();
                if (!dirBuilt) {
                    throw new RuntimeException("Could not build directory: " + outputDir.getPath());
                }
            }
            Log.d("DownloadFileTask", "Directory: " + outputDir.getPath());

            // Creates the File object to write the download data to
            outputFile = new File(outputDir, options.fileName);

            // If the file already exists and it doesn't require an overwrite, returns the file
            Log.d("DownloadFileTask", "Overwrite: " + options.overwrite);
            if (outputFile.exists() && !options.overwrite) {
                return outputFile;
            }

            // Checks if the network exists. If it doesn't and there is already a file, returns it.
            // Else, returns null.
            Log.d("DownloadFilesTask", "Internet: " + isNetworkAvailable());
            if (!isNetworkAvailable()) {
                if (outputFile.exists()) {
                    return outputFile;
                } else {
                    return null;
                }
            }

            // Sets up the output stream to write the file to
            output = new FileOutputStream(outputFile);

            // Sets up the HTTP connection the file is to be downloaded from
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // Expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.w("DownloadFileTask", "Server returned: HTTP '" + connection.getResponseCode()
                        + " " + connection.getResponseMessage() + "' for " + params[0]);
                return null;
            }

            // This will be useful to display download percentage
            // Might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // Opens the input stream
            input = connection.getInputStream();

            // Writes to the file through the input/output stream
            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = input.read(buffer)) != -1) {
                output.write(buffer, 0, len1);
            }
        } catch (Exception e) {
            Log.w("DownloadFileTask", e.toString());
            return null;
        } finally {
            // Attempts to close input and output streams
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (IOException ignored) {

            }
            // Attempts to close the URL connection
            if (connection != null) {
                connection.disconnect();
            }
        }

        return outputFile;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    public static class DownloadOptions {

        private String filePath;
        private String fileName;
        private boolean overwrite;

        public DownloadOptions setFilePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public DownloadOptions setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public DownloadOptions setOverwrite(boolean overwrite) {
            this.overwrite = overwrite;
            return this;
        }
    }
}
