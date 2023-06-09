package com.example.musicplayer;

import static com.example.musicplayer.MainActivity.imagesCache;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.Log;
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

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.MyHolder>{

    private final Context aContext;
    private final ArrayList<SongData> aFiles;

    View view;

    public AlbumAdapter(Context aContext, ArrayList<SongData> aFiles) {
        this.aContext = aContext;
        this.aFiles = aFiles;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater
                .from(aContext)
                .inflate(R.layout.album_item, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        holder.album_name.setText(aFiles.get(position).getAlbum());

        Bitmap albumArt = imagesCache.getBitmapFromMemCache(aFiles.get(position).getPath());

        Glide.with(aContext).asBitmap()
                .load(albumArt)
                .fallback(R.drawable.player_icon)
                .into(holder.album_img);


        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(aContext, AlbumSongs.class);
            intent.putExtra("albumName", aFiles.get(position).getAlbum());
            intent.putExtra("imagePath", aFiles.get(position).getPath());
            aContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return aFiles.size();
    }
    public static class MyHolder extends RecyclerView.ViewHolder{
        ImageView album_img;
        TextView album_name;
        public MyHolder(@NonNull View itemView) {
            super(itemView);

            album_img = itemView.findViewById(R.id.album_item_img);
            album_name = itemView.findViewById(R.id.album_name);
        }
    }

}
