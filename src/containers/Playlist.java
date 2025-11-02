package containers;

public class Playlist {

    public Playlist(int id, String name) {
        this.id = id;
        this.name = name;
        // this.imagePath = imagePath;
    }

    private int id;
    public void setId(int id) { this.id = id; }
    public int getId() { return id; }

    private String name;
    public String getName() { return name; }

    private String thumbnailPath;
    public String getThumbnailPath() { return thumbnailPath; }
}
