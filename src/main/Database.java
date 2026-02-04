package main;

import containers.Song;
import containers.Playlist;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;

public class Database {

    private final Connection connection;

    /**
     *  Constructor for the Database class.
     *  Uses PostgreSQL JDBC to connect to the database.
     *  @param url      the database URL
     *  @param user     the database username
     *  @param password the database password
     */
    public Database(String url, String user, String password) throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        connection = DriverManager.getConnection(url, user, password);
    }



    /**
     *  Function adding a song to the database.
     *  Also retrieves the generated ID and sets it to the Song object.
     *  @param song Song class object
     */
    public void addSong(Song song) {
        String sql = "INSERT INTO songs(title, artist, duration, format, filepath) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, song.getTitle());
            statement.setString(2, song.getArtist());
            statement.setInt(3, song.getDuration());
            statement.setString(4, song.getFormat());
            statement.setString(5, song.getFilepath());
            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt(1);
                    song.setId(id);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Function deleting a song from the database.
     *  @param song Song class object
     */
    public void deleteSong(Song song) {
        String sql = "DELETE FROM songs WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, song.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Function retrieving all songs from the database.
     *  If the specified file path does not exist,
     *  the song is deleted from the database.
     *  @return ArrayList of Song objects
     */
    public ArrayList<Song> getSongs() {

        ArrayList<Song> songs = new ArrayList<>();

        String sql = "SELECT * FROM songs";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String artist = resultSet.getString("artist");
                int duration = resultSet.getInt("duration");
                String format = resultSet.getString("format");
                String filepath = resultSet.getString("filepath");

                if (!Files.exists(Paths.get(filepath))) {
                    try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM songs WHERE id = ?")) {
                        deleteStatement.setInt(1, id);
                        deleteStatement.executeUpdate();
                        continue;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                Song song = new Song(id, title, artist, duration, format, filepath);
                songs.add(song);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return songs;
    }



    /**
     *  Function adding a playlist to the database.
     *  Also retrieves the generated ID and sets it to the Playlist object.
     *  @param playlist Playlist class object
     */
    public void addPlaylist(Playlist playlist) {
        String sql = "INSERT INTO playlists(name, filepath) VALUES (?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, playlist.getName());
            statement.setString(2, playlist.getFilePath());
            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt(1);
                    playlist.setId(id);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     *
     * @param playlist
     */
    public void editPlaylist(Playlist playlist) {
        String sql = "INSERT INTO playlists(name, filepath) VALUES (?, ?)";
        sql = "UPDATE playlists " +
                "SET name = ?, filepath = ? " +
                "WHERE id = ?;";

        // Dont need to return key here
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playlist.getName());
            statement.setString(2, playlist.getFilePath());
            statement.setInt(3, playlist.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    /**
     *  Function deleting a playlist from the database.
     *  @param playlist Playlist class object
     */
    public void deletePlaylist(Playlist playlist) {
        String sql = "DELETE FROM playlists WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, playlist.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Function retrieving all playlists from the database.
     *  @return ArrayList of Playlist objects
     */
    public ArrayList<Playlist> getPlaylists() {

        ArrayList<Playlist> playlists = new ArrayList<>();

        String sql = "SELECT * FROM playlists";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String filepath = resultSet.getString("filepath");

                Playlist playlist = new Playlist(id, name, filepath);
                playlists.add(playlist);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return playlists;
    }



    /**
     *  Function inserting a song into a playlist.
     *  @param songId ID of the song to insert
     *  @param playlistId ID of the playlist to insert into
     */
    public void insertSongIntoPlaylist(int songId, int playlistId) {
        String sql = "INSERT INTO playlist_songs(playlist_id, song_id) VALUES (?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, playlistId);
            statement.setInt(2, songId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Function retrieving all songs in a playlist.
     *  @param playlist Playlist class object
     *  @return ArrayList of Song objects.
     */
    public ArrayList<Song> getSongsInPlaylist(Playlist playlist) {
        ArrayList<Song> songs = new ArrayList<>();

        String sql =
                "SELECT song.id, song.title, song.artist, song.duration, song.format, song.filepath " +
                "FROM playlist_songs playlist_song " +
                "JOIN songs song ON song.id = playlist_song.song_id " +
                "WHERE playlist_song.playlist_id = ? " +
                "ORDER BY playlist_song.song_id";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, playlist.getId());

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String artist = resultSet.getString("artist");
                int duration = resultSet.getInt("duration");
                String format = resultSet.getString("format");
                String filepath = resultSet.getString("filepath");

                Song song = new Song(id, title, artist, duration, format, filepath);
                songs.add(song);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return songs;
    }

    /**
     *  Function retrieving the size of a playlist.
     *  @param playlist Playlist class object
     *  @return size of the playlist as an integer
     */
    public int getPlaylistSize(Playlist playlist) {
        String sql = "SELECT COUNT(*) FROM playlist_songs WHERE playlist_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, playlist.getId());

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int size = resultSet.getInt(1);
                System.out.println(size);
                return size;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
