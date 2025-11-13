package main;

import containers.Song;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;

import java.io.File;

public class Extractor {

    File file;
    AudioFile audioFile;
    AudioHeader header;
    Song song;

    public Extractor(File file) {
        this.file = file;

        try {
            audioFile = AudioFileIO.read(file);
            header = audioFile.getAudioHeader();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
        String artist = audioFile.getTag().getFirst(FieldKey.ARTIST);
        artist = artist.replace("\0", ""); // Remove null characters (.wav is bad format ig)
        int duration = header.getTrackLength();
        String format = header.getFormat().toLowerCase();
        String filepath = file.getAbsolutePath();

        if (artist == null || artist.isEmpty()) {
            artist = "Unknown Artist";
        }

        song = new Song(0, name, artist, duration, format, filepath);
    }

    public Song get() { return song; }
}
