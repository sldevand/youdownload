package com.sldevand.youdownload;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class RootActivity extends AppCompatActivity {

    private static final String TAG = "ROOTACTIVITY";
    private static final int REQUEST = 112;

    private Context mContext = RootActivity.this;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e(TAG, "onRequestPermissionsResult " + requestCode);

        switch (requestCode) {
            case REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Permission is granted!");
                    this.launchPermittedAction();
                } else {
                    Toast.makeText(mContext, "The app was not allowed to write in your storage", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public boolean isStoragePermissionGranted() {
        String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= 23) {
            if (hasPermissions(mContext, PERMISSIONS)) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this,PERMISSIONS, REQUEST);
                return false;
            }
        }

        return true;
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void launchPermittedAction(){
        // Intentionally empty statement
    }
}
