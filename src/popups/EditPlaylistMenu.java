package popups;

import containers.Playlist;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.*;

import main.Database;
import main.LibraryMenu;
import main.MusicPlayer;

import java.io.File;

public class EditPlaylistMenu {

    private Database database;
    private MusicPlayer musicPlayer;
    private LibraryMenu libraryMenu;
    private Playlist oldPlaylist;
    private Playlist playlist;


    private final SimpleStringProperty name = new SimpleStringProperty();

    @FXML private TextField textField;
    @FXML private Button cancelButton;
    @FXML private Button confirmButton;
    @FXML private Button chooseCoverButton;
    @FXML private ImageView coverImage;

    private File file;

    public EditPlaylistMenu() {}

    //! SAVE PHOTO
    public void initializeEditPlaylistMenu(Window window, Parent parent, Database database, MusicPlayer musicPlayer, LibraryMenu libraryMenu, Playlist playlist) {
        this.database = database;
        this.musicPlayer = musicPlayer;

        this.libraryMenu = libraryMenu;
        this.playlist = playlist;
        this.oldPlaylist = playlist;

        /*
         * TODO DOCUMENTATION
         */
        textField.textProperty().bindBidirectional(name);
        name.setValue(playlist.getName());

        if (playlist.getFilePath() != null) {
            file = new File(playlist.getFilePath());
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                coverImage.setImage(image);
            }
        }





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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Change Cover Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));
        chooseCoverButton.setOnAction(evt -> {
            file = fileChooser.showOpenDialog(chooseCoverButton.getScene().getWindow());
            if (file != null) {
                Image image = new Image(file.toURI().toString());
                coverImage.setImage(image);
            }
        });



        // --- xxx ---
        Stage stage = new Stage(StageStyle.UNDECORATED);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(window);

        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.setTitle("Edit Playlist");
        stage.showAndWait();
    }



    //TODO check file
    private boolean editPlaylist() {
        if (name.get() == null || name.isEmpty().get()) {
            textField.setStyle("-fx-border-color: red;");
            textField.setOnKeyPressed(e -> textField.setStyle(""));
            return false;
        }
        if (file != null) {
            playlist.setName(name.get());
            playlist.setFilePath(file.getAbsolutePath());

        } else {
            playlist.setName(name.get());
            playlist.setFilePath(null);
        }

        database.editPlaylist(playlist);
        libraryMenu.editPlaylist(oldPlaylist, playlist);
        int i = musicPlayer.getPlaylistSequence().indexOf(oldPlaylist);
        musicPlayer.getPlaylistSequence().set(i, playlist);
        return true;
    }
}
