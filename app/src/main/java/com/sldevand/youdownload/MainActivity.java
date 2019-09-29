package com.sldevand.youdownload;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

        Button downloadButton = findViewById(R.id.downloadImageButton);
        if (null == this.mSavedInstanceState && Intent.ACTION_SEND.equals(getIntent().getAction())
                && getIntent().getType() != null && "text/plain".equals(getIntent().getType())) {

            handleIntent();
            return;
        }

        downloadButton.setEnabled(true);
        downloadButton.setOnClickListener(new View.OnClickListener() {
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

        Toast.makeText(this, ytLink, Toast.LENGTH_LONG).show();
        YtDownloader ytDownloader = new YtDownloader(this);
        ytDownloader.extractYoutubeFile(ytLink);
    }
}
