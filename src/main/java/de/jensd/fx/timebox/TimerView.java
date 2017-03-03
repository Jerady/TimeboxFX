/**
 * Copyright (c) 2012-2017 Jens Deters http://www.jensd.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *
 */
package de.jensd.fx.timebox;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Screen;
import javafx.util.Duration;

/**
 *
 * @author Jens Deters
 */
public class TimerView extends StackPane {

    private Button startButton;
    private ToggleButton pauseButton;
    private Button stopButton;
    private Button aboutButton;
    private VBox stageBar;
    private Arc timeLeftSliceArc;
    private Arc timeLeftBackgroundSliceArc;
    private Circle backgroundSliceArc;
    private Circle centerSliceArc;
    private Text counterText;
    private Text titleText;
    private Text copyrightText;
    private Text versionText;
    private Group clockPane;
    private HBox clockButtonsBox;
    private VBox appInfoBox;

    private DoubleProperty startLengthProperty;
    private DoubleProperty scaleProperty;
    private DoubleProperty centerXProperty;
    private DoubleProperty centerYProperty;
    private BooleanProperty audioOnProperty;

    private TimerClockWork timerClockWork;
    private AudioClip alarmPlayer;
    private AudioClip clickPlayer;
    private Properties appProperties;
    private Preferences userPreferences;
    public final static String PROPERTIES_FILE = "/timebox.properties";
    private ModalDimmer modalDimmer;
    private Pane settingsPane;

    public TimerView() {
        init();
    }

    private void init() {
        appProperties = loadAppProperties();
        timerClockWork = new TimerClockWork();
        modalDimmer = new ModalDimmer();
        settingsPane = createAboutPane();
        scaleProperty = new SimpleDoubleProperty(200);
        centerXProperty = new SimpleDoubleProperty(200);
        centerYProperty = new SimpleDoubleProperty(200);
        startLengthProperty = new SimpleDoubleProperty(0);
        String alarmSource = getClass().getResource(appProperties.getProperty("clip.source.alarm")).toExternalForm();
        String clickSource = getClass().getResource(appProperties.getProperty("clip.source.click")).toExternalForm();
        alarmPlayer = new AudioClip(alarmSource);
        clickPlayer = new AudioClip(clickSource);
        clickPlayer.setVolume(0.05);
        timeLeftSliceArc = new Arc();
        timeLeftSliceArc.getStyleClass().setAll("time-slice-arc-green");
        timeLeftSliceArc.setLength(0.0);
        timeLeftSliceArc.setStartAngle(90.0);
        timeLeftSliceArc.setType(ArcType.ROUND);
        timeLeftBackgroundSliceArc = new Arc();
        timeLeftBackgroundSliceArc.setFill(new Color(1, 1, 1, 0.9));
        timeLeftBackgroundSliceArc.setLength(360.0);
        timeLeftBackgroundSliceArc.setStartAngle(90.0);
        timeLeftBackgroundSliceArc.setType(ArcType.ROUND);
        timeLeftBackgroundSliceArc.getStyleClass().setAll("timer-background-slice-arc");
        backgroundSliceArc = new Circle();
        backgroundSliceArc.getStyleClass().setAll("background-slice-arc");
        centerSliceArc = new Circle(75.0);
        centerSliceArc.getStyleClass().setAll("center-slice-arc");
        counterText = new Text();
        counterText.getStyleClass().setAll("counter-text-normal");
        titleText = new Text(appProperties.getProperty("app.name"));
        titleText.getStyleClass().setAll("title-text");
        copyrightText = new Text(appProperties.getProperty("app.copyright"));
        copyrightText.getStyleClass().setAll("copyright-text");
        versionText = new Text(appProperties.getProperty("app.version"));
        versionText.getStyleClass().setAll("version-text");
        startButton = new Button("");
        startButton.setGraphic(new MaterialDesignIconView(MaterialDesignIcon.PLAY));
        startButton.getStyleClass().setAll("navigation-button");
        pauseButton = new ToggleButton("");
        pauseButton.setGraphic(new MaterialDesignIconView(MaterialDesignIcon.PAUSE));
        pauseButton.getStyleClass().setAll("navigation-button");
        stopButton = new Button("");
        stopButton.setGraphic(new MaterialDesignIconView(MaterialDesignIcon.STOP));
        stopButton.getStyleClass().setAll("navigation-button");
        clockButtonsBox = new HBox(pauseButton, startButton, stopButton);
        aboutButton = new Button("");
        aboutButton.setGraphic(new MaterialDesignIconView(MaterialDesignIcon.HELP_CIRCLE_OUTLINE));
        aboutButton.getStyleClass().setAll("navigation-button");
        appInfoBox = new VBox(titleText, versionText, copyrightText);
        appInfoBox.setAlignment(Pos.CENTER);
        clockPane = new Group();
        clockPane.getChildren().
                addAll(backgroundSliceArc, timeLeftBackgroundSliceArc, timeLeftSliceArc, createTickGroup(),
                        centerSliceArc, counterText,
                        clockButtonsBox, appInfoBox);
        stageBar = new VBox(5);
        stageBar.setAlignment(Pos.TOP_CENTER);
        Region spacer = new Region();
        spacer.setPrefHeight(20);
        stageBar.getChildren().addAll(aboutButton);
        AnchorPane.setTopAnchor(stageBar, 0.0);
        AnchorPane.setRightAnchor(stageBar, 0.0);
        AnchorPane root = new AnchorPane();
        root.getStyleClass().setAll("root-pane");
        root.getChildren().addAll(clockPane, stageBar);
        Rectangle2D r = Screen.getPrimary().getBounds();
        getStyleClass().setAll("root-pane");
        getChildren().addAll(root, modalDimmer);
        attachEventHandler();
        bindLayoutProperties(root);
        bindTimerProperties();
        attachKeyEventsListener();
        userPreferences = loadUserPreferences();

    }

