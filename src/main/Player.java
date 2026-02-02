package main;

import containers.Song;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;



public class Player extends HBox {

    MusicPlayer musicPlayer;

    private double lastVolumeValue = 0;
    private boolean userIsSeeking = false;

    private RepeatMode repeatMode = RepeatMode.OFF;



    @FXML private Label title;

    @FXML private ToggleButton playpauseButton;
    public void setPlaypauseSelected(boolean selected) { playpauseButton.setSelected(selected); }
    @FXML private Button previousButton;
    @FXML private Button nextButton;

    @FXML private Button shuffleButton;
    @FXML private Button repeatButton;

    @FXML private Label currentTime;
    @FXML private Slider progressSlider;
    @FXML private Label totalTime;

    @FXML private ToggleButton volumeButton;
    @FXML private StackPane sliderContainer;
    @FXML private Slider volumeSlider;



    public Player() {}

    public void initializePlayer(MusicPlayer musicPlayer) {
        this.musicPlayer = musicPlayer;
        musicPlayer.currentSongProperty().addListener((obs, oldSong, newSong) -> {
            setupProgressSlider();
            System.out.println("triggered");
        });
        initializeMainButtons();
        initializeSecondaryButtons();
        initializeTimeLabels();
        initializeVolumeControls();
        initializeTitle();
    }

    public void initializeMainButtons() {
        playpauseButton.setOnAction(evt -> playOrPause());
        previousButton.setOnAction(evt -> playPrevious());
        nextButton.setOnAction(evt -> playNext());
    }

    public void initializeSecondaryButtons() {
        shuffleButton.setOnAction(evt -> shuffle());
        repeatButton.setOnAction(evt -> repeat());
    }

    private void initializeTimeLabels() {
        currentTime.setText("0:00");
        totalTime.setText("0:00");

        musicPlayer.cachedDurationProperty().addListener((obs, oldVal, newVal) -> {
            totalTime.setText(formatTime(newVal));
        });
    }

    public void initializeVolumeControls() {

        // --- Volume sync ---
        volumeSlider.valueProperty().bindBidirectional(musicPlayer.volumeProperty());
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateVolumeSlider(newVal.doubleValue()));
        Platform.runLater(() -> updateVolumeSlider(volumeSlider.getValue()));

        Popup volumePopup = new Popup();
        volumePopup.getContent().add(sliderContainer);



        // ---  Hide delay ---
        PauseTransition hideDelay = new PauseTransition(Duration.millis(200));
        hideDelay.setOnFinished(evt -> {
            if (!volumeButton.isHover() && !sliderContainer.isHover()) {
                volumePopup.hide();
            }
        });



        // --- Hover behavior ---
        volumeButton.setOnMouseEntered(evt -> {
            Node source = (Node) evt.getSource();
            double x = source.localToScreen(source.getBoundsInLocal()).getMinX();
            double y = source.localToScreen(source.getBoundsInLocal()).getMinY();

            if (!volumePopup.isShowing()) {
                volumePopup.show(source, x, y - volumeSlider.getPrefHeight() - 50);
            }

            hideDelay.stop();
        });

        sliderContainer.setOnMouseEntered(evt -> hideDelay.stop());

        volumeButton.setOnMouseExited(evt -> hideDelay.playFromStart());
        sliderContainer.setOnMouseExited(evt -> hideDelay.playFromStart());



