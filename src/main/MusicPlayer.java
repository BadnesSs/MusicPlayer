package main;

import containers.Song;
import data_structures_algorithms.CircularDoublyLinkedList;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

public class MusicPlayer {

    public MusicPlayer(Database database) {

        this.database = database;

        //setupVolumeListener();

        playlist = new CircularDoublyLinkedList<>();
        ArrayList<Song> songs = database.getSongs();

        playlist.rebuildFrom(songs);
        setCurrentSong(playlist.get());

        Song song = playlist.get();
        if (song != null) {
            File file = new File(song.getFilepath());
            Media media = new Media(file.toURI().toString());

            runOnFX(() -> {
                MediaPlayer temp = new MediaPlayer(media);
                temp.setOnReady(() -> {
                    cachedDuration.set(temp.getTotalDuration());
                    temp.dispose();
                });
            });
        }
    }

    private void setupVolumeListener() {
        volume.addListener((obs, oldValue, newValue) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newValue.doubleValue()/100.);
            }
        });
    }



    // ----- Duration for Initialization -----
    private final ObjectProperty<Duration> cachedDuration = new SimpleObjectProperty<>(Duration.UNKNOWN);
    public ReadOnlyObjectProperty<Duration> cachedDurationProperty() { return cachedDuration; }
    public Duration getCachedDuration() { return cachedDuration.get(); }



    // ----- Volume Property -----
    private final SimpleDoubleProperty volume = new SimpleDoubleProperty(10);
    public SimpleDoubleProperty volumeProperty() { return volume; }



    //
    //
    // Destytojui stekas
    public Stack<Song> songStack = new Stack<>();
    //
    //
    //

    // Status related
    private final ObjectProperty<Status> actualStatus = new SimpleObjectProperty<>(Status.UNKNOWN);

    private final ChangeListener<Status> statusListener = (obs, oldVlue, newValue) -> actualStatus.set(newValue);



    private Database database;
    private MediaPlayer mediaPlayer;

    public CircularDoublyLinkedList<Song> playlist;

    private boolean bRepeating = false;
    public void setRepeating(boolean repeating) { this.bRepeating = repeating; }
    public boolean getRepeating() { return bRepeating; }



    public void addSong(Song song) { playlist.add(song); }



    public void playOrPause() {

        if (mediaPlayer == null) {
            play();
        } else {
            // cia
            MediaPlayer.Status status = actualStatus.get();
            System.out.println(status.toString());
            if (status == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.play();
            }
        }
    }

    public void play() {
        Song song = playlist.get();
        setCurrentSong(song);

        //
        //
        songStack.push(song);
        //
        //

        File file = new File(song.getFilepath());
        Media media = new Media(file.toURI().toString());

        if (mediaPlayer != null) {
            mediaPlayer.statusProperty().removeListener(statusListener);
            mediaPlayer.stop();
        }

        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.statusProperty().addListener(statusListener);
        actualStatus.set(mediaPlayer.getStatus());

        mediaPlayer.setOnEndOfMedia(() -> {
            if(!bRepeating) next();
            play();
        });

        /*
        mediaPlayer.setVolume(volume.get()/100.);

         */

        mediaPlayer.volumeProperty().bind(volume.divide(100.));
        System.out.println(volume.get()/100.);
        mediaPlayer.play();

        if (onSongChanged != null) {
            onSongChanged.songChanged();
        }
    }


    public void stop() {
        runOnFX(() -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
        });
    }

    public void previous() {
        playlist.previous();
        setCurrentSong(playlist.get());
    }

    public void next() {
        playlist.next();
        setCurrentSong(playlist.get());
    }



    public void setVolume(double value) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(value/100.);
        }
    }

    public double getVolume() {
        if (mediaPlayer != null) {
            return mediaPlayer.getVolume() * 100.;
        }
        return volume.get();
    }

    public Duration getDuration() {
        if (mediaPlayer != null) { return mediaPlayer.getTotalDuration(); }
        return null;
    }

    public ReadOnlyObjectProperty<Duration> currentTimeProperty() {
        if (mediaPlayer != null) { return mediaPlayer.currentTimeProperty(); }
        return null;
    }

    public void seek(Duration duration) {
        if (mediaPlayer != null) { mediaPlayer.seek(duration); }
    }



    //
    private void runOnFX(Runnable runnable) {
        if (Platform.isFxApplicationThread()) runnable.run();
        else Platform.runLater(runnable);
    }



    // REPLACE DELEGATE USAGES WITH OBJECT PROPERTY
    private OnSongChanged onSongChanged;

    public interface OnSongChanged {
        void songChanged();
    }

    public void songChanged(OnSongChanged onSongChanged) {
        this.onSongChanged = onSongChanged;
    }

    // REPLACING DELEGATE WITH OBJECT PROPERTY
    private final ObjectProperty<Song> currentSong = new SimpleObjectProperty<>();

    public ObjectProperty<Song> currentSongProperty() { return currentSong; }
    public void setCurrentSong(Song song) { this.currentSong.set(song); }
    public Song getCurrentSong() { return this.currentSong.get(); }
}
