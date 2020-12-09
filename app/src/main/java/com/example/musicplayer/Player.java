package com.example.musicplayer;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.marcinmoskala.arcseekbar.ArcSeekBar;
import com.marcinmoskala.arcseekbar.ProgressListener;
import java.util.ArrayList;
import java.util.Random;

import static com.example.musicplayer.MainActivity.arrayList;

public class Player extends AppCompatActivity {
    TextView song_name, artist_name, duration_start, duration_end;
    ImageView album_art;
    ImageView next_btn;
    ImageView previous_btn;
    ImageView shuffle_btn;
    ImageView repeat_btn;
    ImageView play_pause;
    ImageView heart;
    ArcSeekBar Seek_bar;
    static MediaPlayer mediaPlayer;
    Thread playThread, prevThread, nextThread;
    Handler handler = new Handler();
    Uri uri;
    int position = -1;
    boolean isShuffleEnabled = false;
    boolean isLoopEnabled = false;
    static ArrayList<Music> songList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_layout);

        initialiseData();

        getIntentMethod();
        song_name.setText(songList.get(position).getTitle());
        artist_name.setText(songList.get(position).getArtist());

        heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heart.setImageResource(R.drawable.heart1);
            }
        });

        shuffle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShuffleEnabled) {
                    isShuffleEnabled = false;
                    shuffle_btn.setImageResource(R.drawable.shuffle);
                } else {
                    isShuffleEnabled = true;
                    shuffle_btn.setImageResource(R.drawable.shuffle_on);
                }
            }
        });

        repeat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoopEnabled) {
                    isLoopEnabled = false;
                    repeat_btn.setImageResource(R.drawable.repeat);
                    mediaPlayer.setLooping(false);
                } else {
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                        play_pause.setImageResource(R.drawable.pause1);
                    }
                    isLoopEnabled = true;
                    repeat_btn.setImageResource(R.drawable.repeat_on);
                    mediaPlayer.setLooping(true);
                }
            }
        });


        Seek_bar.setOnProgressChangedListener(new ProgressListener() {
                                                  @Override
                                                  public void invoke(int progress) {
                                                      if (mediaPlayer != null) {
                                                          mediaPlayer.seekTo(progress);
                                                      }
                                                  }
                                              }
        );

        handler.postDelayed(runnable, 100);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer.isPlaying()) {
                Seek_bar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 500);
                duration_start.setText(formattedTime(mediaPlayer.getCurrentPosition()));
                duration_end.setText(formattedTime(mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition()));
            }
        }
    };

    //construct variables
    public void initialiseData() {
        song_name = (TextView) findViewById(R.id.name);
        artist_name = (TextView) findViewById(R.id.artist);
        duration_start = (TextView) findViewById(R.id.duration_start);
        duration_end = (TextView) findViewById(R.id.duration_end);
        album_art = (ImageView) findViewById(R.id.album_art);
        next_btn = (ImageView) findViewById(R.id.next_btn);
        previous_btn = (ImageView) findViewById(R.id.previous_btn);
        shuffle_btn = (ImageView) findViewById(R.id.shuffle_btn);
        repeat_btn = (ImageView) findViewById(R.id.repeat_btn);
        play_pause = (ImageView) findViewById(R.id.play_pause);
        heart = (ImageView) findViewById(R.id.heart);
        Seek_bar = (ArcSeekBar) findViewById(R.id.arc_seek_bar);
    }

    //initialise media player to play son at current position
    public void initialiseMediaPlayer(Boolean isPlaying) {
        uri = Uri.parse(songList.get(position).getPath());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        metaData(uri);
        song_name.setText(songList.get(position).getTitle());
        artist_name.setText(songList.get(position).getArtist());
        Seek_bar.setMaxProgress(mediaPlayer.getDuration());
        Seek_bar.setProgress(0);
        duration_start.setText(formattedTime(0));
        duration_end.setText(formattedTime(mediaPlayer.getDuration()));
        handler.postDelayed(runnable, 500);

        if (isPlaying) {
            mediaPlayer.start();
            play_pause.setImageResource(R.drawable.pause1);
        } else {
            play_pause.setImageResource(R.drawable.play1);
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mp != null && !isLoopEnabled && !isShuffleEnabled) {
                    play_pause.setImageResource(R.drawable.play1);
                    mp.pause();
                } else if (mp != null && isShuffleEnabled && !isLoopEnabled) {
                    mp.stop();
                    mp.release();
                    position = getRandom(songList.size() - 1);
                    play_pause.setImageResource(R.drawable.pause1);
                    initialiseMediaPlayer(true);
                }
            }
        });
    }

    public int getPosition() {
        if (isShuffleEnabled && !isLoopEnabled) {
            position = getRandom(songList.size() - 1);
        } else if (!isShuffleEnabled && !isLoopEnabled) {
            position = ((position - 1) < 0 ? (songList.size() - 1) : (position - 1));
        }
        return position;
    }

    public String formattedTime(int CurrentPosition) {
        String totalOut;
        String totalNew;
        String seconds = String.valueOf(CurrentPosition / 1000 % 60);
        String minutes = String.valueOf(CurrentPosition / 1000 / 60);
        totalOut = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;
        if (seconds.length() == 1) {
            return totalNew;
        } else {
            return totalOut;
        }
    }

    //for album art
    public void metaData(Uri uri) {
        final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal = Integer.parseInt(songList.get(position).getDuration()) / 1000;
        duration_end.setText(formattedTime(durationTotal));

        byte[] art = retriever.getEmbeddedPicture();

        if (art != null) {

            Glide.with(getApplicationContext()).asBitmap().load(art).into(album_art);
        } else {

            Glide.with(getApplicationContext()).asBitmap().load(R.drawable.icon).into(album_art);
        }


    }

    public void getIntentMethod() {
        position = getIntent().getIntExtra("position", -1);
        songList = arrayList;
        if (songList != null) {
            play_pause.setImageResource(R.drawable.pause1);
            uri = Uri.parse(songList.get(position).getPath());
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();
        Seek_bar.setMaxProgress(mediaPlayer.getDuration());
        Seek_bar.setProgress(0);
        metaData(uri);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mp != null && !isLoopEnabled && !isShuffleEnabled) {
                    play_pause.setImageResource(R.drawable.play1);
                    mp.pause();
                        /*mp.release();
                        mp = null;*/
                } else if (mp != null && isShuffleEnabled) {
                    mp.stop();
                    mp.release();
                    position = getRandom(songList.size() - 1);
                    initialiseMediaPlayer(true);
                }
            }
        });
    }


    @Override
    protected void onResume() {
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        super.onResume();
    }

    private void prevThreadBtn() {
        prevThread = new Thread() {
            @Override
            public void run() {
                super.run();
                previous_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prevBtnClicked();
                    }


                });
            }
        };
        prevThread.start();
    }

    private void prevBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            position = getPosition();
            initialiseMediaPlayer(true);
        } else {
            mediaPlayer.stop();
            mediaPlayer.release();
            position = getPosition();
            initialiseMediaPlayer(false);
        }
    }

    private void nextThreadBtn() {
        nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                next_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnClicked();
                    }


                });
            }
        };
        nextThread.start();
    }

    private void nextBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (isShuffleEnabled && !isLoopEnabled) {
                position = getRandom(songList.size() - 1);
            } else if (!isShuffleEnabled && !isLoopEnabled) {
                position = ((position + 1) % songList.size());
            }
            initialiseMediaPlayer(true);
        } else {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (isShuffleEnabled && !isLoopEnabled) {
                position = getRandom(songList.size() - 1);
            } else if (!isShuffleEnabled && !isLoopEnabled) {
                position = ((position + 1) % songList.size());
            }
            initialiseMediaPlayer(false);
        }

    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    private void playThreadBtn() {
        playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                play_pause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnClicked();
                    }


                });
            }
        };
        playThread.start();
    }

    private void playPauseBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            play_pause.setImageResource(R.drawable.play1);
            mediaPlayer.pause();
        } else {
            play_pause.setImageResource(R.drawable.pause1);
            mediaPlayer.start();
            handler.postDelayed(runnable, 500);
        }
    }

    @Override
    public void finish()//animation of screen
    {
        super.finish();
        overridePendingTransition(R.anim.no_animation, R.anim.slide_down);
    }

}