        // --- Action ---
        volumeButton.setOnAction(evt -> {
            if (volumeButton.isSelected()) {
                lastVolumeValue = musicPlayer.getVolume();
                musicPlayer.volumeProperty().set(0);
                updateVolumeSlider(0);
            } else {
                musicPlayer.volumeProperty().set(lastVolumeValue);
                updateVolumeSlider(lastVolumeValue);
            }
        });
    }

    public void initializeTitle() {
        Song current = musicPlayer.getCurrentSong();
        if (current != null) {
            title.setText(current.getTitle());
        } else {
            title.setText("");
        }

        musicPlayer.currentSongProperty().addListener((obs, oldSong, newSong) -> {
            if (newSong != null) {
                title.setText(newSong.getTitle());
            } else {
                if (oldSong != null) {
                    title.setText(oldSong.getTitle());
                }
            }
        });
    }



    private void playOrPause() {
        musicPlayer.playOrPause();
        setupProgressSlider();
    }

    private void playPrevious() {
        playpauseButton.setSelected(true);
        musicPlayer.previous();
        musicPlayer.play();
        setupProgressSlider();
    }

    private void playNext() {
        playpauseButton.setSelected(true);
        musicPlayer.next();
        musicPlayer.play();
        setupProgressSlider();
    }



    private void shuffle() {
        ArrayList<Song> songs = musicPlayer.playlist.toList();
        Collections.shuffle(songs);
        musicPlayer.playlist.rebuildFrom(songs);
    }

    private void repeat() {
        repeatMode = repeatMode.next();

        switch (repeatMode) {
            case OFF:
                repeatButton.getStyleClass().removeAll("repeat-all", "repeat-one");
                break;
            case ALL:
                if (!repeatButton.getStyleClass().contains("repeat-all")) {
                    repeatButton.getStyleClass().add("repeat-all");
                }
                break;
            case ONE:
                if (!repeatButton.getStyleClass().contains("repeat-one")) {
                    repeatButton.getStyleClass().add("repeat-one");
                }
                break;
        }

        if (musicPlayer != null) {
            musicPlayer.setRepeatMode(repeatMode);
        }
    }



    private void setupProgressSlider() {

        // Slider moves as music plays
        musicPlayer.currentTimeProperty().addListener((obs, oldVal, newVal) -> {
            double duration = musicPlayer.getDuration() != null ? musicPlayer.getDuration().toSeconds() : 0;
            if (duration > 0 && !userIsSeeking) {
                double progress = newVal.toSeconds() / duration * 100;

                progressSlider.setValue(progress);

                progressSlider.applyCss();
                Node track = progressSlider.lookup(".track");
                if (track != null) {
                    track.setStyle(String.format(
                            Locale.US,
                            "-fx-background-color: linear-gradient(to right, black %.2f%%, #b3b3b3 %.2f%%);",
                            progress, progress
                    ));
                }
            }

            currentTime.setText(formatTime(newVal));
            totalTime.setText(formatTime(musicPlayer.getDuration()));
        });

        // Handle slider movement by user
        progressSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging) {
                double total = musicPlayer.getDuration() != null ? musicPlayer.getDuration().toSeconds() : 0;
                double seekTime = progressSlider.getValue() / 100. * total;
                musicPlayer.seek(Duration.seconds(seekTime));
            }
        });



        //
        progressSlider.setOnMousePressed(evt -> {
            lastVolumeValue = progressSlider.getValue();
            userIsSeeking = true;

            /*
            //
            // FIX THE SLIDER BUG LATER
            // JAU NE STRIMAICIUI TIKRAI
            Object target = evt.getTarget();
            System.out.println("Target: " + target.getClass().getName());
             */
        });

        progressSlider.setOnMouseReleased(evt -> {
            double currentValue = progressSlider.getValue();
            double total = musicPlayer.getDuration() != null ? musicPlayer.getDuration().toSeconds() : 0;
            double seekTime = progressSlider.getValue() / 100. * total;
            musicPlayer.seek(Duration.seconds(seekTime));
            userIsSeeking = false;
        });
    }

    private String formatTime(Duration duration) {
        if (duration == null || duration.isUnknown()) return "00:00";

        int minutes = (int) duration.toMinutes();
        int seconds = (int) (duration.toSeconds() % 60);
        return String.format("%d:%02d", minutes, seconds);
    }

    private void updateVolumeSlider(double value) {
        volumeSlider.applyCss();
        Node track = volumeSlider.lookup(".track");
        if (track != null) {
            track.setStyle(String.format(
                    Locale.US,
                    "-fx-background-color: linear-gradient(to top, black %.2f%%, #b3b3b3 %.2f%%);",
                    value, value
            ));
        }

        if (value == 0) {
            volumeButton.setSelected(true);
        } else {
            volumeButton.setSelected(false);
        }
    }



    /*
     * Deprecated features
     * will be deleted later
     * FXML elements are already removed
     */



    /*
    @FXML private Button randomButton;

    public void initializeSecondaryButtons() {
        shuffleButton.setOnAction(evt -> shuffle());
        repeatButton.setOnAction(evt -> repeat());
        randomButton.setOnAction(evt -> random());
    }

    private void random() {
        playpauseButton.setSelected(true);
        musicPlayer.stop();
        musicPlayer.playlist.moveRandom();
        play();
    }
     */



    /*
    private void play() {
        musicPlayer.play();
        setupProgressSlider();
    }
     */
}
