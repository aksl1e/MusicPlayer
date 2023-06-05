package com.example.musicplayer;

import static com.example.musicplayer.MainActivity.imagesCache;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;

public class AlbumSongsAdapter extends RecyclerView.Adapter<AlbumSongsAdapter.MyHolder>{

    private Context aContext;
    static ArrayList<SongData> albumSongs;

    View view;

    public AlbumSongsAdapter(Context aContext, ArrayList<SongData> albumSongs) {
        this.aContext = aContext;
        AlbumSongsAdapter.albumSongs = albumSongs;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater
                .from(aContext)
                .inflate(R.layout.music_items, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        holder.album_name.setText(albumSongs.get(position).getTitle());
        Bitmap songArt = imagesCache.getBitmapFromMemCache(albumSongs.get(position).getPath());

        if(songArt != null){
            Glide.with(aContext).asBitmap()
                    .load(songArt)
                    .into(holder.album_img);
        }
        else {
            Glide.with(aContext)
                    .load(R.drawable.player_icon)
                    .into(holder.album_img);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(aContext, PlayerActivity.class);
                intent.putExtra("intentBy", "fromAlbums");
                intent.putExtra("position", position);
                aContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumSongs.size();
    }

    private byte[] getAlbumArt(String uri) {
        byte[] art;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        System.out.println(uri);
        try {
            retriever.setDataSource(uri);
            art = retriever.getEmbeddedPicture();
            retriever.release();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return art;
    }

    public class MyHolder extends RecyclerView.ViewHolder{
        ImageView album_img;
        TextView album_name;
        public MyHolder(@NonNull View itemView) {
            super(itemView);

            album_img = itemView.findViewById(R.id.music_img);
            album_name = itemView.findViewById(R.id.music_file_name);
        }
    }

}
