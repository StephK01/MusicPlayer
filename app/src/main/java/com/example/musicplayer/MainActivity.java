package com.example.musicplayer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.api.Authentication;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.rpc.context.AttributeContext;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_REQUEST = 1;
    MusicAdapter adapter;
    ListView listView;
    ImageView bar_play;
    TextView Song;
    TextView Artist;
    RelativeLayout bar;
    DrawerLayout drawerLayout;
    TextView userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bar_play = (ImageView) findViewById(R.id.bar_play_pause);
        Song = (TextView) findViewById(R.id.song_name);
        Artist = (TextView) findViewById(R.id.textView2);
        userEmail = (TextView) findViewById(R.id.user_email);
        bar = (RelativeLayout) findViewById(R.id.musicplayer_bar);
        drawerLayout = findViewById(R.id.drawer_layout);

        userEmail.setText(Global.fAuth.getCurrentUser().getEmail());

        if (!Global.mp_opened)
            bar.setVisibility(View.GONE);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
        } else {
            doStuff();
        }

        bar_play.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Global.mediaPlayer.isPlaying() && Global.mediaPlayer.getCurrentPosition() > 1) {         //media player is paused
                    Global.mediaPlayer.start();
                    bar_play.setImageResource(R.drawable.pause1);
                } else {
                    Global.mediaPlayer.pause();
                    bar_play.setImageResource(R.drawable.play1);
                }
            }

        });

        bar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Player.class);
                intent.putExtra("position", Global.CurrentPosition);
                startActivity(intent);

                overridePendingTransition(R.anim.slide_up, R.anim.no_animation);

            }
        });
    }

    public void ClickMenu(View view) {
        openDrawer(drawerLayout);
    }

    private static void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void ClickLogout(View view) {
        logout(this);
    }

    private void logout(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Global.fAuth.signOut();
                Global.Song_List.clear();
                finish();
                startActivity(new Intent(MainActivity.this, Login.class));

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onSupportActionModeFinished(@NonNull ActionMode mode) {
        super.onSupportActionModeFinished(mode);
    }

    public void doStuff() {
        listView = (ListView) findViewById(R.id.list_view);
        getMusic();
        adapter = new MusicAdapter(this, Global.Song_List);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                //opens music player

                                                Intent intent = new Intent(MainActivity.this, Player.class);
                                                intent.putExtra("position", position);
                                                Global.mp_opened = true;
                                                startActivity(intent);

                                                overridePendingTransition(R.anim.slide_up, R.anim.no_animation);
                                            }

                                            ;


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
                Music music = new Music(currentData, currentTitle, currentArtist, currentAlbum, currentDuration);

                Global.Song_List.add(music);
            } while (songCursor.moveToNext());
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSION_REQUEST) {
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
                    doStuff();
                } else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Global.mp_opened) {
            if (Global.Song_List.get(Global.CurrentPosition).getTitle().length() > 33)
                Song.setText(Global.Song_List.get(Global.CurrentPosition).getTitle().substring(0, 33) + "...");
            else
                Song.setText(Global.Song_List.get(Global.CurrentPosition).getTitle());

            Artist.setText(Global.Song_List.get(Global.CurrentPosition).getArtist());

            bar.setVisibility(View.VISIBLE);


            if (!Global.mediaPlayer.isPlaying() && Global.mediaPlayer.getCurrentPosition() > 1)          //media player is paused
                bar_play.setImageResource(R.drawable.play1);
            else
                bar_play.setImageResource(R.drawable.pause1);

        } else
            bar.setVisibility(View.GONE);
    }
}