package main;

import containers.Playlist;

import popups.CreatePlaylistMenu;
import popups.EditPlaylistMenu;

import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Window;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;


public class LibraryMenu extends VBox {

    private Database database;
    private MusicPlayer musicPlayer;

    private Library library;

    private ObservableList<Playlist> playlistList;

    @FXML private Button shufflePlaylistButton;
    @FXML private Button createPlaylistButton;
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
        createPlaylistButton.setOnAction(evt -> createPlaylist());

        //
        musicPlayer.setPlaylistSequence(new ArrayList<>(playlistList));
        musicPlayer.currentPlaylistProperty().addListener((obs, oldPlaylist, newPlaylist) -> listView.refresh());

        musicPlayer.currentPlaylistProperty().set(mainPlaylist);
    }

    public void addPlaylist(Playlist playlist) {
        playlistList.add(playlist);
    }

    // todo test
    public void editPlaylist(Playlist oldPlaylist, Playlist playlist) {
        int i = playlistList.indexOf(oldPlaylist);
        playlistList.set(i, playlist);
    }

    public void createPlaylist() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PlaylistPopup.fxml"));
            Parent parent = loader.load();

            Window window = listView.getScene().getWindow();

            CreatePlaylistMenu createPlaylistMenu = loader.getController();
            createPlaylistMenu.initializeCreatePlaylistMenu(window, parent, database, musicPlayer, this);

        }   catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void temp(Playlist playlist) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/editPlaylistPopup.fxml"));
            Parent parent = loader.load();

            Window window = listView.getScene().getWindow();

           EditPlaylistMenu editPlaylistMenu = loader.getController();
           editPlaylistMenu.initializeEditPlaylistMenu(window, parent, database, musicPlayer, this, playlist);

        } catch (Exception e) {
            e.printStackTrace();
        }
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



            // Right click
            {
                ContextMenu contextMenu = new ContextMenu();
                SeparatorMenuItem separator = new SeparatorMenuItem();

                MenuItem editPlaylistButton = new MenuItem("Edit Playlist");
                editPlaylistButton.setOnAction(e -> temp(getItem()));
                getItem();
                //editPlaylistButton

                //?
                MenuItem temp = new MenuItem("Edit TBD");

                contextMenu.getItems().addAll(editPlaylistButton, temp);

                emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
                    setContextMenu(isEmpty ? null : contextMenu);
                });
            }



            @Override
            protected void updateItem(Playlist playlist, boolean empty) {
                super.updateItem(playlist, empty);
                if (empty || playlist == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: white;");
                } else {
                    nameLabel.setText(playlist.getName());
                    setStyle("");

                    Image image = null;
                    try {
                        String path = playlist.getFilePath();
                        if (path != null && !path.isEmpty()) {
                            File file = new File(path);

                            image = new Image(file.toURI().toString());
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
                    musicPlayer.getPlaylistSequence().remove(selectedPlaylist);
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
