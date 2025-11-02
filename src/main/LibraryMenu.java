package main;

import containers.Playlist;

import containers.Song;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;


public class LibraryMenu extends VBox {

    Database database;
    MusicPlayer musicPlayer;

    Library library;

    ObservableList<Playlist> playlistList;

    @FXML private ListView<Playlist> listView;

    public LibraryMenu() {

    }

    public void initializeLibraryMenu(Database database, MusicPlayer musicPlayer, Library library) {
        this.database = database;
        this.musicPlayer = musicPlayer;
        this.library = library;

        playlistList = FXCollections.observableArrayList(database.getPlaylists());
        Playlist mainPlaylist = new Playlist(0, "Library");
        playlistList.addFirst(mainPlaylist);
        listView.setItems(playlistList);

        setupCellFactory();
        setupDeletionHandler();
    }

    public void addPlaylist(Playlist playlist) {
        playlistList.add(playlist);
    }

    //
    // Private Methods
    //
    private void setupCellFactory() {
        listView.setCellFactory(lv -> new ListCell<Playlist>() {
            private final VBox container = new VBox(5);
            private final ImageView thumbnail = new ImageView();
            private final Label nameLabel = new Label();

            {
                thumbnail.setFitWidth(50);
                thumbnail.setFitHeight(50);
                thumbnail.setPreserveRatio(true);
                container.getChildren().addAll(thumbnail, nameLabel);
            }

            // Double click to open CreatePlaylistMenu
            {
                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!isEmpty())) {

                        Playlist playlist = getItem();
                        ArrayList<Song> songs;
                        if (playlist.getId() != 0)
                            songs = database.getSongsInPlaylist(playlist);
                        else
                            songs = database.getSongs();

                        musicPlayer.playlist.rebuildFrom(songs);
                        library.onPlaylistChanged(playlist);
                    }
                });
            }

            @Override
            protected void updateItem(Playlist playlist, boolean empty) {
                super.updateItem(playlist, empty);
                if (empty || playlist == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    nameLabel.setText(playlist.getName());

                    Image image = null;
                    try {
                        String path = playlist.getThumbnailPath();
                        if (path != null && !path.isEmpty()) {
                            image = new Image(new File(path).toURI().toString());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (image == null) {
                        try {
                            image = new Image(getClass().getResourceAsStream("/PlaylistCover.png"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    thumbnail.setImage(image);
                    container.setAlignment(Pos.CENTER);
                    setGraphic(container);
                }
            }
        });
    }

    private void setupDeletionHandler() {
        listView.setOnKeyPressed(event -> {
           if (event.getCode() == KeyCode.DELETE) {
               Playlist selectedPlaylist = listView.getSelectionModel().getSelectedItem();
                if (selectedPlaylist != null) {
                    playlistList.remove(selectedPlaylist);
                    database.deletePlaylist(selectedPlaylist);
                }
           }
        });
    }
}
