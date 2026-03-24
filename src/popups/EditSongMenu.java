package popups;

import main.Database;
import main.Library;
import main.MusicPlayer;

import containers.Song;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.*;

import java.io.File;

public class EditSongMenu {
    private Database database;
    private MusicPlayer musicPlayer;
    private Library library;
    private Song song;

    private final SimpleStringProperty title = new SimpleStringProperty();
    private final SimpleStringProperty artist = new SimpleStringProperty();

    @FXML private TextField textFieldTitle;
    @FXML private TextField textFieldArtist;
    @FXML private Button cancelButton;
    @FXML private Button confirmButton;
    @FXML private Button chooseCoverButton;
    @FXML private ImageView coverImage;

    private File file;

    public EditSongMenu() {}

    public void initializeEditSongMenu(Window window, Parent parent, Database database, MusicPlayer musicPlayer, Library library, Song song) {
        this.database = database;
        this.musicPlayer = musicPlayer;

        this.library = library;
        this.song = song;

        /*
         * Initialize the popup
         * with existing
         * playlist data
         */
        textFieldTitle.textProperty().bindBidirectional(title);
        title.setValue(song.getTitle());

        textFieldArtist.textProperty().bindBidirectional(artist);
        artist.setValue(song.getArtist());

        /*if (playlist.getFilePath() != null) {
            file = new File(playlist.getFilePath());
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                coverImage.setImage(image);
            }
        }*/



        /*
         * Buttons to CANCEL & CONFIRM creation of playlist.
         * Both buttons are set as CANCEL/DEFAULT button,
         * so they consume the VK_ESC/VK_ENTER events.
         */
        cancelButton.setOnAction(evt -> {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });

        confirmButton.setOnAction(evt -> {
            if (editPlaylist()) {
                Stage stage = (Stage) confirmButton.getScene().getWindow();
                stage.close();
            }
        });



        /*
         * TODO DOCUMENTATION
         */

        // ADD MORE FORMAAAAAAAAAAAAATS FOR IMAGEEEEEEEEEEEEEES YOOOOO
        /*FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Change Cover Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));
        chooseCoverButton.setOnAction(evt -> {
            file = fileChooser.showOpenDialog(chooseCoverButton.getScene().getWindow());
            if (file != null) {
                Image image = new Image(file.toURI().toString());
                coverImage.setImage(image);
            }
        });*/



        // --- xxx ---
        Stage stage = new Stage(StageStyle.UNDECORATED);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(window);

        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.setTitle("Edit Song");
        stage.showAndWait();
    }



    private boolean editPlaylist() {
        if (title.get() == null || title.isEmpty().get()) {
            textFieldTitle.setStyle("-fx-border-color: red;");
            textFieldTitle.setOnKeyPressed(e -> textFieldTitle.setStyle(""));
            return false;
        }

        if (artist.get() == null || artist.isEmpty().get()) {
            textFieldArtist.setStyle("-fx-border-color: red;");
            textFieldArtist.setOnKeyPressed(e -> textFieldArtist.setStyle(""));
            return false;
        }


        /*if (file != null) {
            playlist.setName(name.get());
            playlist.setFilePath(file.getAbsolutePath());

        } else {
            playlist.setName(name.get());
            playlist.setFilePath(null);
        }*/

        database.editSong(song);
        library.editSong(song);
        return true;
    }
}
