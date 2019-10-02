package com.sldevand.youdownload;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sldevand.youdownload.converter.FFmpegConverter;
import com.sldevand.youdownload.receiver.DownloadListenerService;
import com.sldevand.youdownload.service.YtDownloader;

import java.io.File;

public class MainActivity extends RootActivity implements FFmpegConverter.FFMpegConverterOnFinishListener, YtDownloader.OnDownloadStartedListener, DownloadListenerService.OnDownloadCompleteListener {

    private Bundle mSavedInstanceState;

    private ProgressBar preparingProgressBar;
    private ProgressBar downloadingProgressBar;
    private ProgressBar convertingProgressBar;

    private TextView preparingTextView;
    private TextView downloadingTextView;
    private TextView convertingTextView;
    private TextView doneTextView;
    private TextView versionTextView;

    private Button downloadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mSavedInstanceState = savedInstanceState;

        this.preparingProgressBar = findViewById(R.id.preparingProgressBar);
        this.downloadingProgressBar = findViewById(R.id.downloadingProgressBar);
        this.convertingProgressBar = findViewById(R.id.convertingProgressBar);

        this.preparingTextView = findViewById(R.id.preparingTextView);
        this.downloadingTextView = findViewById(R.id.downloadingTextView);
        this.convertingTextView = findViewById(R.id.convertingTextView);
        this.doneTextView = findViewById(R.id.doneTextView);
        this.versionTextView = findViewById(R.id.versionTextView);

        this.downloadButton = findViewById(R.id.downloadImageButton);
        String versionName = "";
        try {
            versionName = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
            this.versionTextView.setText(versionName);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (isStoragePermissionGranted()) {
            this.launchPermittedAction();
        }
    }

    @Override
    protected void launchPermittedAction() {
        super.launchPermittedAction();

        DownloadListenerService dls = new DownloadListenerService();
        dls.setOnDownloadCompleteListener(this);
        IntentFilter intentFilter = new IntentFilter("android.intent.action.DOWNLOAD_COMPLETE");
        this.registerReceiver(dls, intentFilter);

        if (null == this.mSavedInstanceState && Intent.ACTION_SEND.equals(getIntent().getAction())
                && getIntent().getType() != null && "text/plain".equals(getIntent().getType())) {
            handleIntent();
            return;
        }

        this.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText uriEditText = findViewById(R.id.uriEditText);
                String ytLink = uriEditText.getText().toString();

                if (!URLUtil.isValidUrl(ytLink)) {
                    Toast.makeText(
                            MainActivity.this,
                            MainActivity.this.getString(R.string.not_valid_url),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                extract(ytLink);
            }
        });
    }

    private void handleIntent() {
        String ytLink = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        EditText uriEditText = findViewById(R.id.uriEditText);
        uriEditText.setText(ytLink);

        this.extract(ytLink);
    }

    private void extract(String ytLink) {
        if (null == ytLink) {
            Toast.makeText(this, R.string.error_no_yt_link, Toast.LENGTH_LONG).show();
            finish();
        }

        YtDownloader ytDownloader = new YtDownloader(this);
        ytDownloader.setOnDownloadStartedListener(this);
        this.downloadButton.setVisibility(View.GONE);
        this.preparingTextView.setVisibility(View.VISIBLE);
        this.preparingProgressBar.setVisibility(View.VISIBLE);

        ytDownloader.extractYoutubeFile(ytLink);
    }

    @Override
    public void onDownloadStarted() {
        this.downloadingTextView.setVisibility(View.VISIBLE);
        this.downloadingProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void ondownloadComplete(File file) {
        this.downloadingProgressBar.clearAnimation();

        FFmpegConverter fFmpegConverter = new FFmpegConverter(this);
        fFmpegConverter.setOnFinishListener(this);
        fFmpegConverter.convert(file);
        this.convertingTextView.setVisibility(View.VISIBLE);
        this.convertingProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConversionFinished() {
        this.doneTextView.setVisibility(View.VISIBLE);
    }
}
