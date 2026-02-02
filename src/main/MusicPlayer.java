package main;

import containers.Playlist;
import containers.Song;
import data_structures_algorithms.CircularDoublyLinkedList;

import javafx.application.Platform;
import javafx.beans.property.*;
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

        playlist = new CircularDoublyLinkedList<>();
        ArrayList<Song> songs = database.getSongs();

        playlist.rebuildFrom(songs);
        setCurrentSong(playlist.get());
        setCurrentPlaylist(new Playlist(0, "Library"));

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

    private final ChangeListener<Status> statusListener = (obs, oldValue, newValue) -> actualStatus.set(newValue);



    private Database database;
    private MediaPlayer mediaPlayer;
    public MediaPlayer getMediaPlayer() { return mediaPlayer; }

    public CircularDoublyLinkedList<Song> playlist;

    private RepeatMode repeatMode = RepeatMode.OFF;
    public void setRepeatMode(RepeatMode repeatMode) { this.repeatMode = repeatMode; }



    public void addSong(Song song) { playlist.add(song); }



    public void playOrPause() {

        if (mediaPlayer == null) {
            play();

        } else {
            MediaPlayer.Status status = actualStatus.get();
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

        currentTime.bind(mediaPlayer.currentTimeProperty());



        mediaPlayer.setOnEndOfMedia(() -> {
            if (repeatMode == RepeatMode.OFF) {
                next();
                play();

            } else if (repeatMode == RepeatMode.ALL) {
               if (next()) {
                   play();
               }

            } else if (repeatMode == RepeatMode.ONE) {
                play();
            }
        });


        setCurrentSong(song);
        mediaPlayer.volumeProperty().bind(volume.divide(100.));
        mediaPlayer.play();
    }


    public void stop() {
        runOnFX(() -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
        });
    }



    public boolean previous() {
        /*
         * If there is only one playlist
         * loop within it, otherwise
         *
         * if there are multiple playlists,
         * and RepeatMode is not set
         * to loop all playlists,
         * loop within current playlist
         */
        if (playlistSequence.size() == 1) {
            playlist.previous(true);
            setCurrentSong(playlist.get());
            return true;

        } else if (playlist.previous(repeatMode != RepeatMode.ALL)) {
            setCurrentSong(playlist.get());
            return true;
        }



        playlistIndex = (playlistIndex - 1 + playlistSequence.size()) % playlistSequence.size();
        Playlist previousPlaylist = null;



        /*
         * Loop until we find
         * a non-empty playlist
         */
        int checkedIndex = 0;
        while (checkedIndex < playlistSequence.size()) {
            previousPlaylist = playlistSequence.get(playlistIndex);

            boolean isEmpty = database.getPlaylistSize(previousPlaylist) == 0;
            boolean isLibrary = previousPlaylist.getId() == 0;
            boolean isDatabaseEmpty = database.getSongs().isEmpty();

            if (!isEmpty || (isLibrary && !isDatabaseEmpty)) break;

            playlistIndex = (playlistIndex - 1 + playlistSequence.size()) % playlistSequence.size();
            checkedIndex++;
        }



        /*
         * Update current playlist and song
         * object properties, in order for UI
         * to reflect the changes
         */
        setCurrentPlaylist(previousPlaylist);
        setCurrentSong(playlist.getLast());
        return false;
    }



    public boolean next() {
        /*
         * If there is only one playlist
         * loop within it, otherwise
         *
         * if there are multiple playlists,
         * and RepeatMode is not set
         * to loop all playlists,
         * loop within current playlist
         */
        if (playlistSequence.size() == 1) {
            playlist.next(true);
            setCurrentSong(playlist.get());
            return true;

        } else if (playlist.next(repeatMode != RepeatMode.ALL)) {
            setCurrentSong(playlist.get());
            return true;
        }



        playlistIndex = (playlistIndex + 1) % playlistSequence.size();
        Playlist nextPlaylist = null;



        /*
         * Loop until we find
         * a non-empty playlist
         */
        int checkedIndex = 0;
        while (checkedIndex < playlistSequence.size()) {
            nextPlaylist = playlistSequence.get(playlistIndex);

            boolean isEmpty = database.getPlaylistSize(nextPlaylist) == 0;
            boolean isLibrary = nextPlaylist.getId() == 0;
            boolean isDatabaseEmpty = database.getSongs().isEmpty();

            if (!isEmpty || (isLibrary && !isDatabaseEmpty)) break;

            playlistIndex = (playlistIndex + 1) % playlistSequence.size();
            checkedIndex++;
        }



        /*
         * Update current playlist and song
         * object properties, in order for UI
         * to reflect the changes
         */
        setCurrentPlaylist(nextPlaylist);
        setCurrentSong(playlist.getFirst());
        return false;
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

    private final ReadOnlyObjectWrapper<Duration> currentTime = new ReadOnlyObjectWrapper<>(Duration.ZERO);
    public ReadOnlyObjectProperty<Duration> currentTimeProperty() { return currentTime.getReadOnlyProperty(); }

    public void seek(Duration duration) {
        if (mediaPlayer != null) { mediaPlayer.seek(duration); }
    }



    // TODO DOCUMENTATION
    private void runOnFX(Runnable runnable) {
        if (Platform.isFxApplicationThread()) runnable.run();
        else Platform.runLater(runnable);
    }



    /*
     * Object property that stores current song,
     * used for synchronization with UI.
     * Paired with functions to manipulate it.
     */
    private final ObjectProperty<Song> currentSong = new SimpleObjectProperty<>();

    public ObjectProperty<Song> currentSongProperty() { return currentSong; }
    public void setCurrentSong(Song song) {
        Song current = this.currentSong.get();
        //if (current == song) this.currentSong.set(null);
        this.currentSong.set(song); }
    public Song getCurrentSong() { return this.currentSong.get(); }



    /*
     * Object property that stores current playlist,
     * used for synchronization with UI.
     * Paired with functions to manipulate it.
     */
    private final ObjectProperty<Playlist> currentPlaylist = new SimpleObjectProperty<>();

    public ObjectProperty<Playlist> currentPlaylistProperty() { return currentPlaylist; }
    public Playlist getCurrentPlaylist() { return this.currentPlaylist.get(); }
    public void setCurrentPlaylist(Playlist playlist) { this.currentPlaylist.set(playlist); }



    // TODO DOCUMENTATION
    private ArrayList<Playlist> playlistSequence = new ArrayList<>();
    private int playlistIndex = 0;

    public ArrayList<Playlist> getPlaylistSequence() { return this.playlistSequence; }
    public void setPlaylistSequence(ArrayList<Playlist> playlists) {
        this.playlistSequence = playlists;

        for(int i = 0; i < playlists.size(); i++) {
            if (playlists.get(i).getId() == getCurrentPlaylist().getId()) {
                this.playlistIndex = i;
                break;
            }
        }
    }
}
