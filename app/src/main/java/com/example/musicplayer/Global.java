package com.example.musicplayer;

import android.app.Application;
import android.media.MediaPlayer;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class Global extends Application {

    static FirebaseAuth fAuth = FirebaseAuth.getInstance();
    static public ArrayList<Music> Song_List = new ArrayList<>();
    static boolean isShuffleEnabled = false;
    static boolean isLoopEnabled = false;
    static MediaPlayer mediaPlayer;
    static  boolean mp_opened = false;
    static int CurrentPosition = 1;
}
