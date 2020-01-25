package com.sldevand.youdownload.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.sldevand.youdownload.R;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    public static int PICK_DIRECTORY_RESULT_CODE = 9999;
    public static String PREFS_KEY = "app_prefs";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean onPreferenceTreeClick(Preference preference) {

            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivityForResult(
                    Intent.createChooser(intent, "Choose directory"),
                    PICK_DIRECTORY_RESULT_CODE
            );

            return super.onPreferenceTreeClick(preference);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            assert data != null;
            if (requestCode == PICK_DIRECTORY_RESULT_CODE && resultCode == RESULT_OK) {
                Uri treeUri = data.getData();

                String path = getRealPathFromURI(treeUri, getActivity());

                this.writeOutputFolderPathPreference(path);
            }
        }


        private void writeOutputFolderPathPreference(String outputFolderPath) {
            SharedPreferences sharedPref = Objects.requireNonNull(getActivity()).getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.outputFolderKey), outputFolderPath);
            editor.apply();
        }
    }

    public static String getRealPathFromURI(Uri contentUri, Context activity) {
        String result = "";
        boolean isok = false;
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = activity.getContentResolver().query(contentUri, proj,
                    null, null, null);
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
            isok = true;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return isok ? result : "";
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}