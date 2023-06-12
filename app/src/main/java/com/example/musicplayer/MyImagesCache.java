package com.example.musicplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.util.LruCache;

import java.io.IOException;
import java.util.ArrayList;

public class MyImagesCache extends LruCache<String, Bitmap> {
    public MyImagesCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, Bitmap bitmap) {
        return bitmap.getByteCount() / 1024;
    }

    public void cacheAlbumImage(String path){
        byte[] art = getAlbumArt(path);

        if(art != null){
            Bitmap bitmap = BitmapFactory.decodeByteArray(art,0 , art.length);
            addBitmapToMemoryCache(path, bitmap);
        }
    }

    public void cacheAlbumImages(ArrayList<SongData> fromList){
        for (SongData song:
             fromList) {
            byte[] art = getAlbumArt(song.getPath());

            if(art != null){
                Bitmap bitmap = BitmapFactory.decodeByteArray(art,0 , art.length);
                addBitmapToMemoryCache(song.getPath(), bitmap);
            }
        }
    }


    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            this.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return this.get(key);
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
}
