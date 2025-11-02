package main;

import containers.Playlist;
import containers.Song;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.ArrayList;

public class Library {

    @FXML private VBox root;
    @FXML private Label title;
    @FXML private TableView tableView;

    Database database;
    MusicPlayer musicPlayer;
    Player player;

    ObservableList<Song> songList;

    public Library() {}

    public void initializeLibrary(Database database, MusicPlayer musicPlayer, Player player) {
        this.database = database;
        this.musicPlayer = musicPlayer;
        this.player = player;

        songList = FXCollections.observableArrayList(musicPlayer.playlist.toList());

        createTableView();
        setupRowFactory(tableView);
        setupDeletionHandler(tableView);

        tableView.sortPolicyProperty().set(new Callback<TableView<Song>, Boolean>() {
            @Override
            public Boolean call(TableView<Song> tv) {
                Boolean result = TableView.DEFAULT_SORT_POLICY.call(tv);
                if (!tableView.getItems().isEmpty()) {
                    onTableSorted(new ArrayList<>(tableView.getItems()), musicPlayer);
                }
                return result;
            }
        });


        tableView.setItems(songList);

        musicPlayer.currentSongProperty().addListener((obs, oldSong, newSong) -> tableView.refresh());
    }



    public void addSong(Song song) {
        songList.add(song);
    }



    public void onTableSorted(ArrayList<Song> songs, MusicPlayer musicPlayer) {
        musicPlayer.playlist.rebuildFrom(songs);
    }



    public void onPlaylistChanged(Playlist playlist) {
        title.setText(playlist.getName());
        songList.setAll(musicPlayer.playlist.toList());
    };



    //
    // PRIVATE METHODS
    //

    // make it void later
    private void createTableView() {
        TableColumn<Song, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(50);

        TableColumn<Song, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setPrefWidth(400);

        TableColumn<Song, String> artistColumn = new TableColumn<>("Artist");
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        artistColumn.setPrefWidth(150);

        TableColumn<Song, Integer> durationColumn = new TableColumn<>("Duration");
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        durationColumn.setCellFactory(col -> new TableCell<Song, Integer>() {
            @Override
            protected void updateItem(Integer seconds, boolean empty) {
                super.updateItem(seconds, empty);
                if (empty || seconds == null) {
                    setText("");
                } else {
                    int mins = (int) (seconds / 60);
                    int secs = (int) (seconds % 60);
                    setText(String.format("%02d:%02d", mins, secs));
                }
            }
        });
        durationColumn.setPrefWidth(100);

        TableColumn<Song, String> formatColumn = new TableColumn<>("Format");
        formatColumn.setCellValueFactory(new PropertyValueFactory<>("format"));
        formatColumn.setPrefWidth(80);

        tableView.getColumns().addAll(idColumn, titleColumn, artistColumn, durationColumn, formatColumn);
    }



    private void setupRowFactory(TableView<Song> tableView) {
        tableView.setRowFactory(tv -> new TableRow<Song>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);
                if (song == null || empty) {
                    setStyle("");
                } else if (song == musicPlayer.playlist.get()) {
                    setStyle("-fx-background-color: #f0f0f0;");
                } else {
                    setStyle("");
                }
            }

            // Double click to play song
            {
                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!isEmpty())) {
                        Song rowData = getItem();
                        musicPlayer.playlist.moveTo(rowData);
                        player.setPlaypauseSelected(true);
                        musicPlayer.play();
                        System.out.println("Double click on: " + rowData.getTitle());
                    }
                });
            }



            // Drag start
            {
                setOnDragDetected(event -> {
                    if(!isEmpty()) {
                        Integer index = getIndex();
                        Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                        ClipboardContent clipboard = new ClipboardContent();
                        clipboard.put(DataFormat.PLAIN_TEXT, index.toString());
                        dragboard.setContent(clipboard);
                        event.consume();
                    }
                });
            }

            // Drag over
            {
                setOnDragOver(event -> {
                    Dragboard dragboard = event.getDragboard();
                    if (dragboard.hasString()) {
                        int index = Integer.parseInt(dragboard.getString());
                        if (getIndex() != index) {
                            event.acceptTransferModes(TransferMode.MOVE);
                            event.consume();
                        }
                    }
                });
            }

            // Drop
            {
                setOnDragDropped(event -> {
                    Dragboard dragboard = event.getDragboard();
                    if (dragboard.hasString()) {
                        int index = Integer.parseInt(dragboard.getString());
                        Song song = tableView.getItems().remove(index);

                        int dropIndex;
                        if (isEmpty()) {
                            dropIndex = tableView.getItems().size();
                        } else {
                            dropIndex = getIndex();
                        }

                        tableView.getItems().add(dropIndex, song);
                        event.setDropCompleted(true);
                        tableView.getSelectionModel().select(dropIndex);
                        event.consume();
                        musicPlayer.playlist.rebuildFrom(new ArrayList<>(tableView.getItems()));
                    }
                });
            }

            // Right Click
            {
                ContextMenu contextMenu = new ContextMenu();
                SeparatorMenuItem separator = new SeparatorMenuItem();
                Menu addToPlaylistMenu = new Menu("Add to Playlist");

                // Show playlists menu
                contextMenu.setOnShowing(evt -> {
                    addToPlaylistMenu.getItems().clear();

                    Song song = getItem();
                    if (song == null) return;

                    MenuItem createPlaylistItem = new MenuItem("Create New Playlist");
                    addToPlaylistMenu.getItems().add(createPlaylistItem);
                    addToPlaylistMenu.getItems().add(separator);

                    ArrayList<Playlist> playlists = database.getPlaylists();

                    for (Playlist playlist : playlists) {
                        MenuItem playlistItem = new MenuItem(playlist.getName());
                        playlistItem.setOnAction(e -> {
                            try {
                                database.insertSongIntoPlaylist(song.getId(), playlist.getId());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                        addToPlaylistMenu.getItems().add(playlistItem);
                    }

                });

                MenuItem temp = new MenuItem("Edit TBD");
                contextMenu.getItems().addAll(addToPlaylistMenu, temp);

                emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
                    setContextMenu(isEmpty ? null : contextMenu);
                });
            }
        });
    }

    private void setupDeletionHandler(TableView<Song> tableView) {
        tableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                Song selectedSong = tableView.getSelectionModel().getSelectedItem();
                if (selectedSong != null) {
                    songList.remove(selectedSong);
                    musicPlayer.playlist.remove(selectedSong);
                    database.deleteSong(selectedSong);
                }
            }
        });
    }
}
