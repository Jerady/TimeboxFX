package de.jensd.fx.timebox;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 *
 * @author Jens Deters
 */
public class ModalDimmer extends StackPane {

    private final Duration DURATION_HIDE = Duration.millis(700);
    private final Duration DURATION_SHOW = Duration.millis(350);

    public ModalDimmer() {
        init();
    }

    private void init() {
        setId("modal-dimmer");
        setOnMouseClicked(Event::consume);
        setVisible(false);
    }

    public void hideModalMessage() {
        setCache(true);
        Timeline timeline = new Timeline(
                new KeyFrame(DURATION_HIDE, (ActionEvent t) -> {
                    setCache(false);
                    setVisible(false);
                    getChildren().
                            clear();
                },
                        new KeyValue(opacityProperty(), 0, Interpolator.EASE_BOTH)));
        timeline.play();
    }

    public void showModalMessage(Node message) {
        getChildren().clear();
        getChildren().add(message);
        setOpacity(0);
        setCache(true);
        setVisible(true);
        Timeline timeline = new Timeline(new KeyFrame(DURATION_SHOW, (ActionEvent t) -> {
            setCache(false);
        }, new KeyValue(opacityProperty(), 1, Interpolator.EASE_BOTH)));
        timeline.play();
    }
}
