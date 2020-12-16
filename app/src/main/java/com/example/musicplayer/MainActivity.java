package com.example.musicplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static  final int MY_PERMISSION_REQUEST = 1;
    MusicAdapter adapter;
    ListView listView;
    ImageView bar_play;
    TextView Song;
    TextView Artist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bar_play = (ImageView) findViewById(R.id.bar_play_pause);
        Song = (TextView) findViewById(R.id.textView);
        Artist = (TextView) findViewById(R.id.textView2);

        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST) ;
        }
        else {
            doStuff();
        }

        bar_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Global.mediaPlayer.isPlaying() && Global.mediaPlayer.getCurrentPosition() > 1) {         //media player is paused
                    Global.mediaPlayer.start();
                    bar_play.setImageResource(R.drawable.pause1);
                }
                else{
                    Global.mediaPlayer.pause();
                    bar_play.setImageResource(R.drawable.play1);
                }
            }

        });
    }

    @Override
    public void onSupportActionModeFinished(@NonNull ActionMode mode) {
        super.onSupportActionModeFinished(mode);
    }

    public void doStuff() {
        listView = (ListView)findViewById(R.id.list_view);
        getMusic();
        adapter = new MusicAdapter(this,Global.Song_List);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //opens music player

                Intent intent=new Intent(MainActivity.this, Player.class);
                intent.putExtra("position",position);
                startActivity(intent);

                overridePendingTransition(R.anim.slide_up,R.anim.no_animation);
                };


        }
    );


    }

    public void getMusic() {
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null, null, null, "TITLE ASC");

        if (songCursor != null && songCursor.moveToFirst()) {
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songAlbum = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int songDuration = songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            do {
                String currentTitle = songCursor.getString(songTitle);
                String currentArtist = songCursor.getString(songArtist);
                String currentAlbum = songCursor.getString(songAlbum);
                String currentData = songCursor.getString(songData);
                String currentDuration = songCursor.getString(songDuration);
                 Music music=new Music(currentData,currentTitle,currentArtist,currentAlbum,currentDuration );

                Global.Song_List.add(music);
            }while (songCursor.moveToNext());
        }
    }

    public void onRequestPermissionsResult(int requestCode, String [] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST: {
                if ((grantResults.length >  0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
                        doStuff();
                    }
                    else {
                        Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    return;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        finishAffinity();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //mp_bar.setVisibility(View.VISIBLE);

        Song.setText(Global.Song_List.get(Global.CurrentPosition).getTitle());
        Artist.setText(Global.Song_List.get(Global.CurrentPosition).getArtist());

        if(Global.mp_opened){
            if(!Global.mediaPlayer.isPlaying() && Global.mediaPlayer.getCurrentPosition() > 1)          //media player is paused
                bar_play.setImageResource(R.drawable.play1);
            else
                bar_play.setImageResource(R.drawable.pause1);
        }
        else
            Toast.makeText(this, "Media Player is not playing", Toast.LENGTH_SHORT).show();

    }
}