package com.sldevand.youdownload.converter;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler;
import nl.bravobit.ffmpeg.FFmpeg;

public class FFmpegConverter {

    private static final String TAG = "FFmpegConverter";
    private Context mContext;

    private FFMpegConverterOnFinishListener onFinishListener;

    public interface FFMpegConverterOnFinishListener {
        void onConversionFinished();
    }

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

        execute(ffmpeg, file);
    }

    private void execute(FFmpeg ffmpeg, File file) {
        final String sourceAbsolutePath = file.getAbsolutePath();
        final String noExtension = FilenameUtils.removeExtension(sourceAbsolutePath);
        final String targetExtension = "aac";
        final String targetAbsolutePath = noExtension + "." + targetExtension;

        String[] cmd = {"-y", "-i", sourceAbsolutePath, "-vn", "-acodec", "copy", targetAbsolutePath};
        ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

            @Override
            public void onStart() {
                Log.e(TAG, "FFMpeg started");
            }

            @Override
            public void onProgress(String message) {
                Log.e(TAG, "FFMpeg progress = " + message);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(
                        mContext,
                        "FFMpeg failure = " + message,
                        Toast.LENGTH_LONG
                ).show();
                Log.e(TAG, "FFMpeg failure = " + message);
            }


            @Override
            public void onSuccess(String message) {
                Toast.makeText(
                        mContext,
                        "Successfully converted " + sourceAbsolutePath + "\n into " + targetAbsolutePath,
                        Toast.LENGTH_LONG
                ).show();

                Log.e(TAG, "Successfully converted " + sourceAbsolutePath + "\n into " + targetAbsolutePath);
            }

            @Override
            public void onFinish() {
                File fdelete = new File(sourceAbsolutePath);
                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        System.out.println("file Deleted :" + sourceAbsolutePath);
                    } else {
                        System.out.println("file not Deleted :" + sourceAbsolutePath);
                    }
                }

                if (null != onFinishListener) {
                    onFinishListener.onConversionFinished();
                }
            }
        });
    }

    public void setOnFinishListener(FFMpegConverterOnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }
}
