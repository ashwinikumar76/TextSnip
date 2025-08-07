package com.pausedtextgrabber;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class App extends Application {

    private double startX, startY;
    private Rectangle selectionRect;

    public static void main(String[] args) {
        // Print any uncaught exceptions on JavaFX thread
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            System.err.println("Uncaught exception in thread " + t.getName());
            e.printStackTrace();
        });

        System.out.println("Launching JavaFX application...");
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        System.out.println("JavaFX started! Setting up UI...");

        Pane root = new Pane();

        // Use semi-transparent black background to confirm window is visible
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);"); // semi-transparent black

        // Initialize selection rectangle with visible stroke & fill
        selectionRect = new Rectangle();
        selectionRect.setFill(Color.web("blue", 0.3));
        selectionRect.setStroke(Color.BLUE);
        root.getChildren().add(selectionRect);

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight(), Color.TRANSPARENT);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint(""); // Disable exit hint text
        stage.setScene(scene);
        stage.show();

        // Debug: confirm window size and position
        System.out.println("Window shown with size: " + stage.getWidth() + "x" + stage.getHeight());

        // Mouse event handlers with debug prints
        scene.setOnMousePressed(this::onMousePressed);
        scene.setOnMouseDragged(this::onMouseDragged);
        scene.setOnMouseReleased(e -> {
            onMouseReleased(e);
            System.out.println("Exiting application...");
            Platform.exit();
        });
    }

    private void onMousePressed(MouseEvent e) {
        System.out.println("Mouse pressed at: (" + e.getScreenX() + ", " + e.getScreenY() + ")");
        startX = e.getScreenX();
        startY = e.getScreenY();
        selectionRect.setX(startX);
        selectionRect.setY(startY);
        selectionRect.setWidth(0);
        selectionRect.setHeight(0);
    }

    private void onMouseDragged(MouseEvent e) {
        System.out.println("Mouse dragged at: (" + e.getScreenX() + ", " + e.getScreenY() + ")");
        double endX = e.getScreenX();
        double endY = e.getScreenY();
        selectionRect.setX(Math.min(startX, endX));
        selectionRect.setY(Math.min(startY, endY));
        selectionRect.setWidth(Math.abs(endX - startX));
        selectionRect.setHeight(Math.abs(endY - startY));
    }

    private void onMouseReleased(MouseEvent e) {
        System.out.println("Mouse released at: (" + e.getScreenX() + ", " + e.getScreenY() + ")");

        int x = (int) selectionRect.getX();
        int y = (int) selectionRect.getY();
        int w = (int) selectionRect.getWidth();
        int h = (int) selectionRect.getHeight();

        System.out.println("Selected area: x=" + x + ", y=" + y + ", width=" + w + ", height=" + h);

        if (w > 0 && h > 0) {
            captureAndExtractText(x, y, w, h);
        } else {
            System.out.println("No area selected. Nothing to capture.");
        }
    }

    private void captureAndExtractText(int x, int y, int w, int h) {
        try {
            System.out.println("Capturing screenshot at specified rectangle...");

            Robot robot = new Robot();
            BufferedImage screenshot = robot.createScreenCapture(new java.awt.Rectangle(x, y, w, h));

            File outFile = new File("screenshot.png");
            ImageIO.write(screenshot, "png", outFile);
            System.out.println("Screenshot saved at: " + outFile.getAbsolutePath());

            // Setup Tesseract OCR engine
            ITesseract tess = new Tesseract();
            tess.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata"); // Path to Tesseract installation folder (with tessdata subfolder)
            tess.setLanguage("eng");

            System.out.println("Performing OCR on captured image...");
            String result = tess.doOCR(outFile);

            System.out.println("\nExtracted Text:\n----------------------\n" + result);

        } catch (AWTException | IOException | TesseractException ex) {
            System.err.println("Error during screenshot or OCR process:");
            ex.printStackTrace();
        }

    }
}
