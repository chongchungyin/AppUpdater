package com.github.javiersantos.appupdater;

import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.Context.DOWNLOAD_SERVICE;

public class AppDownloader {

    private static final String ACTION_INSTALL_COMPLETE = "com.afwsamples.testdpc.INSTALL_COMPLETE";
    @NonNull
    private Context context;
    private final String fileName = "launcher.apk";
    private String downloadUrl;
    private String targetVersion;
    private long enqueueId = -1;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                if (enqueueId == downloadId) {
                    File downloadFile = getDownloadFile();
                    if (downloadFile != null) {
                        /*Intent downloadIntent = new Intent(Intent.ACTION_VIEW);
                        downloadIntent.setDataAndType(Uri.fromFile(downloadFile), "application/vnd.android.package-archive");
                        downloadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        downloadIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        context.startActivity(downloadIntent);*/

                        /*Intent install = new Intent(Intent.ACTION_VIEW);
                        install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        Uri apkURI = FileProvider.getUriForFile(
                                context,
                                context.getApplicationContext()
                                        .getPackageName() + ".provider", downloadFile);
                        install.setDataAndType(apkURI, "application/vnd.android.package-archive");
                        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        context.startActivity(install);*/

                        //File file = new File(context.getFilesDir(), filename);
                        int size = (int) downloadFile.length();
                        byte[] bytes = new byte[size];
                        try {
                            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(downloadFile));
                            buf.read(bytes, 0, bytes.length);
                            buf.close();
                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        String filename = "launcher.apk";
                        FileOutputStream outputStream;

                        try {
                            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
                            outputStream.write(bytes);
                            outputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        /*File file = new File(context.getFilesDir(), filename);
                        try {
                            InputStream targetStream = new FileInputStream(file);
                            installPackage(context, targetStream, "com.github.javiersantos.appupdater.demo");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/

                        File file = new File(context.getFilesDir(), filename);
                        Intent install = new Intent(Intent.ACTION_VIEW);
                        install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        Uri apkURI = FileProvider.getUriForFile(
                                context,
                                context.getApplicationContext()
                                        .getPackageName() + ".provider", file);
                        install.setDataAndType(apkURI, "application/vnd.android.package-archive");
                        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        context.startActivity(install);

                    }
                }
            }
        }
    };

    public static boolean installPackage(Context context, InputStream in, String packageName)
            throws IOException {
        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setAppPackageName(packageName);
        // set params
        int sessionId = packageInstaller.createSession(params);
        PackageInstaller.Session session = packageInstaller.openSession(sessionId);
        OutputStream out = session.openWrite("COSU", 0, -1);
        byte[] buffer = new byte[65536];
        int c;
        while ((c = in.read(buffer)) != -1) {
            out.write(buffer, 0, c);
        }
        session.fsync(out);
        in.close();
        out.close();

        session.commit(createIntentSender(context, sessionId));
        return true;
    }

    private static IntentSender createIntentSender(Context context, int sessionId) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                new Intent(ACTION_INSTALL_COMPLETE),
                0);
        return pendingIntent.getIntentSender();
    }

    public AppDownloader(Context context) {
        this.context = context;
    }

    public void setTargetVersion(String targetVersion) {
        this.targetVersion = targetVersion;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void startDownload() {
        //Clean Up
        cleanUpDownloadFile();

        //Start Download
        DownloadManager dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(downloadUrl));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getDownloadFileName());
        context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        enqueueId = dm.enqueue(request);
    }

    public void cancelDownload() {
        context.unregisterReceiver(receiver);
    }

    public void cleanUpDownloadFile() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), getDownloadFileName());
        if (file.exists()) {
            file.delete();
        }
    }

    @Nullable
    public File getDownloadFile() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), getDownloadFileName());
        if (file.exists()) {
            return file;
        }
        return null;
    }

    public String getDownloadFileName() {
        return fileName;
    }
}
