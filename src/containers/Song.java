package containers;

import java.util.Comparator;



public class Song {

    // Destytojui quicksort
    public static final Comparator<Song> BY_ID = Comparator.comparingInt(Song::getId);
    public static final Comparator<Song> BY_ARTIST = Comparator.comparing(Song::getArtist);
    //

    public Song(int id, String title, String artist, int duration, String format, String filePath) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.format = format;
        this.filePath = filePath;
    }

    private int id;
    public int setId(int id) { return this.id = id; }
    public int getId() { return id; }

    private String title;
    public String getTitle() { return title; }

    private String artist;
    public String getArtist() { return artist; }

    private int duration;
    public int getDuration() { return duration; }

    private String format;
    public String getFormat() { return format; }

    private String filePath;
    public String getFilepath() { return filePath; }
}
