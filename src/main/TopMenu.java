package main;

import containers.Song;

import popups.CreatePlaylistMenu;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;

import java.io.File;
import java.util.Queue;

public class TopMenu extends HBox {

    Database database;
    MusicPlayer musicPlayer;

    Library library;
    LibraryMenu libraryMenu;

    // FXML
    @FXML private MenuButton fileMenu;

    @FXML private MenuItem addSong;
    @FXML private MenuItem addFolder;

    @FXML private MenuItem createPlaylist;

    @FXML private MenuItem quit;



    //
    //
    // DSA
    @FXML private MenuItem sortByIdButton;
    @FXML private MenuItem sortByArtistButton;
    @FXML private MenuItem getHistoryButton;
    //
    //
    //



    public TopMenu() {}

    public void initializeMenu(Database database, MusicPlayer musicPlayer, Library library, LibraryMenu libraryMenu) {
        this.database = database;
        this.musicPlayer = musicPlayer;

        this.library = library;
        this.libraryMenu = libraryMenu;

        initializeButtons();
    }

    public void initializeButtons() {
        addSong.setOnAction(evt -> addSong());
        addFolder.setOnAction(evt -> addFolder());

        createPlaylist.setOnAction(evt -> createPlaylist());

        quit.setOnAction(evt -> Platform.exit());

        //
        //
        //
        sortByIdButton.setOnAction(evt -> musicPlayer.playlist.quickSort(Song.BY_ID));
        sortByArtistButton.setOnAction(evt -> musicPlayer.playlist.quickSort(Song.BY_ARTIST));
        getHistoryButton.setOnAction(evt -> displayStack());
        //
        //
        //
    }

    //
    public void displayStack() {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.initOwner(fileMenu.getScene().getWindow());
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.setTitle("Playback History");

        VBox root = new VBox(8);
        root.setPadding(new javafx.geometry.Insets(10));

        ListView<String> listView = new ListView<>();
        while (!musicPlayer.songStack.isEmpty()) {
            Song song = musicPlayer.songStack.pop();
            listView.getItems().add(song.getTitle());
        }

        Button close = new Button("Close");
        close.setOnAction(evt -> stage.close());

        root.getChildren().addAll(listView, close);

        stage.setScene(new javafx.scene.Scene(root, 400, 300));
        stage.showAndWait();
    }
    //

    public void addSong() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Audio File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aac", "*.pcm"));
        File file = fileChooser.showOpenDialog(fileMenu.getScene().getWindow());

        Extractor extractor = new Extractor(file);
        Song song = extractor.get();

        database.addSong(song);
        musicPlayer.addSong(song);
        library.addSong(song);
    }

    public void addFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder");
        File file = directoryChooser.showDialog(fileMenu.getScene().getWindow());
        FolderScanner folderScanner = new FolderScanner(file);
        Queue<File> folderQueue = folderScanner.getFolderQueue();
        while (!folderQueue.isEmpty()) {
            File songFile = folderQueue.poll();

            Extractor extractor = new Extractor(songFile);
            Song song = extractor.get();

            database.addSong(song);
            musicPlayer.addSong(song);
            library.addSong(song);
        }
    }

    public void createPlaylist() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PlaylistPopup.fxml"));
            Parent parent = loader.load();

            Window window = fileMenu.getScene().getWindow();

            CreatePlaylistMenu createPlaylistMenu = loader.getController();
            createPlaylistMenu.initializeCreatePlaylistMenu(window, parent, database, libraryMenu);

        }   catch (Exception e) {
            e.printStackTrace();
        }
    }
}
