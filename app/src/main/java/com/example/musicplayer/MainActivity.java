package com.example.musicplayer;

import static com.example.musicplayer.PlayerService.mediaPlayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MiniPlayerActions,
        ServiceConnection {

    public static final int REQUEST_CODE = 1;
    static ArrayList<SongData> allSongs = new ArrayList<>();
    static boolean isOnShuffle = false;
    static boolean isOnRepeat = false;

    static MyImagesCache imagesCache;

    static ArrayList<SongData> albums = new ArrayList<>();

    Context context;

    PlayerService playerService = null;

    RelativeLayout miniPlayerContainer;
    TextView miniPlayer_song;
    TextView miniPlayer_artist;
    ImageView miniPlayer_img, miniPlayer_next, miniPlayer_playPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        setContentView(R.layout.activity_main);

        cacheSetup();

        filesPermission();
        initViewPager();

        miniPlayerInit();
    }

    private void miniPlayerInit() {
        miniPlayerContainer = findViewById(R.id.mini_player_layout);
        miniPlayer_song = findViewById(R.id.mini_player_song);
        miniPlayer_artist = findViewById(R.id.mini_player_artist);
        miniPlayer_img = findViewById(R.id.mini_player_img);

        miniPlayer_song.setSelected(true);
        miniPlayer_artist.setSelected(true);

        miniPlayer_next = findViewById(R.id.mini_player_next);
        miniPlayer_playPause = findViewById(R.id.mini_player_playPause);

        miniPlayer_playPause.setOnClickListener(view -> {
            playerService.playerActions.playPauseButtonClicked();
            if(playerService.isPlaying()) {
                miniPlayer_song.setSelected(true);
                miniPlayer_artist.setSelected(true);
            } else {
                miniPlayer_song.setSelected(false);
                miniPlayer_artist.setSelected(false);
            }
            refresh();
        });

        miniPlayer_next.setOnClickListener(view -> {
            playerService.playerActions.nextButtonClicked(false);
            refresh();
        });

        miniPlayerContainer.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("position", playerService.position);
            if(!playerService.isPlaying()){
                intent.putExtra("isPlaying", false);
            }
            intent.putExtra("intentBy", "fromMainActivity");
            startActivity(intent);
        });
    }

    private void miniPlayerRefresh(int playPause){
        SongData currentPlayingSong = playerService.getCurrentSong();

        if(imagesCache.getBitmapFromMemCache(currentPlayingSong.getPath()) != null){
            miniPlayer_img.setImageBitmap(imagesCache.getBitmapFromMemCache(currentPlayingSong.getPath()));
        } else {
            miniPlayer_img.setImageResource(R.drawable.def_song_art);
        }

        miniPlayer_song.setText(currentPlayingSong.getTitle());
        miniPlayer_artist.setText(currentPlayingSong.getArtist());
        miniPlayer_playPause.setImageResource(playPause);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void cacheSetup() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 2;
        imagesCache = new MyImagesCache(cacheSize);
    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this, PlayerService.class);
        bindService(intent, this, BIND_AUTO_CREATE);

        if(mediaPlayer != null){
            miniPlayerContainer.setVisibility(View.VISIBLE);
            miniPlayer_song.setSelected(true);
            miniPlayer_artist.setSelected(true);
            refresh();
        }
        super.onResume();
    }

    private void filesPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.READ_MEDIA_AUDIO},
                        REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE);
            }
        }
        else {
            allSongs = getAllAudio(this);
            initViewPager();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)){
            allSongs = getAllAudio(this);
            initViewPager();
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_AUDIO},
                        REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE);
            }
        }
    }

    private void initViewPager() {
        ViewPager viewPager = findViewById(R.id.viewpager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new SongsFragment(), "Songs");
        viewPagerAdapter.addFragments(new AlbumFragment(), "Albums");

        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);

    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        PlayerService.MyBinder myBinder = (PlayerService.MyBinder) iBinder;
        playerService = myBinder.getService();
        playerService.setCallBack_mini(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        miniPlayerContainer.setVisibility(View.GONE);
        playerService = null;
    }

    @Override
    public void refresh() {
        if(playerService != null) {
            if (playerService.isPlaying()) {
                miniPlayerRefresh(R.drawable.notification_pause);

                miniPlayer_song.setSelected(true);
                miniPlayer_artist.setSelected(true);
            } else {
                miniPlayerRefresh(R.drawable.notification_play);

                miniPlayer_song.setSelected(false);
                miniPlayer_artist.setSelected(false);
            }
        }
    }

    public static class ViewPagerAdapter extends FragmentPagerAdapter{

        private final ArrayList<Fragment> fragments;
        private final ArrayList<String> titles;

        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        void addFragments(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }


        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    public static ArrayList<SongData> getAllAudio(Context context){
        ArrayList<SongData> result = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ArrayList<String> duplicate = new ArrayList<>();

        String [] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,                //File's directory
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID
        };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if(cursor != null){
            while(cursor.moveToNext()) {
                SongData song = new SongData(cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5));
                if(songIsValid(song, result)){
                    result.add(song);
                    imagesCache.cacheAlbumImage(result.get(result.size() - 1).getPath());
                    if(!duplicate.contains(song.getAlbum())){
                        albums.add(song);
                        duplicate.add(song.getAlbum());
                    }
                }
            }
            cursor.close();
        }
        return result;
    }


    private static boolean songIsValid(SongData song, ArrayList<SongData> inList) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(song.getPath());
        } catch (RuntimeException e){
            return false;
        }

        for(int i = 0; i < inList.size(); i++){
            if(song.getTitle().equals(inList.get(i).getTitle()) && song.getArtist().equals(inList.get(i).getArtist()) && song.getDuration().equals(inList.get(i).getDuration())){
                return false;
            }
        }

        return true;
    }
}