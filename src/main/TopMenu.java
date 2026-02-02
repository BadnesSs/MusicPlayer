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

        createPlaylist.setOnAction(evt -> libraryMenu.createPlaylist());

        quit.setOnAction(evt -> Platform.exit());
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
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aiff"));
        File file = fileChooser.showOpenDialog(fileMenu.getScene().getWindow());

        Song song = Extractor.extract(file);

        database.addSong(song);
        musicPlayer.addSong(song);
        library.addSong(song);
    }

    public void addFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder");
        File file = directoryChooser.showDialog(fileMenu.getScene().getWindow());

        Queue<File> queue = FolderScanner.scanFolder(file);

        if (queue == null) {
            return;
        }

        while (!queue.isEmpty()) {
            File songFile = queue.poll();

            Song song = Extractor.extract(songFile);

            database.addSong(song);
            musicPlayer.addSong(song);
            library.addSong(song);
        }
    }
}
