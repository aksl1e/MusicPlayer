package com.example.musicplayer;

import static com.example.musicplayer.AlbumSongsAdapter.albumSongs;

import static com.example.musicplayer.MainActivity.allSongs;
import static com.example.musicplayer.MainActivity.isOnRepeat;
import static com.example.musicplayer.MainActivity.isOnShuffle;
import static com.example.musicplayer.PlayerService.audioManager;
import static com.example.musicplayer.PlayerService.focusRequest;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PlayerActivity extends AppCompatActivity implements
        PlayingActions, ServiceConnection {

    TextView song_name, artist_name, timeTotal, timePlayed;
    ImageView cover_art, nextButton, previousButton, shuffleButton, repeatButton;
    FloatingActionButton playPauseButton;
    SeekBar seekBar;

    static Uri current_song_path;

    private final Handler handler = new Handler();

    int position = -1;
    static ArrayList<SongData> player_songs_list = new ArrayList<>();

    PlayerService playerService;
    String intentBy;

    static boolean running = true;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initializeViews();

        position = getIntent().getIntExtra("position", -1);
        intentBy = getIntent().getStringExtra("intentBy");
        boolean isPlaying = getIntent().getBooleanExtra("isPlaying", true);
        boolean fromNotification = getIntent().getBooleanExtra("fromNotification", false);

        if(intentBy != null && intentBy.equals("fromAlbums")){
            player_songs_list = albumSongs;
        } else if(intentBy != null && intentBy.equals("fromSongs")){
            player_songs_list = allSongs;
        }

        if(player_songs_list != null){
            playPauseButton.setImageResource(R.drawable.player_pause);
            current_song_path = Uri.parse(player_songs_list.get(position).getPath());
        }

        if(!isPlaying){
            playPauseButton.setImageResource(R.drawable.player_play_arrow);

            song_name.setSelected(false);
            artist_name.setSelected(false);
        } else {
            song_name.setSelected(true);
            artist_name.setSelected(true);
        }

        if(intentBy != null && !intentBy.equals("fromMainActivity")){
            Intent intent = new Intent(this, PlayerService.class);
            intent.putExtra("servicePosition", position);
            intent.putExtra("fromNotification", fromNotification);
            if (intentBy != null && intentBy.equals("albumDetails")) {
                intent.putExtra("intentBy", "fromAlbums");
            } else if (intentBy != null && intentBy.equals("fromSongs")){
                intent.putExtra("intentBy", "fromSongs");
            }
            startService(intent);
        }


        seekBarSetting(seekBar);
        metaDataSetting(current_song_path);


        shuffleButtonSetListener();
        repeatButtonSetListener();

    }

    private void repeatButtonSetListener() {
        repeatButton.setOnClickListener(view -> {
            if(isOnRepeat){
                isOnRepeat = false;
                repeatButton.setImageResource(R.drawable.player_repeat_off);
            }
            else {
                isOnRepeat = true;
                repeatButton.setImageResource(R.drawable.player_repeat_on);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void shuffleButtonSetListener() {
        shuffleButton.setOnClickListener(view -> {
            if(isOnShuffle){
                isOnShuffle = false;
                shuffleButton.setImageResource(R.drawable.player_shuffle_off);
            }
            else {
                isOnShuffle = true;
                shuffleButton.setImageResource(R.drawable.player_shuffle_on);
            }
        });
    }


    private void seekBarSetting(SeekBar seekBar) {

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(playerService != null && b){
                    playerService.seekTo(i * 1000);
                }

                assert playerService != null;
                if(playerService.isPlaying()){
                    playerService.showNotification(R.drawable.notification_pause, 1F);
                } else {
                    playerService.showNotification(R.drawable.notification_play, 0F);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        PlayerActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(playerService != null){
                    if(running){
                        int mCurrentPosition = playerService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                        timePlayed.setText(formattedTime(mCurrentPosition * 1000));
                    }
                }
                handler.postDelayed(this, 1);
            }
        });
    }

    private void seekBarUpdate(SeekBar seekBar){
        seekBar.setMax(playerService.getDuration() / 1000);

        PlayerActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(playerService != null){
                    if(running){
                        int mCurrentPosition = playerService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                        timePlayed.setText(formattedTime(mCurrentPosition * 1000));
                    }
                }
                handler.postDelayed(this, 1);
            }
        });
    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this, PlayerService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        playThreadButton();
        prevThreadButton();
        nextThreadButton();

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    private void playThreadButton() {
        Thread playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                playPauseButton.setOnClickListener(view -> playPauseButtonClicked());
            }
        };
        playThread.start();
    }

    public void playPauseButtonClicked() {
        if(playerService.isPlaying()){
            playPauseButton.setImageResource(R.drawable.player_play_arrow);
            playerService.pause();

            seekBarUpdate(seekBar);

            playerService.showNotification(R.drawable.notification_play, 0F);

            song_name.setSelected(false);
            artist_name.setSelected(false);
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                    playPauseButton.setImageResource(R.drawable.player_pause);
                    playerService.start();

                    seekBarUpdate(seekBar);

                    if(playerService.isPlaying()){
                        playerService.showNotification(R.drawable.notification_pause, 1F);
                    } else {
                        playerService.showNotification(R.drawable.notification_play, 1F);
                    }

                    song_name.setSelected(true);
                    artist_name.setSelected(true);
                }
            }
        }
    }

    private void prevThreadButton() {
        Thread prevThread = new Thread() {
            @Override
            public void run() {
                super.run();
                previousButton.setOnClickListener(view -> previousButtonClicked());
            }
        };
        prevThread.start();
    }

    public void previousButtonClicked() {
        if(playerService.isPlaying()){
            playerService.stop();
            playerService.release();
            position = ((position - 1 < 0 ? (player_songs_list.size() - 1) : (position - 1)));
            current_song_path = Uri.parse(player_songs_list.get(position).getPath());

            playerService.createMediaPlayer(position);
            metaDataSetting(current_song_path);

            playerService.start();
            seekBarUpdate(seekBar);
            playerService.onComplete();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    playerService.stop();
                    playerService.release();

                    if (!isOnRepeat) {
                        position = ((position - 1) % player_songs_list.size());
                    }

                    current_song_path = Uri.parse(player_songs_list.get(position).getPath());

                    playerService.createMediaPlayer(position);
                    metaDataSetting(current_song_path);


                    playerService.start();
                    seekBarUpdate(seekBar);
                    playerService.onComplete();

                    playPauseButton.setImageResource(R.drawable.player_pause);
                }
            }
        }
        playerService.showNotification(R.drawable.notification_pause, 1F);
    }

    public void nextThreadButton() {
        Thread nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                nextButton.setOnClickListener(view -> nextButtonClicked(false));
            }
        };
        nextThread.start();
    }

    public void nextButtonClicked(boolean fromComplete){
        if(playerService.isPlaying()){
            playerService.stop();
            playerService.release();

            position = getNextPosition(fromComplete);

            current_song_path = Uri.parse(player_songs_list.get(position).getPath());

            playerService.createMediaPlayer(position);

            metaDataSetting(current_song_path);

            playerService.onComplete();
            playerService.start();
            seekBarUpdate(seekBar);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                     playerService.stop();
                     playerService.release();

                     position = getNextPosition(fromComplete);
                     current_song_path = Uri.parse(player_songs_list.get(position).getPath());

                     playerService.createMediaPlayer(position);
                     metaDataSetting(current_song_path);


                     playerService.onComplete();
                     playerService.start();
                     seekBarUpdate(seekBar);

                     playPauseButton.setImageResource(R.drawable.player_pause);
                 }
            }
        }
        playerService.showNotification(R.drawable.notification_pause, 1F);
    }

    private int getNextPosition(boolean fromComplete) {
        if(isOnShuffle) {
            return new Random().nextInt(player_songs_list.size());
        } else if(isOnRepeat && fromComplete) {
            return position;
        } else {
            return ((position + 1) % player_songs_list.size());
        }
    }

    private String formattedTime(int time) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(time) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(time) % TimeUnit.MINUTES.toSeconds(1)); // milliseconds to "minutes:seconds" format
    }

    private void initializeViews() {
        song_name = findViewById(R.id.player_song_name);
        artist_name = findViewById(R.id.player_artist_name);

        song_name.setSelectAllOnFocus(true);
        artist_name.setSelectAllOnFocus(true);

        timeTotal = findViewById(R.id.seek_bar_timeTotal);
        timePlayed = findViewById(R.id.seek_bar_timePlayed);

        cover_art = findViewById(R.id.player_cover_art);

        nextButton = findViewById(R.id.player_skip_next_button);
        previousButton = findViewById(R.id.player_skip_previous_button);
        shuffleButton = findViewById(R.id.player_shuffle_button);
        playPauseButton = findViewById(R.id.player_playPause);
        repeatButton = findViewById(R.id.player_repeat_button);

        seekBar = findViewById(R.id.player_seek_bar);

        shuffleButton.setImageResource(isOnShuffle ? R.drawable.player_shuffle_on : R.drawable.player_shuffle_off);
        repeatButton.setImageResource(isOnRepeat ? R.drawable.player_repeat_on : R.drawable.player_repeat_off);
    }

    private void metaDataSetting(Uri song_Uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        retriever.setDataSource(song_Uri.toString());

        song_name.setText(player_songs_list.get(position).getTitle());
        artist_name.setText(player_songs_list.get(position).getArtist());

        timeTotal.setText(formattedTime(Integer.parseInt(player_songs_list.get(position).getDuration())));

        byte[] songArt = retriever.getEmbeddedPicture();
        if(songArt != null){
            makeGradientEffectWithPalette(songArt);
        }
        else {
            makeGradientEffectNoPalette(drawableToBitmap(R.drawable.def_player_img));
        }

    }

    private Bitmap drawableToBitmap(int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(this, drawableId);                                                 // Converting drawable to bitmap
        assert drawable != null;
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void makeGradientEffectWithPalette(byte[] art) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
        ImageAnimation(this, cover_art, bitmap);
        Palette.from(bitmap).generate(palette -> {
            assert palette != null;
            Palette.Swatch swatch = palette.getDominantSwatch();
            ImageView gradient = findViewById(R.id.player_imageViewGradient);
            RelativeLayout mContainer = findViewById(R.id.player_mContainer);
            gradient.setBackgroundResource(R.drawable.gradient_bg);
            mContainer.setBackgroundResource(R.drawable.main_bg);
            if(swatch != null){

                gradient.setBackground(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[]{swatch.getRgb(), 0x00000000}));

                mContainer.setBackground(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[]{swatch.getRgb(), swatch.getRgb()}));

                song_name.setTextColor(swatch.getTitleTextColor());
                artist_name.setTextColor(swatch.getBodyTextColor());
                timeTotal.setTextColor(swatch.getBodyTextColor());
                timePlayed.setTextColor(swatch.getBodyTextColor());
            }
            else {

                gradient.setBackground(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[]{0xff000000, 0x00000000}));

                mContainer.setBackground(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[]{0xff000000, 0xff000000}));

                song_name.setTextColor(Color.WHITE);
                artist_name.setTextColor(Color.DKGRAY);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                seekBar.setOutlineAmbientShadowColor(Color.GRAY);
            }
        });
    }

    private void makeGradientEffectNoPalette(Bitmap bitmap){
        ImageAnimation(this, cover_art, bitmap);
        ImageView gradient = findViewById(R.id.player_imageViewGradient);
        RelativeLayout mContainer = findViewById(R.id.player_mContainer);
        gradient.setBackgroundResource(R.drawable.gradient_bg);
        mContainer.setBackgroundResource(R.drawable.main_bg);

        gradient.setBackground(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{0xff000000, 0x00000000}));

        mContainer.setBackground(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{0xff000000, 0xff000000}));

        song_name.setTextColor(Color.WHITE);
        artist_name.setTextColor(Color.GRAY);

        timeTotal.setTextColor(Color.WHITE);
        timePlayed.setTextColor(Color.WHITE);
    }

    public void ImageAnimation(Context context, ImageView imageView, Bitmap bitmap){
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);

        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);

                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        imageView.startAnimation(animOut);
    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        PlayerService.MyBinder myBinder = (PlayerService.MyBinder) iBinder;
        playerService = myBinder.getService();

        playerService.showNotification(R.drawable.notification_pause, 1F);
        playerService.setCallBack(this);

        seekBar.setMax(playerService.getDuration() / 1000);
        playerService.onComplete();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        playerService = null;
    }


}