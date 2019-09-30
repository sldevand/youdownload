package com.sldevand.youdownload.service;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.sldevand.youdownload.MainActivity;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class YtDownloader {
    private static final String TAG = "YtDownloader";

    private static final int ITAG = 18;

    private MainActivity mActivity;

    public YtDownloader(MainActivity _activity) {
        this.mActivity = _activity;
    }

    public void extractYoutubeFile(String youtubeLink) {
        new YouTubeExtractor(this.mActivity) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                if (ytFiles != null) {
                    Log.e(TAG, "onExtractionComplete");
                    for (int i = 0; i < ytFiles.size(); i++) {
                        int key = ytFiles.keyAt(i);
                        Log.e("onCreate", ytFiles.get(key).toString());
                    }

                    YtFile ytFile = ytFiles.get(ITAG);
                    String downloadUrl = ytFile.getUrl();
                    String filename = formatFileName(vMeta.getTitle(), ytFile);

                    downloadFromUrl(downloadUrl, vMeta.getTitle(), filename);
                }
            }
        }.extract(youtubeLink, true, true);
    }

    private String formatFileName(String videoTitle, YtFile ytFile) {
        String filename;
        if (videoTitle.length() > 55) {
            filename = videoTitle.substring(0, 55) + "." + ytFile.getFormat().getExt();
        } else {
            filename = videoTitle + "." + ytFile.getFormat().getExt();
        }
        return filename.replaceAll("[\\\\><\"|*?%:#/]", "");
    }

    private void downloadFromUrl(String youtubeDlUrl, String downloadTitle, String fileName) {
        Toast.makeText(this.mActivity, "Download Started", Toast.LENGTH_SHORT).show();
        Uri uri = Uri.parse(youtubeDlUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(downloadTitle);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        DownloadManager manager = (DownloadManager) this.mActivity.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }
}
