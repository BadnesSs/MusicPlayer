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

import java.io.File;


public class CreatePlaylistMenu {

    private Database database;
    private LibraryMenu libraryMenu;

    private final SimpleStringProperty name = new SimpleStringProperty();

    @FXML private TextField textField;
    @FXML private Button chooseCoverButton;
    @FXML private ImageView coverImage;

    private File file;

    public CreatePlaylistMenu() {}

    public void initializeCreatePlaylistMenu(Window window, Parent parent, Database database, LibraryMenu libraryMenu) {
        this.database = database;
        this.libraryMenu = libraryMenu;

        /*
         * TODO DOCUMENTATION
         */
        textField.textProperty().bindBidirectional(name);
        textField.setOnKeyPressed(evt -> {
            switch (evt.getCode()) {
                case ENTER -> {
                    createPlaylist();
                    Stage stage = (Stage) textField.getScene().getWindow();
                    stage.close();
                }
                case ESCAPE -> {
                    Stage stage = (Stage) textField.getScene().getWindow();
                    stage.close();
                }
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



    private void createPlaylist() {
        Playlist playlist;
        if (file != null) {
            playlist = new Playlist(0, name.get(), file.getAbsolutePath());

        } else {
            playlist = new Playlist(0, name.get());
        }

        database.addPlaylist(playlist);
        libraryMenu.addPlaylist(playlist);
    }
}
