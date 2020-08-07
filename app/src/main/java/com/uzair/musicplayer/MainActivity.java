package com.uzair.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 10;
    private ListView musicList;
    private TextView noItemFound;
    private SongListAdapter adapter;
    private static List<SongModel> mySongList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        // if permisson is granted then load the music else request permission
        if (!checkPermission()) {
            requestPermission();
        } else {
            LoadMusicFromMemory musicFromMemory = new LoadMusicFromMemory();
            musicFromMemory.execute();
        }

        // add click on list view item
        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SongModel model = mySongList.get(position);

                // Starting new intent
                Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                intent.putExtra("path", model.getaPath());
                intent.putExtra("title", model.getaName());
                setResult(Activity.RESULT_OK, intent);
                // Closing PlayListView
                finish();

            }
        });

    }

    private void initViews() {
        musicList = findViewById(R.id.songList);
        noItemFound = findViewById(R.id.noItemFound);


        mySongList = new ArrayList<>();
        adapter = new SongListAdapter(mySongList);


    }

    public static List<SongModel> getMySongList()
    {
        return mySongList;
    }


    // check permission
    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {

            return true;
        }

        return false;
    }
// request permission
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }


    // on request permission result whether accept or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    LoadMusicFromMemory musicFromMemory = new LoadMusicFromMemory();
                    musicFromMemory.execute();
                } else {
                    Toast.makeText(this, "Please enable permission to load the music", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            default: {
                break;
            }

        }

    }


    // backgorund task to load data from memory
    class LoadMusicFromMemory extends AsyncTask<Void, Void, Void> {
        ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Loading your playlist...");
            pd.setCanceledOnTouchOutside(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pd.dismiss();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Uri externalUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Uri internalUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;

            String[] projection = {MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.TITLE,
                    MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.AudioColumns.ARTIST};

            Cursor internalCursor = getContentResolver().query(internalUri, projection, null, null, null);
            Cursor externalCursor = getContentResolver().query(externalUri, projection, null, null, null);

            Cursor musicCursor = new MergeCursor(new Cursor[]{internalCursor, externalCursor});


            if (musicCursor != null) {
                while (musicCursor.moveToNext()) {

                    String path = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATA));
                    String title = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE));
                    String album = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM));
                    String artist = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST));

                    mySongList.add(new SongModel(path, title, album, artist));

                }

                musicCursor.close();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        noItemFound.setVisibility(View.GONE);
                        musicList.setAdapter(adapter);
                    }
                });

            } else {
                Toast.makeText(MainActivity.this, "No Items found", Toast.LENGTH_SHORT).show();
            }


            return null;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!checkPermission()) {
            requestPermission();
        }
    }

}
