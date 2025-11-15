package main;

import containers.Song;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;

import java.io.File;



public class Extractor {

    /**
     * Extracts metadata from an audio file and returns a Song object.
     * If artist metadata is missing, it defaults to "Unknown Artist".
     * @param file the audio file from which to extract metadata.
     * @return Song object with extracted metadata, or null if extraction fails.
     */
    public static Song extract(File file) {
        try {
            AudioFile audioFile = AudioFileIO.read(file);
            AudioHeader header = audioFile.getAudioHeader();

            String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
            String artist = audioFile.getTag().getFirst(FieldKey.ARTIST);
            if (artist == null || artist.isEmpty()) artist = "Unknown Artist";
            artist = artist.replace("\0", ""); // .wav contains null character in artist tag
            int duration = header.getTrackLength();
            String format = header.getFormat().toLowerCase();
            String filepath = file.getAbsolutePath();

            return new Song(0, name, artist, duration, format, filepath);

        } catch (Exception e) {
            System.out.println("Failed to read: " + file.getAbsolutePath());
            e.printStackTrace();
            return null;
        }
    }
}
