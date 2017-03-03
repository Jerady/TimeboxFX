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

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Jens Deters
 */
public class Main extends Application {

    private TimerView mainPane;

    @Override
    public void start(final Stage primaryStage) throws Exception {
        mainPane = new TimerView();
        
        String title = mainPane.getAppProperties().getProperty("app.name") + " - " + mainPane.getAppProperties().getProperty("app.version");
        Scene scene = new Scene(mainPane, 500, 500);
        scene.getStylesheets().add(mainPane.getAppProperties().getProperty("app.css"));
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream(mainPane.getAppProperties().getProperty("app.icon"))));
        primaryStage.setTitle(title);
        primaryStage.setFullScreen(false);
         primaryStage.setOnCloseRequest((WindowEvent event) -> {
            event.consume();
            mainPane.onExitApp();
        });
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
