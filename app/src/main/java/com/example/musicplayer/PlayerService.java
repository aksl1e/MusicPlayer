package com.example.musicplayer;

import static com.example.musicplayer.ApplicationClass.ACTION_NEXT;
import static com.example.musicplayer.ApplicationClass.ACTION_PLAY;
import static com.example.musicplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.example.musicplayer.ApplicationClass.CHANNEL_ID_2;
import static com.example.musicplayer.MainActivity.imagesCache;
import static com.example.musicplayer.PlayerActivity.player_songs_list;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;

public class PlayerService extends Service implements MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener {

    IBinder mBinder = new MyBinder();
    static MediaPlayer mediaPlayer = null;

    ArrayList<SongData> songs_service = new ArrayList<>();

    PlayingActions playerActions;
    MiniPlayerActions miniPlayerActions;
    MediaSessionCompat mediaSessionCompat;

    boolean wasPlaying = false;


    int position = -1;
    boolean fromNotification;

    // Audio Focus
    static int audioFocusRequest;
    static AudioManager audioManager;
    static AudioFocusRequest focusRequest;

    AudioAttributes playbackAttributes;

    Bitmap artBitmap;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My Audio");

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        playbackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this)
                .build();

    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {

        mediaPlayer.stop();
        mediaPlayer.release();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(focusRequest);
        }
        stopService(new Intent(this, PlayerService.class));

        int id = android.os.Process.myPid();
        android.os.Process.killProcess(id);
        super.onTaskRemoved(rootIntent);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            if(wasPlaying){
                mediaPlayer.start();
                wasPlaying = false;
                showNotification(R.drawable.notification_pause, 1F);
            }
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            if(isPlaying()){
                wasPlaying = true;
                mediaPlayer.pause();
                showNotification(R.drawable.notification_play, 0F);
            }
            mediaPlayer.pause();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            if(isPlaying()){
                playerActions.playPauseButtonClicked();
                miniPlayerActions.refresh();
                showNotification(R.drawable.notification_play, 0F);
            }
        }
    }


    public class MyBinder extends Binder {
        PlayerService getService(){
            return PlayerService.this;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPos = intent.getIntExtra("servicePosition", -1);


        String actionName = intent.getStringExtra("ActionName");
        fromNotification = intent.getBooleanExtra("fromNotification", false);
        audioFocusRequest = audioManager.requestAudioFocus(focusRequest);

        if(myPos != -1){
            playMedia(myPos);
        }

        setArtBitmap(position);
        if(actionName != null){
            switch(actionName){
                case "playPause" -> {
                    if(playerActions != null){
                        playerActions.playPauseButtonClicked();
                        if(!isPlaying()){
                            showNotification(R.drawable.notification_play, 0F);
                        } else {
                            showNotification(R.drawable.notification_play, 1F);
                        }
                        miniPlayerActions.refresh();
                    }
                }
                case "next" -> {
                    if(playerActions != null){
                        playerActions.nextButtonClicked(false);

                        setArtBitmap(position);
                        miniPlayerActions.refresh();
                    }
                }
                case "previous" -> {
                    if(playerActions != null){
                        playerActions.previousButtonClicked();

                        setArtBitmap(position);
                        miniPlayerActions.refresh();
                    }
                }
            }
        }
        return START_STICKY;

    }

    private void setArtBitmap(int myPos) {
        if(imagesCache.getBitmapFromMemCache(player_songs_list.get(myPos).getPath()) == null){
            byte[] artByte = getAlbumArt(player_songs_list.get(myPos).getPath());
            artBitmap = artByte != null ? BitmapFactory.decodeByteArray(artByte, 0, artByte.length)
                    : BitmapFactory.decodeResource(getResources(), R.drawable.def_song_art);
        } else {
            artBitmap = imagesCache.getBitmapFromMemCache(player_songs_list.get(myPos).getPath());
        }
    }

    private void playMedia(int startPosition) {
        songs_service = player_songs_list;
        position = startPosition;


        if((mediaPlayer != null && mediaPlayer.isPlaying()) && (!fromNotification)){
            mediaPlayer.stop();
            mediaPlayer.release();
            if(songs_service != null){
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }
        else if (!fromNotification){
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(playerActions != null){
            playerActions.nextButtonClicked(true);
        }
        if(mediaPlayer != null){
            stop();
            release();
        }
        createMediaPlayer(position);
        start();
        miniPlayerActions.refresh();
        setArtBitmap(position);

        onComplete();
    }
    void start(){
        mediaPlayer.start();
    }

    void stop(){
        mediaPlayer.stop();
    }
    void pause(){
        mediaPlayer.pause();
    }

    int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }

    void release(){
        mediaPlayer.release();
    }

    boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    int getDuration(){
        return mediaPlayer.getDuration();
    }

    void seekTo(int pos){
        mediaPlayer.seekTo(pos);
    }

    void createMediaPlayer(int pos){
        position = pos;
        mediaPlayer = MediaPlayer.create(getBaseContext(), Uri.parse(songs_service.get(position).getPath()));
    }

    void onComplete(){
        mediaPlayer.setOnCompletionListener(this);
    }

    void setCallBack(PlayingActions playingActions){
        this.playerActions = playingActions;
    }

    void setCallBack_mini(MiniPlayerActions miniPlayerActions){
        this.miniPlayerActions = miniPlayerActions;
    }

    SongData getCurrentSong() {
        return player_songs_list.get(position);
    }
    void playbackStateSetting(float playbackSpeed){

        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder()
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, getDuration());

        mediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(getCurrentState(), getCurrentPosition(), playbackSpeed)
                .setBufferedPosition(getDuration())
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .build());

        mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onSeekTo(long pos) {
                seekTo((int)(pos));
                super.onSeekTo(pos);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                playerActions.nextButtonClicked(false);
                miniPlayerActions.refresh();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                playerActions.previousButtonClicked();
                miniPlayerActions.refresh();
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onPlay() {
                playerActions.playPauseButtonClicked();
                miniPlayerActions.refresh();
                audioFocusRequest = audioManager.requestAudioFocus(focusRequest);

                super.onPlay();
            }


            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onPause() {
                playerActions.playPauseButtonClicked();
                miniPlayerActions.refresh();
                audioFocusRequest = audioManager.requestAudioFocus(focusRequest);

                super.onPause();
            }

        });

        mediaSessionCompat.setMetadata(builder.build());
    }

    void showNotification(int playPauseButton, float playbackSpeed){
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("position", position);
        if(!isPlaying()){
            intent.putExtra("isPlaying", false);
        }
        intent.putExtra("fromNotification", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        playbackStateSetting(playbackSpeed);

        Intent PREV_Intent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent PREV_Pending = PendingIntent.getBroadcast(this, 0, PREV_Intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent NEXT_Intent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent NEXT_Pending = PendingIntent.getBroadcast(this, 0, NEXT_Intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent PAUSE_Intent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent PAUSE_Pending = PendingIntent.getBroadcast(this, 0, PAUSE_Intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);



        int[] actions = {0,1,2};

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(R.drawable.app_icon_def)
                .setLargeIcon(artBitmap)
                .setContentTitle(player_songs_list.get(position).getTitle())
                .setContentText(player_songs_list.get(position).getArtist())
                .addAction(R.drawable.notification_previous, "Previous", PREV_Pending)
                .addAction(playPauseButton, "Pause", PAUSE_Pending)
                .addAction(R.drawable.notification_next, "Next", NEXT_Pending)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(actions)
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setSilent(true);


        Notification notification = builder.build();


        startForeground(2, notification);
    }

    byte[] getAlbumArt(String uri) {
        byte[] art = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        try {
            retriever.setDataSource(uri);
            art = retriever.getEmbeddedPicture();
            retriever.release();
        } catch (IOException e) {
            Log.e("getEmbeddedPicture failed", "path:" + uri);
        }

        return art;
    }


    private int getCurrentState() {
        if(isPlaying()){
            return PlaybackStateCompat.STATE_PLAYING;
        } else return PlaybackStateCompat.STATE_PAUSED;
    }


}
