package main;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Queue;

public class FolderScanner {

    /**
     *  Scans a folder for audio files and returns them.
     *  @param folder directory to scan for supported audio formats.
     *  @return Queue of File objects, matching supported audio formats.
     */
    public static Queue<File> scanFolder(File folder) {
        Queue<File> queue = new ArrayDeque<File>();

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() &&
                   (file.getName().toLowerCase().endsWith(".mp3") ||
                    file.getName().toLowerCase().endsWith(".wav") ||
                    file.getName().toLowerCase().endsWith(".aiff"))
                ) {
                    queue.add(file);
                }
            }
            return queue;
        } else {
            return null;
        }


    }
}
