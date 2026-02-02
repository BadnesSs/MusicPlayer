package popups;

import containers.Playlist;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.*;

import main.Database;
import main.LibraryMenu;
import main.MusicPlayer;

import java.io.File;


public class CreatePlaylistMenu {

    private Database database;
    private MusicPlayer musicPlayer;
    private LibraryMenu libraryMenu;

    private final SimpleStringProperty name = new SimpleStringProperty();

    @FXML private TextField textField;
    @FXML private Button cancelButton;
    @FXML private Button confirmButton;
    @FXML private Button chooseCoverButton;
    @FXML private ImageView coverImage;

    private File file;

    public CreatePlaylistMenu() {}

    public void initializeCreatePlaylistMenu(Window window, Parent parent, Database database, MusicPlayer musicPlayer, LibraryMenu libraryMenu) {
        this.database = database;
        this.musicPlayer = musicPlayer;

        this.libraryMenu = libraryMenu;

        /*
         * TODO DOCUMENTATION
         */
        textField.textProperty().bindBidirectional(name);



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
            if (createPlaylist()) {
                Stage stage = (Stage) confirmButton.getScene().getWindow();
                stage.close();
            }
        });



        /*
         * TODO DOCUMENTATION
         */

        // ADD MORE FORMAAAAAAAAAAAAATS FOR IMAGEEEEEEEEEEEEEES YOOOOO
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Cover Image");
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
        stage.setTitle("Create New Playlist");
        stage.showAndWait();
    }



    private boolean createPlaylist() {
        Playlist playlist;
        // TODO CHECK
        if (name.get() == null || name.isEmpty().get()) {
            textField.setStyle("-fx-border-color: red;");
            textField.setOnKeyPressed(e -> textField.setStyle(""));
            return false;
        }
        if (file != null) {
            playlist = new Playlist(0, name.get(), file.getAbsolutePath());

        } else {
            playlist = new Playlist(0, name.get());
        }

        database.addPlaylist(playlist);
        libraryMenu.addPlaylist(playlist);
        musicPlayer.getPlaylistSequence().add(playlist);
        return true;
    }
}
