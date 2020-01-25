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
    private String mOutputFolder;

    private FFMpegConverterOnFinishListener onFinishListener;

    public interface FFMpegConverterOnFinishListener {
        void onConversionFinished(String message);

        void onConversionError(String message);

        void onConversionSuccess(String message);
    }

    public FFmpegConverter(Context context, String outputFolder) {
        this.mContext = context;
        this.mOutputFolder = outputFolder;
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
        final String baseName = FilenameUtils.getBaseName(sourceAbsolutePath);
        String targetAbsolutePath = noExtension + "." + targetExtension;

        if (!this.mOutputFolder.isEmpty()) {
            targetAbsolutePath = this.mOutputFolder + baseName + "." + targetExtension;
        }

        final String outputFilePath = targetAbsolutePath;

        String[] cmd = {"-y", "-i", sourceAbsolutePath, "-vn", "-acodec", "copy", outputFilePath};
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
                Log.e(TAG, "FFMpeg failure = " + message);
                if (null != onFinishListener) {
                    onFinishListener.onConversionError(message);
                }
            }


            @Override
            public void onSuccess(String message) {

                if (null != onFinishListener) {
                    onFinishListener.onConversionSuccess("Successfully converted " + sourceAbsolutePath + "\n into " + outputFilePath);
                }

                Log.e(TAG, "Successfully converted " + sourceAbsolutePath + "\n into " + outputFilePath);
            }

            @Override
            public void onFinish() {
                File fdelete = new File(sourceAbsolutePath);
                String message = "Can't delete any file";
                if (fdelete.exists()) {
                    message = "file not deleted :" + sourceAbsolutePath;
                    if (fdelete.delete()) {
                        message = "file deleted :" + sourceAbsolutePath;
                    }
                }

                if (null != onFinishListener) {
                    onFinishListener.onConversionFinished(message);
                }
            }
        });
    }

    public void setOnFinishListener(FFMpegConverterOnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }
}
