package main;

import containers.Playlist;

import containers.Song;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;


public class LibraryMenu extends VBox {

    Database database;
    MusicPlayer musicPlayer;

    Library library;

    ObservableList<Playlist> playlistList;

    @FXML private Button shufflePlaylistButton;
    @FXML private ListView<Playlist> listView;

    public LibraryMenu() {

    }

    public void initializeLibraryMenu(Database database, MusicPlayer musicPlayer, Library library) {
        this.database = database;
        this.musicPlayer = musicPlayer;
        this.library = library;



        /*
         * Load Playlists from Database
         * Add "Library" Playlist at the top
         * Setup the cell factory and deletion handler
         */
        playlistList = FXCollections.observableArrayList(database.getPlaylists());
        Playlist mainPlaylist = new Playlist(0, "Library");
        playlistList.addFirst(mainPlaylist);
        listView.setItems(playlistList);

        setupCellFactory();
        setupDeletionHandler();

        shufflePlaylistButton.setOnAction(evt -> shuffle());

        //
        musicPlayer.setPlaylistSequence(new ArrayList<>(playlistList));
        musicPlayer.currentPlaylistProperty().addListener((obs, oldPlaylist, newPlaylist) -> listView.refresh());

        musicPlayer.currentPlaylistProperty().set(mainPlaylist);
        listView.refresh();
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

            // Double click to open playlist
            {
                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!isEmpty())) {

                        Playlist playlist = getItem();
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

                    // --- Update style ---
                    if (playlist == musicPlayer.getCurrentPlaylist()) {
                        setStyle("-fx-background-color: #f0f0f0;");
                    } else {
                        setStyle("");
                    }
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

    private void shuffle() {
        ArrayList<Playlist> playlists = musicPlayer.getPlaylistSequence();
        Collections.shuffle(playlists);
        musicPlayer.setPlaylistSequence(playlists);
    }
}