    private void attachKeyEventsListener() {
        addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            event.consume();
            switch (event.getCode()) {
                case SPACE:
                    toggleRunningStatus();
                    break;
                case LEFT:
                case UP:
                    increateStartTime();
                    break;
                case RIGHT:
                case DOWN:
                    decreaseStartTime();
                    break;
                case ENTER:
                case ESCAPE:
                    resetStartTime();
                    break;
            }
        });
    }

    private void increateStartTime() {
        double value = startLengthProperty.get() + 1.0;
        if(value > 60.0){
            value = 1.0;
        }
        startLengthProperty.set(value);
    }

    private void decreaseStartTime() { 
        double value = startLengthProperty.get() - 1.0;
        if(value < 0.0){
            value+=60.0;
        }
        startLengthProperty.set(value);
    }

    private void resetStartTime() {
        onStop();
        startLengthProperty.set(0.0);
    }

    private void toggleRunningStatus() {
        if (timerClockWork.isRunning()) {
            onStop();
        } else {
            onStart();
        }
    }

    private Preferences loadUserPreferences() {
        Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
        timerClockWork.overdueOnProperty().set(userPrefs.getBoolean("overdueOn", false));
        audioOnProperty().set(userPrefs.getBoolean("audioOn", true));
        return userPrefs;
    }

    public void storePreferences(Preferences preferences) {
        preferences.putBoolean("overdueOn", timerClockWork.overdueOnProperty().get());
        preferences.putBoolean("audioOn", isAudioOn());
        try {
            preferences.sync();
        } catch (BackingStoreException ex) {
            Logger.getLogger(Main.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }

    private Properties loadAppProperties() {
        Properties props = new Properties();
        try {
            props.load(getClass().
                    getResourceAsStream(PROPERTIES_FILE));
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        return props;
    }

    public Properties getAppProperties() {
        return appProperties;
    }

    public Preferences getUserPreferences() {
        return userPreferences;
    }

    public void showModalMessage(Node dialogNode) {
        modalDimmer.showModalMessage(dialogNode);
    }

    public void hideModalMessage() {
        modalDimmer.hideModalMessage();
    }

    public void onExitApp() {
        storePreferences(userPreferences);
        Platform.exit();
    }

    private void onStart() {
        if (timerClockWork.isRunning()) {
            return;
        }
        alarmPlayer.stop();
        timerClockWork.setStartTimeMinutes(startLengthProperty.intValue());
        // execute on startanimation finished:
        EventHandler<ActionEvent> startTimer = (ActionEvent t) -> {
            timerClockWork.stop();
            timerClockWork.start();
        };
        FadeTransition fade = new FadeTransition(Duration.millis(550), timeLeftSliceArc);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setAutoReverse(true);
        fade.play();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(550),
                startTimer,
                new KeyValue(timerClockWork.milliSecondsLeftProperty(),
                        startLengthProperty.multiply(60000.0).get(),
                        Interpolator.EASE_BOTH)));
        timeline.play();
    }

    private void onStop() {
        if (!timerClockWork.isRunning()) {
            return;
        }
        alarmPlayer.stop();
        timerClockWork.stop();
        // Stopanimation:
        FadeTransition fade = new FadeTransition(Duration.millis(550), timeLeftSliceArc);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setAutoReverse(true);
        fade.play();
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.millis(550),
                new KeyValue(timerClockWork.milliSecondsLeftProperty(),
                        startLengthProperty.get() / 60000,
                        Interpolator.EASE_BOTH)));
        timeline.play();
    }

    private void attachEventHandler() {
        timerClockWork.modeProperty().
                addListener((ObservableValue<? extends TimerClockWork.Mode> ov, TimerClockWork.Mode oldMode, TimerClockWork.Mode newMode) -> {
                    switch (newMode) {
                        case NORMAL:
                            normalMode();
                            break;
                        case OVERDUE:
                            overdueMode();
                            break;
                    }
                });

        timeLeftBackgroundSliceArc.
                setOnMousePressed((MouseEvent mouseEvent) -> {
                    if (timerClockWork.runningProperty().not().get()) {
                        setTimeStartValue(mouseEvent);
                        mouseEvent.consume();
                    }
                });

        timeLeftBackgroundSliceArc.
                setOnMouseDragged((MouseEvent mouseEvent) -> {
                    if (timerClockWork.runningProperty().not().get()) {
                        setTimeStartValue(mouseEvent);
                        mouseEvent.consume();
                    }
                });

        backgroundSliceArc.setOnMousePressed((MouseEvent mouseEvent) -> {
            if (timerClockWork.runningProperty().not().get()) {
                setTimeStartValue(mouseEvent);
                mouseEvent.consume();
            }
        });

        backgroundSliceArc.setOnMouseDragged((MouseEvent mouseEvent) -> {
            if (timerClockWork.runningProperty().not().get()) {
                setTimeStartValue(mouseEvent);
                mouseEvent.consume();
            }
        });

        aboutButton.setOnAction((ActionEvent t) -> {
            modalDimmer.showModalMessage(settingsPane);
        });
        startButton.setOnAction((ActionEvent t) -> {
            onStart();
        });
        FadeTransition fadeTransitionButton = new FadeTransition(Duration.millis(600), pauseButton);
        fadeTransitionButton.setFromValue(0.5);
        fadeTransitionButton.setToValue(1.0);
        fadeTransitionButton.setCycleCount(Timeline.INDEFINITE);
        fadeTransitionButton.setInterpolator(Interpolator.EASE_BOTH);
        fadeTransitionButton.setAutoReverse(true);

        pauseButton.selectedProperty().
                addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    if (newValue) {
                        fadeTransitionButton.play();
                        timerClockWork.pause();
                    } else {
                        fadeTransitionButton.stop();
                        timerClockWork.continuePlay();
                    }
                });
        stopButton.setOnAction((ActionEvent t) -> {
            onStop();
        });

    }

    public BooleanProperty audioOnProperty() {
        if (audioOnProperty == null) {
            audioOnProperty = new SimpleBooleanProperty(Boolean.valueOf(appProperties.getProperty("app.audio.on")));
        }
        return audioOnProperty;
    }

    private boolean isAudioOn() {
        return audioOnProperty().get();
    }

    private void playClick() {
        if (isAudioOn()) {
            clickPlayer.play();
        }
    }

    private void playAlarm() {
        if (isAudioOn()) {
            alarmPlayer.play();
        }
    }

    private void bindTimerProperties() {
        startButton.disableProperty().bind(timerClockWork.runningProperty());
        pauseButton.disableProperty().bind(timerClockWork.runningProperty().not());
        stopButton.disableProperty().bind(timerClockWork.runningProperty().not());
        aboutButton.disableProperty().bind(timerClockWork.runningProperty());
        timerClockWork.finishedProperty().
                addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    if (newValue) {
                        playAlarm();
                    }
                });
        timeLeftSliceArc.lengthProperty().bind(timerClockWork.milliSecondsLeftProperty().divide(10000.0));
        timerClockWork.milliSecondsLeftProperty().
                addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
                    String counterString = String.format("%02d:%02d",
                            timerClockWork.minutesProperty().get(),
                            timerClockWork.secondsProperty().get());
                    if (timerClockWork.overdueRunningProperty().get()) {
                        counterString = String.format("%02d:%02d", (timerClockWork.minutesProperty().get() - 59) * -1,
                                (timerClockWork.secondsProperty().get() - 59) * -1);
                    }
                    counterText.setText(counterString);
                });
        timeLeftBackgroundSliceArc.lengthProperty().bind(startLengthProperty.multiply(6));
        startLengthProperty.addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
            playClick();
        });
    }

    private Pane createAboutPane() {
        Button closeButton = new Button("");
        closeButton.setGraphic(new MaterialDesignIconView(MaterialDesignIcon.WINDOW_CLOSE));
        closeButton.getStyleClass().setAll("navigation-button");
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        HBox headerPane = new HBox(closeButton);
        headerPane.setAlignment(Pos.TOP_RIGHT);
        Label appNameLabel = new Label(appProperties.getProperty("app.name"));
        appNameLabel.getStyleClass().setAll("about-appname-text");
        Label copyrightLabel = new Label(appProperties.getProperty("app.copyright"));
        Label versionLabel = new Label("Version " + appProperties.getProperty("app.version"));
        Label javaVersionLabel = new Label(String.format("Java Runtime Version %s (%sbit)", System.getProperty("java.runtime.version"), System.getProperty("sun.arch.data.model")));
        javaVersionLabel.getStyleClass().setAll("dark-label");
        Hyperlink hyperlink = new Hyperlink("www.jensd.de");
        hyperlink.setOnAction((ActionEvent event) -> {
            onOpenHomepage();
        });
        VBox infoBox = new VBox(appNameLabel, versionLabel, copyrightLabel, hyperlink);
        infoBox.setAlignment(Pos.CENTER);
        CheckBox overdueCheckBox = new CheckBox("Allow Overdue");
        CheckBox audioCheckBox = new CheckBox("Enable Sounds");
        closeButton.setOnAction((ActionEvent t) -> {
            hideModalMessage();
        });
        timerClockWork.overdueOnProperty().bindBidirectional(overdueCheckBox.selectedProperty());
        audioOnProperty().bindBidirectional(audioCheckBox.selectedProperty());
        VBox optionsBox = new VBox(overdueCheckBox, audioCheckBox);
        optionsBox.setAlignment(Pos.CENTER);
        VBox aboutPane = new VBox(headerPane, infoBox, optionsBox, javaVersionLabel);
        aboutPane.setAlignment(Pos.TOP_CENTER);
        aboutPane.setMaxWidth(400);
        aboutPane.setMaxHeight(200);
        aboutPane.setSpacing(20.0);
        aboutPane.getStyleClass().setAll("modal-dimmer-dialog");

        return aboutPane;
    }

    public void onOpenHomepage() {
        try {
            Desktop.getDesktop().browse(new URI("http://www.jensd.de"));
        } catch (URISyntaxException | IOException ex) {
            Logger.getGlobal().severe("Error opening the browser.");
        }
    }

    private void normalMode() {
        timeLeftSliceArc.getStyleClass().setAll("time-slice-arc-green");
        counterText.getStyleClass().setAll("counter-text-normal");
    }

    private void overdueMode() {
        timeLeftSliceArc.getStyleClass().setAll("time-slice-arc-overdue");
        counterText.getStyleClass().setAll("counter-text-overdue");
    }

    private void bindLayoutProperties(final Pane root) {
        clockButtonsBox.layoutXProperty().bind(centerXProperty.subtract(51));
        clockButtonsBox.layoutYProperty().bind(centerYProperty.add(20));
        appInfoBox.layoutXProperty().bind(centerXProperty.subtract(60));
        appInfoBox.layoutYProperty().bind(centerYProperty.add(100));
        scaleProperty.bind(root.heightProperty().divide(2.2));
        centerXProperty.bind(root.widthProperty().divide(2));
        centerYProperty.bind(root.heightProperty().divide(2));
        counterText.layoutXProperty().bind(centerXProperty.subtract(counterText.getLayoutBounds().getWidth() / 2).subtract(64));
        counterText.layoutYProperty().bind(centerYProperty.subtract(counterText.getLayoutBounds().getMinY()).subtract(10));
        timeLeftSliceArc.centerXProperty().bind(centerXProperty);
        timeLeftSliceArc.centerYProperty().bind(centerYProperty);
        timeLeftBackgroundSliceArc.centerXProperty().bind(centerXProperty);
        timeLeftBackgroundSliceArc.centerYProperty().bind(centerYProperty);
        backgroundSliceArc.centerXProperty().bind(centerXProperty);
        backgroundSliceArc.centerYProperty().bind(centerYProperty);
        centerSliceArc.centerXProperty().bind(centerXProperty);
        centerSliceArc.centerYProperty().bind(centerYProperty);
        timeLeftSliceArc.radiusXProperty().bind(scaleProperty);
        timeLeftSliceArc.radiusYProperty().bind(scaleProperty);
        timeLeftBackgroundSliceArc.radiusXProperty().bind(scaleProperty);
        timeLeftBackgroundSliceArc.radiusYProperty().bind(scaleProperty);
        backgroundSliceArc.radiusProperty().bind(scaleProperty);
    }

    private void setTimeStartValue(MouseEvent mouseEvent) {
        double x21 = centerXProperty.get();
        double y21 = centerYProperty.get();
        double x22 = mouseEvent.getSceneX();
        double y22 = mouseEvent.getSceneY();
        double dx = x22 - x21;
        double dy = y22 - y21;
        double angle = Math.toDegrees(Math.atan2(dx, dy)) + 180;
        double time = Math.round(angle / 6);
        startLengthProperty.set(time);
    }

    private Node createTick(int n) {
        Rotate rotate = new Rotate(360 / 60 * n);
        rotate.pivotXProperty().bind(centerXProperty);
        rotate.pivotYProperty().bind(centerYProperty);
        Line line = new Line();
        line.getStyleClass().setAll("tick");
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        line.getTransforms().add(rotate);
        double endYfactor = (n % 5 == 0 ? 0.25 : 0.2);
        if (n % 15 == 0) {
            endYfactor = 0.3;
            line.setStroke(Color.YELLOWGREEN);
        }
        line.strokeWidthProperty().bind(scaleProperty.multiply(0.03));
        line.startXProperty().bind(centerXProperty);
        line.startYProperty().bind(scaleProperty.multiply(0.2));
        line.endXProperty().bind(centerXProperty);
        line.endYProperty().bind(scaleProperty.multiply(endYfactor));
        return line;
    }

    private Group createTickGroup() {
        Group clockTickGroup = new Group();
        for (int n = 0; n < 60; n++) {
            Node tick = createTick(n);
            clockTickGroup.getChildren().add(tick);
        }
        return clockTickGroup;
    }

}
