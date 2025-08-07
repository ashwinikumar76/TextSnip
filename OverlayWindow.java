package com.pausedtextgrabber;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

import javax.imageio.ImageIO;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class OverlayWindow extends Application {

    private double startX, startY;
    private Rectangle selection;

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();

        selection = new Rectangle();
        selection.setFill(Color.color(0, 0, 1, 0.3));
        selection.setStroke(Color.BLUE);
        root.getChildren().add(selection);

        Scene scene = new Scene(root,
                Screen.getPrimary().getBounds().getWidth(),
                Screen.getPrimary().getBounds().getHeight(),
                Color.TRANSPARENT
        );

        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setScene(scene);
        primaryStage.show();

        root.setOnMousePressed(this::handleMousePressed);
        root.setOnMouseDragged(this::handleMouseDragged);
        root.setOnMouseReleased(e -> {
            handleMouseReleased();
            Platform.exit();
        });
    }

    private void handleMousePressed(MouseEvent event) {
        startX = event.getScreenX();
        startY = event.getScreenY();
        selection.setX(startX);
        selection.setY(startY);
        selection.setWidth(0);
        selection.setHeight(0);
    }

    private void handleMouseDragged(MouseEvent event) {
        double currentX = event.getScreenX();
        double currentY = event.getScreenY();
        selection.setX(Math.min(startX, currentX));
        selection.setY(Math.min(startY, currentY));
        selection.setWidth(Math.abs(currentX - startX));
        selection.setHeight(Math.abs(currentY - startY));
    }

    private void handleMouseReleased() {
        int x = (int) selection.getX();
        int y = (int) selection.getY();
        int w = (int) selection.getWidth();
        int h = (int) selection.getHeight();

        try {
            Robot robot = new Robot();
            BufferedImage capture = robot.createScreenCapture(new java.awt.Rectangle(x, y, w, h));
            File outputFile = new File("screenshot.png");
            ImageIO.write(capture, "png", outputFile);
            System.out.println("Screenshot saved to: " + outputFile.getAbsolutePath());

            performOCR(outputFile);
        } catch (AWTException | IOException ex) {
            ex.printStackTrace();
        }
    }

    private void performOCR(File imageFile) {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\");  // Don't point directly to tessdata
        tesseract.setLanguage("eng");

        try {
            String result = tesseract.doOCR(imageFile);
            System.out.println("\nExtracted Text:\n----------------------\n" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
