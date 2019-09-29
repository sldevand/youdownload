package com.sldevand.youdownload.converter;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler;
import nl.bravobit.ffmpeg.FFmpeg;

public class FFmpegConverter {

    private static final String TAG = "FFmpegConverter";
    private Context mContext;

    public FFmpegConverter(Context context) {
        this.mContext = context;
    }

    public void convert(File file) {
        final FFmpeg ffmpeg = FFmpeg.getInstance(this.mContext);

        if (FFmpeg.getInstance(this.mContext).isSupported()) {
            Log.e(TAG, "FFMpeg is supported");
        } else {
            Log.e(TAG, "FFMpeg is NOT supported");
            return;
        }

        execute(ffmpeg,file);

    }

    private void execute(FFmpeg ffmpeg, File file) {
        final String absolutePath = file.getAbsolutePath();
        final String noExtension = FilenameUtils.removeExtension(absolutePath);

        List<String> commandList = new LinkedList<>();
        commandList.add("-i");
        commandList.add(absolutePath);
        commandList.add(noExtension + ".mp3");

        String[] cmd = commandList.toArray(new String[commandList.size()]);

        ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

            @Override
            public void onStart() {
                Log.e(TAG, "FFMpeg started");
            }

            @Override
            public void onProgress(String message) {
                Log.e(TAG, "FFMpeg progress = " + message);
                //TODO calculate progress for ProgressBar
            }

            @Override
            public void onFailure(String message) {
                Log.e(TAG, "FFMpeg failure = " + message);
            }


            @Override
            public void onSuccess(String message) {
                Toast.makeText(
                        mContext,
                        "Successfully converted " + absolutePath + "\n into " + noExtension + ".mp3",
                        Toast.LENGTH_SHORT
                ).show();
            }

            @Override
            public void onFinish() {
                File fdelete = new File(absolutePath);
                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        System.out.println("file Deleted :" + absolutePath);
                    } else {
                        System.out.println("file not Deleted :" + absolutePath);
                    }
                }
            }
        });
    }
}
