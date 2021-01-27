package com.example.musicplayer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.marcinmoskala.arcseekbar.ArcSeekBar;
import com.marcinmoskala.arcseekbar.ProgressListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;


public class Player extends AppCompatActivity {
    String URL_tobeSaved;
    private FirebaseStorage storage;
    Task<Uri> downloadUrl;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;
    boolean savedImg = false;
    private StorageReference storageReference;
    private Uri selectedImage;
    public static final int GET_FROM_GALLERY = 3;

    TextView song_name, artist_name, duration_start, duration_end;
    ImageView album_art;
    ImageView next_btn;
    ImageView previous_btn;
    ImageView shuffle_btn;
    ImageView repeat_btn;
    ImageView play_pause;
    ImageView heart;
    ArcSeekBar Seek_bar;
    Thread playThread, prevThread, nextThread;
    ImageView add_img_btn;
    Handler handler = new Handler();
    Uri uri;
    int position = -1;
    String Title, Artist, SongID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_layout);

        initialiseData();

        getIntentMethod();

        song_name.setText(Global.Song_List.get(Global.CurrentPosition).getTitle());
        artist_name.setText(Global.Song_List.get(Global.CurrentPosition).getArtist());

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heart.setImageResource(R.drawable.heart1);
            }
        });

        add_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });

        album_art.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });

        shuffle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Global.isShuffleEnabled) {
                    Global.isShuffleEnabled = false;
                    shuffle_btn.setImageResource(R.drawable.shuffle);
                } else {
                    Global.isShuffleEnabled = true;
                    shuffle_btn.setImageResource(R.drawable.shuffle_on);
                }
            }
        });

        repeat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Global.isLoopEnabled) {
                    Global.isLoopEnabled = false;
                    repeat_btn.setImageResource(R.drawable.repeat);
                    Global.mediaPlayer.setLooping(false);
                } else {
                    if (!Global.mediaPlayer.isPlaying()) {
                        Global.mediaPlayer.start();
                        play_pause.setImageResource(R.drawable.pause1);
                    }
                    Global.isLoopEnabled = true;
                    repeat_btn.setImageResource(R.drawable.repeat_on);
                    Global.mediaPlayer.setLooping(true);
                }
            }
        });


        Seek_bar.setOnProgressChangedListener(new ProgressListener() {
                                                  @Override
                                                  public void invoke(int progress) {
                                                      if (Global.mediaPlayer != null) {
                                                          Global.mediaPlayer.seekTo(progress);
                                                      }
                                                  }
                                              }
        );

        handler.postDelayed(runnable, 100);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (Global.mediaPlayer.isPlaying()) {
                //Seek_bar.setProgress(Global.mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 500);
                duration_start.setText(formattedTime(Global.mediaPlayer.getCurrentPosition()));
                duration_end.setText(formattedTime(Global.mediaPlayer.getDuration() - Global.mediaPlayer.getCurrentPosition()));
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
        add_img_btn = (ImageView) findViewById(R.id.add_image_btn);

        //firebase objects
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        userID = fAuth.getCurrentUser().getUid();

        if (Global.isShuffleEnabled)
            shuffle_btn.setImageResource(R.drawable.shuffle_on);
        else
            shuffle_btn.setImageResource(R.drawable.shuffle);

        if (Global.isLoopEnabled)
            repeat_btn.setImageResource(R.drawable.repeat_on);
        else
            repeat_btn.setImageResource(R.drawable.repeat);
    }

    //initialise media player to play son at current position
    public void initialiseMediaPlayer(Boolean isPlaying) {

        uri = Uri.parse(Global.Song_List.get(Global.CurrentPosition).getPath());
        Global.mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        metaData(uri);

        song_name.setText(Global.Song_List.get(Global.CurrentPosition).getTitle());
        artist_name.setText(Global.Song_List.get(Global.CurrentPosition).getArtist());
        Seek_bar.setMaxProgress(Global.mediaPlayer.getDuration());
        Seek_bar.setProgress(0);
        duration_start.setText(formattedTime(0));
        duration_end.setText(formattedTime(Global.mediaPlayer.getDuration()));
        handler.postDelayed(runnable, 500);

        if (isPlaying) {
            Global.mediaPlayer.start();
            play_pause.setImageResource(R.drawable.pause1);
        } else {
            play_pause.setImageResource(R.drawable.play1);
        }

        Global.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mp != null && !Global.isLoopEnabled && !Global.isShuffleEnabled) {
                    play_pause.setImageResource(R.drawable.play1);
                    mp.pause();
                } else if (mp != null && Global.isShuffleEnabled && !Global.isLoopEnabled) {
                    mp.stop();
                    mp.release();
                    Global.CurrentPosition = getRandom(Global.Song_List.size() - 1);
                    play_pause.setImageResource(R.drawable.pause1);
                    initialiseMediaPlayer(true);
                }
            }
        });
    }

    public int getPosition() {
        if (Global.isShuffleEnabled && !Global.isLoopEnabled) {
            Global.CurrentPosition = getRandom(Global.Song_List.size() - 1);
        } else if (!Global.isShuffleEnabled && !Global.isLoopEnabled) {
            Global.CurrentPosition = ((Global.CurrentPosition - 1) < 0 ? (Global.Song_List.size() - 1) : (Global.CurrentPosition - 1));
        }
        return Global.CurrentPosition;
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
    public void metaData(final Uri uri) {

        SongID = Global.Song_List.get(Global.CurrentPosition).getSongID();

        DocumentReference documentReference = fStore.collection("users").document(userID).collection("Songs").document(SongID);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //Toast.makeText(getApplicationContext(), "Document exists!", Toast.LENGTH_LONG).show();
                        Glide.with(getApplicationContext()).asBitmap().load(document.get("URL")).into(album_art);
                    }
                    else {
                        //Toast.makeText(getApplicationContext(), "Document does not exist!", Toast.LENGTH_LONG).show();
                        final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(uri.toString());
                        int durationTotal = Integer.parseInt(Global.Song_List.get(Global.CurrentPosition).getDuration()) / 1000;
                        duration_end.setText(formattedTime(durationTotal));

                        byte[] art = retriever.getEmbeddedPicture();

                        if (art != null)
                            Glide.with(getApplicationContext()).asBitmap().load(art).into(album_art);
                        else
                            Glide.with(getApplicationContext()).asBitmap().load(R.drawable.icon).into(album_art);
                    }
                }
                else {       //'get' task is unsuccessful
                    Toast.makeText(getApplicationContext(), "Failed with task", Toast.LENGTH_LONG).show();

                }
            }
        });

    }

    public void getIntentMethod() {

        position = getIntent().getIntExtra("position", -1);

        if(position != Global.CurrentPosition){

            Global.CurrentPosition = position;
            if (Global.Song_List != null) {
                play_pause.setImageResource(R.drawable.pause1);
                uri = Uri.parse(Global.Song_List.get(Global.CurrentPosition).getPath());
            }

            if (Global.mediaPlayer != null) {
                Global.mediaPlayer.stop();
                Global.mediaPlayer.release();
            }

            Global.mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            Global.mediaPlayer.start();

        }
        else{
            Global.CurrentPosition = position;
            uri = Uri.parse(Global.Song_List.get(Global.CurrentPosition).getPath());

            if(!Global.mediaPlayer.isPlaying() && Global.mediaPlayer.getCurrentPosition() > 1)          //media player is paused
                play_pause.setImageResource(R.drawable.play1);
            else
                play_pause.setImageResource(R.drawable.pause1);
        }

        Seek_bar.setMaxProgress(Global.mediaPlayer.getDuration());
        Seek_bar.setProgress(0);
        metaData(uri);
        Global.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mp != null && !Global.isLoopEnabled && !Global.isShuffleEnabled) {
                    play_pause.setImageResource(R.drawable.play1);
                    mp.pause();
                        /*mp.release();
                        mp = null;*/
                } else if (mp != null && Global.isShuffleEnabled) {
                    mp.stop();
                    mp.release();
                    Global.CurrentPosition = getRandom(Global.Song_List.size() - 1);
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
        if (Global.mediaPlayer.isPlaying()) {
            Global.mediaPlayer.stop();
            Global.mediaPlayer.release();
            Global.CurrentPosition = getPosition();
            initialiseMediaPlayer(true);
        } else {
            Global.mediaPlayer.stop();
            Global.mediaPlayer.release();
            Global.CurrentPosition = getPosition();
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
        if (Global.mediaPlayer.isPlaying()) {
            Global.mediaPlayer.stop();
            Global.mediaPlayer.release();
            if (Global.isShuffleEnabled && !Global.isLoopEnabled) {
                Global.CurrentPosition = getRandom(Global.Song_List.size() - 1);
            } else if (!Global.isShuffleEnabled && !Global.isLoopEnabled) {
                Global.CurrentPosition = ((Global.CurrentPosition + 1) % Global.Song_List.size());
            }
            initialiseMediaPlayer(true);
        } else {
            Global.mediaPlayer.stop();
            Global.mediaPlayer.release();
            if (Global.isShuffleEnabled && !Global.isLoopEnabled) {
                Global.CurrentPosition = getRandom(Global.Song_List.size() - 1);
            } else if (!Global.isShuffleEnabled && !Global.isLoopEnabled) {
                Global.CurrentPosition = ((Global.CurrentPosition + 1) % Global.Song_List.size());
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
        if (Global.mediaPlayer.isPlaying()) {
            play_pause.setImageResource(R.drawable.play1);
            Global.mediaPlayer.pause();
        } else {
            play_pause.setImageResource(R.drawable.pause1);
            Global.mediaPlayer.start();
            handler.postDelayed(runnable, 500);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            selectedImage = data.getData();
            album_art.setImageURI(selectedImage);

            uploadPicture();

        }
    }

    private void uploadPicture() {

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Uploading Image...");
        pd.show();
        savedImg = false;

        final String randomKey = UUID.randomUUID().toString();
        StorageReference storageRef = storageReference.child("images/" + randomKey);

        storageRef.putFile(selectedImage)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        pd.dismiss();

                        Toast.makeText(getApplicationContext(), "Image Uploaded.", Toast.LENGTH_LONG).show();

                        savedImg = true;

                        Global.Song_List.get(Global.CurrentPosition).setPersonalize(true);

                        Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String url = uri.toString();

                                Title = Global.Song_List.get(Global.CurrentPosition).getTitle();
                                Artist = Global.Song_List.get(Global.CurrentPosition).getArtist();
                                SongID = Global.Song_List.get(Global.CurrentPosition).getSongID();

                                Toast.makeText(getApplicationContext(), Global.Song_List.get(Global.CurrentPosition).getSongID(), Toast.LENGTH_LONG).show();

                                final Map<String, Object> user = new HashMap<>();
                                user.put("Title", Title);
                                user.put("Artist", Artist);
                                user.put("URL", url);

                                DocumentReference documentReference = fStore.collection("users").document(userID).collection("Songs").document(SongID);
                                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(Player.this, "Data saved successfully", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }

                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        pd.dismiss();
                        Toast.makeText(getApplicationContext(), "Image failed to Uploaded", Toast.LENGTH_LONG).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {

            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pd.setMessage("Progress: " + (int) progressPercent + "%");
            }

        });
    }


    @Override
    public void finish() //animation of screen
    {
        //Global.CurrentPosition = position;
        super.finish();
        overridePendingTransition(R.anim.no_animation, R.anim.slide_down);
    }

}