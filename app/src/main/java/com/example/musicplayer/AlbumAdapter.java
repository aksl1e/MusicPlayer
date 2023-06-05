package com.example.musicplayer;

import android.content.Context;
import android.content.Intent;
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

    private Context aContext;
    private ArrayList<SongData> aFiles;

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

        byte[] albumArt = getAlbumArt(aFiles.get(position).getPath());


        if(albumArt != null){

            Glide.with(aContext).asBitmap()
                    .load(albumArt)
                    .into(holder.album_img);
        }
        else {
            Glide.with(aContext).asBitmap()
                    .load(R.drawable.player_icon)
                    .into(holder.album_img);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(aContext, AlbumSongs.class);
                intent.putExtra("albumName", aFiles.get(position).getAlbum());
                intent.putExtra("imagePath", aFiles.get(position).getPath());
                aContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return aFiles.size();
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

    public class MyHolder extends RecyclerView.ViewHolder{
        ImageView album_img;
        TextView album_name;
        public MyHolder(@NonNull View itemView) {
            super(itemView);

            album_img = itemView.findViewById(R.id.album_item_img);
            album_name = itemView.findViewById(R.id.album_name);
        }
    }

}
