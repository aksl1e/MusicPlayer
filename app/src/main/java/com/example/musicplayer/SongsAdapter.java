package com.example.musicplayer;

import static com.example.musicplayer.MainActivity.imagesCache;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.util.ArrayList;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.MyVieHolder> {

    private final Context mContext;
    private final ArrayList<SongData> mFiles;

    public SongsAdapter(Context mContext, ArrayList<SongData> mFiles) {
        this.mContext = mContext;
        this.mFiles = mFiles;
    }


    @NonNull
    @Override
    public SongsAdapter.MyVieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new MyVieHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsAdapter.MyVieHolder holder, int position) {
        holder.file_name.setText(mFiles.get(position).getTitle());

        Bitmap albumArt = imagesCache.getBitmapFromMemCache(mFiles.get(position).getPath());

        Glide.with(mContext).asBitmap()
                .placeholder(R.drawable.def_song_art)
                .load(albumArt)
                .fallback(R.drawable.def_song_art)
                .into(holder.album_art);


        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, PlayerActivity.class);
            intent.putExtra("intentBy", "fromSongs");
            intent.putExtra("position", position);
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public static class MyVieHolder extends RecyclerView.ViewHolder {
        TextView file_name;
        ImageView album_art;

        public MyVieHolder(@NonNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.music_file_name);
            album_art = itemView.findViewById(R.id.music_img);
        }
    }

}
