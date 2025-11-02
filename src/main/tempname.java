package main;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public class tempname {

    private BorderPane root;

    private Database database;

    public tempname() {

        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("config.properties")) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");

        try {
            database = new Database(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        MusicPlayer musicPlayer = new MusicPlayer(database);

        root = new BorderPane();

        try {
            FXMLLoader loader;

            loader = new FXMLLoader(getClass().getResource("/fxml/Player.fxml"));
            Parent bottomNode = loader.load();
            root.setBottom(bottomNode);
            Player player = loader.getController();
            player.initializePlayer(musicPlayer);

            loader = new FXMLLoader(getClass().getResource("/fxml/Library.fxml"));
            Parent centerNode = loader.load();
            root.setCenter(centerNode);
            Library library = loader.getController();
            library.initializeLibrary(database, musicPlayer, player);

            loader = new FXMLLoader(getClass().getResource("/fxml/LibraryMenu.fxml"));
            Parent leftNode = loader.load();
            root.setLeft(leftNode);
            LibraryMenu libraryMenu = loader.getController();
            libraryMenu.initializeLibraryMenu(database, musicPlayer, library);

            loader = new FXMLLoader(getClass().getResource("/fxml/TopMenu.fxml"));
            Parent topNode = loader.load();
            root.setTop(topNode);
            TopMenu topMenu = loader.getController();
            topMenu.initializeMenu(database, musicPlayer, library, libraryMenu);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public BorderPane getRoot() { return root; }
}
