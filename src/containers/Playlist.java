package containers;

public class Playlist {

    public Playlist(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Playlist(int id, String name, String thumbnailPath) {
        this.id = id;
        this.name = name;
        this.filePath = thumbnailPath;
    }

    private int id;
    public void setId(int id) { this.id = id; }
    public int getId() { return id; }

    private String name;
    public String getName() { return name; }

    private String filePath;
    public String getFilePath() { return filePath; }
}
