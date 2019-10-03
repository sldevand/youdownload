package com.sldevand.youdownload;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sldevand.youdownload.converter.FFmpegConverter;
import com.sldevand.youdownload.receiver.DownloadListenerService;
import com.sldevand.youdownload.service.YtDownloader;

import java.io.File;

public class MainActivity extends RootActivity implements FFmpegConverter.FFMpegConverterOnFinishListener, YtDownloader.OnDownloadStartedListener, DownloadListenerService.OnDownloadCompleteListener {

    private Bundle mSavedInstanceState;

    private ProgressBar statusProgressBar;
    private TextView statusTextView;
    private ImageView doneImageView;

    private Button downloadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mSavedInstanceState = savedInstanceState;

        this.statusProgressBar = findViewById(R.id.statusProgressBar);
        this.statusTextView = findViewById(R.id.statusTextView);
        this.downloadButton = findViewById(R.id.downloadImageButton);
        this.doneImageView = findViewById(R.id.doneImageView);

        displayVersionTextView();

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
        this.downloadButton.setVisibility(View.GONE);
        this.statusProgressBar.setVisibility(View.VISIBLE);
        this.statusTextView.setVisibility(View.VISIBLE);
        this.statusTextView.setText(getString(R.string.preparing));

        YtDownloader ytDownloader = new YtDownloader(this);
        ytDownloader.setOnDownloadStartedListener(this);
        ytDownloader.extractYoutubeFile(ytLink);
    }

    private void displayVersionTextView() {
        TextView versionTextView = findViewById(R.id.versionTextView);
        try {
            String versionName = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
            versionTextView.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDownloadStarted() {
        this.statusTextView.setText(getString(R.string.downloading));
    }

    @Override
    public void ondownloadComplete(File file) {
        FFmpegConverter fFmpegConverter = new FFmpegConverter(this);
        fFmpegConverter.setOnFinishListener(this);
        fFmpegConverter.convert(file);

        this.statusTextView.setText(getString(R.string.converting));
    }

    @Override
    public void onConversionFinished() {
        this.statusProgressBar.setVisibility(View.INVISIBLE);
        this.statusTextView.setText(getString(R.string.done));
        this.statusTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
        this.doneImageView.setVisibility(View.VISIBLE);
    }
}
