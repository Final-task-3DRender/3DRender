package com.cgvsu;

import com.cgvsu.render_engine.RenderEngine;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import javax.vecmath.Vector3f;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;

public class GuiController {

    final private float TRANSLATION = 0.5F;
    final private float ROTATION_SENSITIVITY = 0.01F;
    final private float ZOOM_SENSITIVITY = 5.0F;
    
    private Vector3f initialCameraPosition = new Vector3f(0, 0, 100);
    private Vector3f initialCameraTarget = new Vector3f(0, 0, 0);

    @FXML
    BorderPane borderPane;

    @FXML
    private Canvas canvas;

    @FXML
    private Label modelInfoLabel;

    @FXML
    private Label cameraPositionLabel;

    @FXML
    private Label cameraTargetLabel;

    @FXML
    private HBox statusBar;

    @FXML
    private MenuItem openMenuItem;

    @FXML
    private MenuItem exitMenuItem;

    @FXML
    private MenuItem resetCameraMenuItem;

    @FXML
    private MenuItem helpMenuItem;

    private Model mesh = null;
    private String loadedFileName = null;

    private Camera camera = new Camera(
            new Vector3f(initialCameraPosition),
            new Vector3f(initialCameraTarget),
            1.0F, 1, 0.01F, 100);

    private Timeline timeline;

    // Mouse drag state
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean isMousePressed = false;

    @FXML
    private void initialize() {
        // Bind canvas size to available space
        canvas.widthProperty().bind(borderPane.widthProperty());
        canvas.heightProperty().bind(borderPane.heightProperty().subtract(95)); // Approximate space for menu+toolbar+statusbar

        // Setup keyboard shortcuts
        canvas.setFocusTraversable(true);
        canvas.requestFocus();
        canvas.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        canvas.addEventHandler(KeyEvent.KEY_RELEASED, this::handleKeyReleased);

        // Setup mouse controls
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
        canvas.addEventHandler(ScrollEvent.SCROLL, this::handleScroll);

        // Setup animation loop
        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            camera.setAspectRatio((float) (width / height));

            if (mesh != null) {
                RenderEngine.render(canvas.getGraphicsContext2D(), camera, mesh, (int) width, (int) height);
            }

            updateStatusBar();
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();

        // Setup menu accelerators
        setupMenuAccelerators();

        updateStatusBar();
    }

