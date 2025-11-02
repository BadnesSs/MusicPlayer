package main;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Queue;

public class FolderScanner {

    Queue<File> folderQueue = new ArrayDeque<>();

    public FolderScanner(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() &&
                   (file.getName().toLowerCase().endsWith(".mp3") ||
                    file.getName().toLowerCase().endsWith(".wav") ||
                    file.getName().toLowerCase().endsWith(".aac") ||
                    file.getName().toLowerCase().endsWith(".pcm"))
                ) {
                    folderQueue.add(file);
                    System.out.println(file.getName());
                }
            }
        }
    }

    public Queue<File> getFolderQueue() {
        return folderQueue;
    }
}
