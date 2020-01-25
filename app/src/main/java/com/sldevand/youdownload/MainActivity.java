package com.sldevand.youdownload;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sldevand.youdownload.activity.SettingsActivity;
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

    private DownloadListenerService dls;
    private IntentFilter dlsIntentFilter;
    private String mOutputFolder;

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

        if (!isStoragePermissionGranted()) {
            return;
        }

        this.init();
        this.launchPermittedAction();

    }

    private void init() {
        SharedPreferences sharedPref = getSharedPreferences(SettingsActivity.PREFS_KEY, Context.MODE_PRIVATE);


        if (sharedPref.contains(getString(R.string.outputFolderKey))) {
            Log.e("SharedPreferences", "contains " + getString(R.string.outputFolderKey));
        } else {
            Log.e("SharedPreferences", "doesn't contain " + getString(R.string.outputFolderKey));
        }


        this.mOutputFolder = sharedPref.getString(getString(R.string.outputFolderKey), "");
        assert this.mOutputFolder != null;
        if (this.mOutputFolder.isEmpty()) {
            this.mOutputFolder = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DOWNLOADS + "/";
        }

        dls = new DownloadListenerService();
        dls.setOnDownloadCompleteListener(this);
        dlsIntentFilter = new IntentFilter("android.intent.action.DOWNLOAD_COMPLETE");

        EditText outputFolderEditText = findViewById(R.id.outputFolderEditText);
        outputFolderEditText.setText(this.mOutputFolder);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (null != dls) {
            this.registerReceiver(dls, dlsIntentFilter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != dls) {
            this.unregisterReceiver(dls);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent prefIntent = new Intent(this, SettingsActivity.class);
            startActivity(prefIntent);
        }

        return true;
    }

    @Override
    protected void launchPermittedAction() {
        super.launchPermittedAction();

        if (null == this.mSavedInstanceState && Intent.ACTION_SEND.equals(getIntent().getAction())
                && getIntent().getType() != null && "text/plain".equals(getIntent().getType())) {
            handleIntent();
            return;
        }

        this.downloadButton.setOnClickListener(view -> {
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
            String versionName = getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(getApplicationContext().getPackageName(), 0)
                    .versionName;
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
        FFmpegConverter fFmpegConverter = new FFmpegConverter(this, this.mOutputFolder);
        fFmpegConverter.setOnFinishListener(this);
        fFmpegConverter.convert(file);

        this.statusTextView.setText(getString(R.string.converting));
    }

    @Override
    public void onConversionFinished(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        this.statusProgressBar.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onConversionError(String message) {

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        this.doneImageView.setVisibility(View.VISIBLE);
        this.statusTextView.setText(getString(R.string.error));
        this.statusTextView.setTextColor(getResources().getColor(R.color.colorAccent));
        this.doneImageView.setImageResource(R.mipmap.ic_error);

    }

    @Override
    public void onConversionSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        this.statusTextView.setText(getString(R.string.done));
        this.statusTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
        this.doneImageView.setVisibility(View.VISIBLE);

    }
}