    private void setupMenuAccelerators() {
        if (openMenuItem != null) {
            openMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        }
        if (exitMenuItem != null) {
            exitMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Q));
        }
        if (resetCameraMenuItem != null) {
            resetCameraMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.R));
        }
        if (helpMenuItem != null) {
            helpMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F1));
        }
    }

    private void updateStatusBar() {
        Vector3f pos = camera.getPosition();
        Vector3f target = camera.getTarget();
        cameraPositionLabel.setText(String.format("Camera Position: (%.1f, %.1f, %.1f)", pos.x, pos.y, pos.z));
        cameraTargetLabel.setText(String.format("Camera Target: (%.1f, %.1f, %.1f)", target.x, target.y, target.z));

        if (mesh != null) {
            int vertexCount = mesh.vertices.size();
            int polygonCount = mesh.polygons.size();
            String fileName = loadedFileName != null ? loadedFileName : "Unknown";
            modelInfoLabel.setText(String.format("Model: %s | Vertices: %d | Polygons: %d", fileName, vertexCount, polygonCount));
        } else {
            modelInfoLabel.setText("No model loaded");
        }
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());
        loadedFileName = file.getName();

        try {
            String fileContent = Files.readString(fileName);
            mesh = ObjReader.read(fileContent);
            updateStatusBar();
            canvas.requestFocus();
        } catch (IOException exception) {
            showError("Error loading model", "Failed to read file: " + exception.getMessage());
        } catch (Exception exception) {
            showError("Error parsing model", "Failed to parse OBJ file: " + exception.getMessage());
        }
    }

    @FXML
    private void onExitMenuItemClick() {
        Stage stage = (Stage) canvas.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onHelpMenuItemClick() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Controls");
        alert.setHeaderText("3D Viewer Controls");
        alert.setContentText(
            "Keyboard Controls:\n" +
            "  Arrow Keys / WASD - Move camera\n" +
            "  R - Reset camera\n" +
            "  O - Open model\n" +
            "  Q - Quit\n\n" +
            "Mouse Controls:\n" +
            "  Left Click + Drag - Rotate camera around target\n" +
            "  Scroll Wheel - Zoom in/out\n\n" +
            "Toolbar:\n" +
            "  Use buttons for quick access to common actions"
        );
        alert.showAndWait();
    }

    @FXML
    private void handleResetCamera() {
        camera.setPosition(new Vector3f(initialCameraPosition));
        camera.setTarget(new Vector3f(initialCameraTarget));
        updateStatusBar();
    }

    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        switch (code) {
            case UP:
            case W:
                handleCameraForward(null);
                break;
            case DOWN:
            case S:
                handleCameraBackward(null);
                break;
            case LEFT:
            case A:
                handleCameraLeft(null);
                break;
            case RIGHT:
            case D:
                handleCameraRight(null);
                break;
            case SPACE:
                handleCameraUp(null);
                break;
            case SHIFT:
                handleCameraDown(null);
                break;
            case R:
                handleResetCamera();
                break;
            case O:
                onOpenModelMenuItemClick();
                break;
            case Q:
                onExitMenuItemClick();
                break;
            default:
                // Other keys are ignored
                break;
        }
    }

    private void handleKeyReleased(KeyEvent event) {
        // Can be used for smooth movement if needed
    }

    private void handleMousePressed(MouseEvent event) {
        if (event.isPrimaryButtonDown()) {
            lastMouseX = event.getX();
            lastMouseY = event.getY();
            isMousePressed = true;
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        if (isMousePressed && event.isPrimaryButtonDown()) {
            double deltaX = event.getX() - lastMouseX;
            double deltaY = event.getY() - lastMouseY;

            rotateCameraAroundTarget((float) deltaX * ROTATION_SENSITIVITY, (float) deltaY * ROTATION_SENSITIVITY);

            lastMouseX = event.getX();
            lastMouseY = event.getY();
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        isMousePressed = false;
    }

    private void handleScroll(ScrollEvent event) {
        double deltaY = event.getDeltaY();
        Vector3f direction = new Vector3f();
        direction.sub(camera.getTarget(), camera.getPosition());
        direction.normalize();
        direction.scale((float) (deltaY * ZOOM_SENSITIVITY * 0.01));
        
        Vector3f newPosition = new Vector3f(camera.getPosition());
        newPosition.add(direction);
        camera.setPosition(newPosition);
    }

    private void rotateCameraAroundTarget(float deltaX, float deltaY) {
        Vector3f position = camera.getPosition();
        Vector3f target = camera.getTarget();

        // Calculate camera offset from target
        Vector3f offset = new Vector3f();
        offset.sub(position, target);

        // Calculate spherical coordinates
        float radius = offset.length();
        float theta = (float) Math.atan2(offset.x, offset.z); // Horizontal angle
        float phi = (float) Math.acos(offset.y / radius); // Vertical angle

        // Update angles
        theta += deltaX;
        phi = Math.max(0.01f, Math.min((float) Math.PI - 0.01f, phi - deltaY));

        // Convert back to Cartesian coordinates
        float newX = radius * (float) (Math.sin(phi) * Math.sin(theta));
        float newY = radius * (float) Math.cos(phi);
        float newZ = radius * (float) (Math.sin(phi) * Math.cos(theta));

        Vector3f newOffset = new Vector3f(newX, newY, newZ);
        Vector3f newPosition = new Vector3f(target);
        newPosition.add(newOffset);

        camera.setPosition(newPosition);
    }

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        Vector3f direction = new Vector3f();
        direction.sub(camera.getTarget(), camera.getPosition());
        direction.normalize();
        direction.scale(TRANSLATION);
        camera.movePosition(direction);
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        Vector3f direction = new Vector3f();
        direction.sub(camera.getPosition(), camera.getTarget());
        direction.normalize();
        direction.scale(TRANSLATION);
        camera.movePosition(direction);
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        Vector3f forward = new Vector3f();
        forward.sub(camera.getTarget(), camera.getPosition());
        Vector3f up = new Vector3f(0, 1, 0);
        Vector3f right = new Vector3f();
        right.cross(forward, up);
        right.normalize();
        right.scale(-TRANSLATION);
        camera.movePosition(right);
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        Vector3f forward = new Vector3f();
        forward.sub(camera.getTarget(), camera.getPosition());
        Vector3f up = new Vector3f(0, 1, 0);
        Vector3f right = new Vector3f();
        right.cross(forward, up);
        right.normalize();
        right.scale(TRANSLATION);
        camera.movePosition(right);
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, -TRANSLATION, 0));
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
