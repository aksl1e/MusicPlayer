package com.example.musicplayer;

import static com.example.musicplayer.MainActivity.allSongs;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class SongsFragment extends Fragment {


    RecyclerView recyclerView;
    SongsAdapter songsAdapter;
    public SongsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_songs, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        if(allSongs.size() >= 1){
           songsAdapter = new SongsAdapter(getContext(), allSongs);
           recyclerView.setAdapter(songsAdapter);
           recyclerView.setHasFixedSize(true);
           recyclerView.setItemViewCacheSize(100);
           recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false){
               @Override
               protected int getExtraLayoutSpace(RecyclerView.State state) {
                   return 5000;
               }
           });

        }
        return view;
    }


}