package com.example.musicplayer;

import static com.example.musicplayer.MainActivity.albums;
import static com.example.musicplayer.MainActivity.allSongs;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class AlbumFragment extends Fragment {

    RecyclerView recyclerView;
    AlbumAdapter albumAdapter;
    public AlbumFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewAlbum);
        recyclerView.setHasFixedSize(true);
        if(albums.size() >= 1){
            albumAdapter = new AlbumAdapter(getContext(), albums);
            recyclerView.setAdapter(albumAdapter);
            recyclerView.setHasFixedSize(true);
            recyclerView.setItemViewCacheSize(50);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2){
                @Override
                protected int getExtraLayoutSpace(RecyclerView.State state) {
                    return 5000;
                }
            });

        }
        return view;
    }
}