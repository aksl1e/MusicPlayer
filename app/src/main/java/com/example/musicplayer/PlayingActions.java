package com.example.musicplayer;

public interface PlayingActions {
    void playPauseButtonClicked();
    void previousButtonClicked();
    void nextButtonClicked(boolean fromComplete);
}
