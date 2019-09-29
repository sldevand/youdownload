package com.sldevand.youdownload.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.sldevand.youdownload.converter.FFmpegConverter;

import java.io.File;


public class DownloadListenerService extends BroadcastReceiver {
    private static final String TAG = "DownloadListenerService";

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            Bundle b = intent.getExtras();
            long fileId = b.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);

            Uri downloadUri = Uri.parse("content://downloads/public_downloads");

            Uri downloadUriAppendId = ContentUris.withAppendedId(downloadUri, fileId);
            String realPath = getImageRealPath(context.getContentResolver(), downloadUriAppendId, null);

            Toast.makeText(context, "Download Complete, file = " + realPath, Toast.LENGTH_SHORT).show();

            File file = new File(realPath);
            FFmpegConverter fFmpegConverter = new FFmpegConverter(context);
            fFmpegConverter.convert(file);

        }
    }

    /* Return uri represented document file real local path.*/
    private String getImageRealPath(ContentResolver contentResolver, Uri uri, String whereClause)
    {
        String ret = "";

        // Query the uri with condition.
        Cursor cursor = contentResolver.query(uri, null, whereClause, null, null);

        if(cursor!=null)
        {
            boolean moveToFirst = cursor.moveToFirst();
            if(moveToFirst)
            {

                // Get columns name by uri type.
                String columnName = MediaStore.Images.Media.DATA;

                if( uri==MediaStore.Images.Media.EXTERNAL_CONTENT_URI )
                {
                    columnName = MediaStore.Images.Media.DATA;
                }else if( uri==MediaStore.Audio.Media.EXTERNAL_CONTENT_URI )
                {
                    columnName = MediaStore.Audio.Media.DATA;
                }else if( uri==MediaStore.Video.Media.EXTERNAL_CONTENT_URI )
                {
                    columnName = MediaStore.Video.Media.DATA;
                }

                // Get column index.
                int imageColumnIndex = cursor.getColumnIndex(columnName);

                // Get column value which is the uri related file local path.
                ret = cursor.getString(imageColumnIndex);
            }
        }

        return ret;
    }
}