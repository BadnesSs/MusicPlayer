package popups;

import containers.Playlist;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import main.Database;
import main.LibraryMenu;


public class CreatePlaylistMenu {

    public CreatePlaylistMenu() {}

    public void initializeCreatePlaylistMenu(Window window, Parent parent, Database database, LibraryMenu libraryMenu) {
        this.database = database;
        this.libraryMenu = libraryMenu;

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

        Stage stage = new Stage(StageStyle.UNDECORATED);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(window);

        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.setTitle("Create New Playlist");
        stage.showAndWait();
    }



    Database database;
    LibraryMenu libraryMenu;

    SimpleStringProperty name = new SimpleStringProperty();

    @FXML private TextField textField;



    private void createPlaylist() {
        Playlist CreatePlaylistMenu = new Playlist(0, name.get());
        database.addPlaylist(CreatePlaylistMenu);
        libraryMenu.addPlaylist(CreatePlaylistMenu);
    }
}
