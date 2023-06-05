package com.example.musicplayer;

import static com.example.musicplayer.MainActivity.allSongs;
import static com.example.musicplayer.MainActivity.imagesCache;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AlbumSongs extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView albumPhoto;
    String albumName;
    AlbumSongsAdapter albumSongsAdapter;

    ArrayList<SongData> albumSongs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);

        recyclerView = findViewById(R.id.recyclerView);
        albumPhoto = findViewById(R.id.albumPhoto);

        albumName = getIntent().getStringExtra("albumName");

        albumSongs = getAlbumSongs();

        Bitmap albumArt = imagesCache.getBitmapFromMemCache(getIntent().getStringExtra("imagePath"));

        if(albumArt != null){
            Glide.with(this).asBitmap()
                    .load(albumArt)
                    .into(albumPhoto);
        } else {
            Glide.with(this)
                    .load(R.drawable.player_icon)
                    .into(albumPhoto);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(albumSongs.size() >= 1){
            albumSongsAdapter = new AlbumSongsAdapter(this, albumSongs);
            recyclerView.setAdapter(albumSongsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            recyclerView.setItemViewCacheSize(20);
        }
    }

    private ArrayList<SongData> getAlbumSongs(){
        ArrayList<SongData> result = new ArrayList<>();
        int j = 0;
        for(int i = 0; i < allSongs.size(); i++){
            if(albumName.equals(allSongs.get(i).getAlbum())){
                result.add(j, allSongs.get(i));
                j++;
            }
        }

        return result;
    }
}