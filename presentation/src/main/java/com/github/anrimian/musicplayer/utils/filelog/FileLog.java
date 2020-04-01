package com.github.anrimian.musicplayer.utils.filelog;

import android.content.Context;
import android.os.Build;

import com.github.anrimian.musicplayer.BuildConfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import static java.io.File.separator;

public class FileLog {

    private static final String LOG_FILE_NAME = "log.txt";
    private static final int MAX_FILE_SIZE = 2 * 1024 * 1024;

    private final Context context;

    public FileLog(Context context) {
        this.context = context;
    }

    public boolean isFileExists() {
        return getFile().exists();
    }

    public long getFileSize() {
        return getFile().length();
    }

    public void writeMessage(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("Log message:\n");
        appendSystemInfo(sb);

        sb.append("Message: ");
        sb.append(message);
        sb.append("\n");

        writeLog(sb.toString());
    }

    public void writeException(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append("Non fatal error:\n");
        appendSystemInfo(sb);

        sb.append("Stacktrace: ");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        String stackTrace = sw.toString();
        sb.append(stackTrace);
        sb.append("\n");

        writeLog(sb.toString());
    }

    public File getFile() {
        String path = context.getFilesDir().getAbsolutePath() + separator + LOG_FILE_NAME;
        return new File(path);
    }

    public String getLogText() {
        try (FileInputStream fin = new FileInputStream(getFile());
             BufferedReader reader = new BufferedReader(new InputStreamReader(fin))
        ) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private void appendSystemInfo(StringBuilder sb) {
        sb.append("App version code: ");
        sb.append(BuildConfig.VERSION_CODE);
        sb.append("\n");

        sb.append("Android system version: ");
        sb.append(Build.VERSION.SDK_INT);
        sb.append("\n");

        sb.append("Device: ");
        sb.append(Build.MODEL);
        sb.append("\n");
    }

    private void writeLog(String text) {
        File logFile = getFile();
        if (!logFile.exists()) {
            try {
                boolean created = logFile.createNewFile();
                if (!created) {
                    return;
                }
            } catch (IOException ignored) {
                return;
            }
        } else if (logFile.length() > MAX_FILE_SIZE) {
            return;
        }
        try (BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true))) {
            buf.append(text);
            buf.newLine();
        } catch (IOException ignored) {}
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void deleteLogFile() {
        getFile().delete();
    }
}
