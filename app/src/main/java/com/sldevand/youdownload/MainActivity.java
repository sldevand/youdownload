package com.sldevand.youdownload;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.youdownload.R;
import com.sldevand.youdownload.service.YtDownloader;

public class MainActivity extends RootActivity {

    private Bundle mSavedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mSavedInstanceState = savedInstanceState;

        if (isStoragePermissionGranted()) {
            this.launchPermittedAction();
        }
    }

    @Override
    protected void launchPermittedAction() {
        super.launchPermittedAction();

        if (null == this.mSavedInstanceState && Intent.ACTION_SEND.equals(getIntent().getAction())
                && getIntent().getType() != null && "text/plain".equals(getIntent().getType())) {
            handleIntent();
        }


        ImageButton downloadButton = findViewById(R.id.downloadImageButton);

    }

    private void handleIntent() {

        String ytLink = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        EditText uriEditText = findViewById(R.id.uriEditText);
        VideoView videoView = findViewById(R.id.youtubeVideoView);
        uriEditText.setText(ytLink);
        videoView.setVideoURI(Uri.parse(ytLink));

        if (null == ytLink) {
            Toast.makeText(this, R.string.error_no_yt_link, Toast.LENGTH_LONG).show();
            finish();
        }

        Toast.makeText(this, ytLink, Toast.LENGTH_LONG).show();
        YtDownloader ytDownloader = new YtDownloader(this);
        ytDownloader.extractYoutubeFile(ytLink);
    }
}
