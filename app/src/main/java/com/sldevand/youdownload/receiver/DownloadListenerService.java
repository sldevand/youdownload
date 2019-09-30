package com.sldevand.youdownload.receiver;


import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;


public class DownloadListenerService extends BroadcastReceiver {
    private static final boolean DEBUG = true;
    private static final String TAG = "DownloadListenerService";
    private Context mContext;

    protected OnDownloadCompleteListener onDownloadCompleteListener;

    public interface OnDownloadCompleteListener {
        void ondownloadComplete(File file);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (!DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            return;
        }

        this.mContext = context;

        Bundle b = intent.getExtras();
        long fileId = b.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);

        String realPath = this.getPath(fileId);

        if (realPath.isEmpty()) {
            Toast.makeText(context, "Download Cancelled, No file to process", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(context, "Download Complete, file = " + realPath, Toast.LENGTH_SHORT).show();
        File file = new File(realPath);
        if (null == this.onDownloadCompleteListener) {
            return;
        }
        this.onDownloadCompleteListener.ondownloadComplete(file);
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        final String column = MediaStore.Files.FileColumns.DATA;
        final String[] projection = {
                column
        };
        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                null)) {
            if (cursor == null || !cursor.moveToFirst()) {
                return "";
            }
            if (DEBUG)
                DatabaseUtils.dumpCursor(cursor);

            final int column_index = cursor.getColumnIndexOrThrow(column);
            return cursor.getString(column_index);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    private String getPath(long fileId) {
        String realPath = "content://downloads/public_downloads";

        String[] contentUriPrefixesToTry = new String[]{
                "content://downloads/public_downloads",
                "content://downloads/my_downloads",
                "content://downloads/all_downloads"
        };

        for (String contentUriPrefix : contentUriPrefixesToTry) {
            Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), fileId);
            try {
                realPath = getDataColumn(this.mContext, contentUri, null, null);
                if (realPath != null) {
                    return realPath;
                }
            } catch (Exception e) {
                if (DEBUG) Log.e(TAG, e.getMessage());
            }
        }

        return realPath;
    }

    public void setOnDownloadCompleteListener(OnDownloadCompleteListener onDownloadCompleteListener) {
        this.onDownloadCompleteListener = onDownloadCompleteListener;
    }
}