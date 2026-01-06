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
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.ModelTransform;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.CameraController;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

public class GuiController {
    
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
    private MenuItem saveMenuItem;

    @FXML
    private MenuItem exitMenuItem;

    @FXML
    private MenuItem resetCameraMenuItem;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private MenuItem resetTransformMenuItem;

    // Transform UI elements - Position
    @FXML
    private TextField positionXField, positionYField, positionZField;
    @FXML
    private Button positionXDecButton, positionXIncButton;
    @FXML
    private Button positionYDecButton, positionYIncButton;
    @FXML
    private Button positionZDecButton, positionZIncButton;

    // Transform UI elements - Rotation
    @FXML
    private TextField rotationXField, rotationYField, rotationZField;
    @FXML
    private Button rotationXDecButton, rotationXIncButton;
    @FXML
    private Button rotationYDecButton, rotationYIncButton;
    @FXML
    private Button rotationZDecButton, rotationZIncButton;

    // Transform UI elements - Scale
    @FXML
    private TextField scaleXField, scaleYField, scaleZField;
    @FXML
    private Button scaleXDecButton, scaleXIncButton;
    @FXML
    private Button scaleYDecButton, scaleYIncButton;
    @FXML
    private Button scaleZDecButton, scaleZIncButton;

    // Mode buttons
    @FXML
    private Button moveModeButton, rotateModeButton, scaleModeButton;
    @FXML
    private Label currentModeLabel;

    // Scene info labels
    @FXML
    private Label sceneModelInfoLabel, scenePositionLabel, sceneRotationLabel, sceneScaleLabel;

    private Model mesh = null;
    private String loadedFileName = null;
    private ModelTransform modelTransform = new ModelTransform();

    private enum TransformMode { MOVE, ROTATE, SCALE }
    private TransformMode currentMode = TransformMode.MOVE;

    private final float TRANSFORM_STEP = 1.0f;
    private final float ROTATION_STEP = 5.0f;
    private final float SCALE_STEP = 0.1f;

    private Camera camera = new Camera(
            new Vector3f(initialCameraPosition),
            new Vector3f(initialCameraTarget),
            1.0F, 1, 0.01F, 100);
    
    private CameraController cameraController;

    private Timeline timeline;

    @FXML
    private void initialize() {
        // Bind canvas size to available space (accounting for side panels, menu, toolbar, statusbar)
        // Left panel is ~250px, right panel is ~200px, menu+toolbar ~60px, statusbar ~25px
        canvas.widthProperty().bind(borderPane.widthProperty().subtract(450)); // Left + Right panels
        canvas.heightProperty().bind(borderPane.heightProperty().subtract(85)); // Menu + Toolbar + StatusBar

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
                RenderEngine.render(canvas.getGraphicsContext2D(), camera, mesh, modelTransform, (int) width, (int) height);
            }

            updateStatusBar();
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();

        // Setup menu accelerators
        setupMenuAccelerators();

        // Setup transform UI
        setupTransformUI();
        
        // Initialize camera controller
        cameraController = new CameraController(camera, initialCameraPosition, initialCameraTarget);

        updateStatusBar();
        updateTransformUI();
    }

    private void setupTransformUI() {
        // Setup position fields
        setupTextField(positionXField, () -> {
            try {
                float value = Float.parseFloat(positionXField.getText());
                modelTransform.setPosition(new Vector3f(value, modelTransform.getPosition().y, modelTransform.getPosition().z));
                updateTransformUI();
            } catch (NumberFormatException e) {
                updateTransformFields();
            }
        });
        setupTextField(positionYField, () -> {
            try {
                float value = Float.parseFloat(positionYField.getText());
                modelTransform.setPosition(new Vector3f(modelTransform.getPosition().x, value, modelTransform.getPosition().z));
                updateTransformUI();
            } catch (NumberFormatException e) {
                updateTransformFields();
            }
        });
        setupTextField(positionZField, () -> {
            try {
                float value = Float.parseFloat(positionZField.getText());
                modelTransform.setPosition(new Vector3f(modelTransform.getPosition().x, modelTransform.getPosition().y, value));
                updateTransformUI();
            } catch (NumberFormatException e) {
                updateTransformFields();
            }
        });

        // Setup rotation fields
        setupTextField(rotationXField, () -> {
            try {
                float value = Float.parseFloat(rotationXField.getText());
                modelTransform.setRotation(new Vector3f(value, modelTransform.getRotation().y, modelTransform.getRotation().z));
                updateTransformUI();
            } catch (NumberFormatException e) {
                updateTransformFields();
            }
        });
        setupTextField(rotationYField, () -> {
            try {
                float value = Float.parseFloat(rotationYField.getText());
                modelTransform.setRotation(new Vector3f(modelTransform.getRotation().x, value, modelTransform.getRotation().z));
                updateTransformUI();
            } catch (NumberFormatException e) {
                updateTransformFields();
            }
        });
        setupTextField(rotationZField, () -> {
            try {
                float value = Float.parseFloat(rotationZField.getText());
                modelTransform.setRotation(new Vector3f(modelTransform.getRotation().x, modelTransform.getRotation().y, value));
                updateTransformUI();
            } catch (NumberFormatException e) {
                updateTransformFields();
            }
        });

        // Setup scale fields
        setupTextField(scaleXField, () -> {
            try {
                float value = Float.parseFloat(scaleXField.getText());
                if (value > 0) {
                    modelTransform.setScale(new Vector3f(value, modelTransform.getScale().y, modelTransform.getScale().z));
                    updateTransformUI();
                }
            } catch (NumberFormatException e) {
                updateTransformFields();
            }
        });
        setupTextField(scaleYField, () -> {
            try {
                float value = Float.parseFloat(scaleYField.getText());
                if (value > 0) {
                    modelTransform.setScale(new Vector3f(modelTransform.getScale().x, value, modelTransform.getScale().z));
                    updateTransformUI();
                }
            } catch (NumberFormatException e) {
                updateTransformFields();
            }
        });
        setupTextField(scaleZField, () -> {
            try {
                float value = Float.parseFloat(scaleZField.getText());
                if (value > 0) {
                    modelTransform.setScale(new Vector3f(modelTransform.getScale().x, modelTransform.getScale().y, value));
                    updateTransformUI();
                }
            } catch (NumberFormatException e) {
                updateTransformFields();
            }
        });

        // Setup position buttons
        setupIncDecButtons(positionXDecButton, positionXIncButton, () -> adjustPositionX(-TRANSFORM_STEP), () -> adjustPositionX(TRANSFORM_STEP));
        setupIncDecButtons(positionYDecButton, positionYIncButton, () -> adjustPositionY(-TRANSFORM_STEP), () -> adjustPositionY(TRANSFORM_STEP));
        setupIncDecButtons(positionZDecButton, positionZIncButton, () -> adjustPositionZ(-TRANSFORM_STEP), () -> adjustPositionZ(TRANSFORM_STEP));

        // Setup rotation buttons
        setupIncDecButtons(rotationXDecButton, rotationXIncButton, () -> adjustRotationX(-ROTATION_STEP), () -> adjustRotationX(ROTATION_STEP));
        setupIncDecButtons(rotationYDecButton, rotationYIncButton, () -> adjustRotationY(-ROTATION_STEP), () -> adjustRotationY(ROTATION_STEP));
        setupIncDecButtons(rotationZDecButton, rotationZIncButton, () -> adjustRotationZ(-ROTATION_STEP), () -> adjustRotationZ(ROTATION_STEP));

        // Setup scale buttons
        setupIncDecButtons(scaleXDecButton, scaleXIncButton, () -> adjustScaleX(-SCALE_STEP), () -> adjustScaleX(SCALE_STEP));
        setupIncDecButtons(scaleYDecButton, scaleYIncButton, () -> adjustScaleY(-SCALE_STEP), () -> adjustScaleY(SCALE_STEP));
        setupIncDecButtons(scaleZDecButton, scaleZIncButton, () -> adjustScaleZ(-SCALE_STEP), () -> adjustScaleZ(SCALE_STEP));

        // Set initial mode
        handleSetMoveMode();
    }

    private void setupTextField(TextField field, Runnable onAction) {
        if (field != null) {
            field.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue && field != null) { // Lost focus
                    onAction.run();
                }
            });
        }
    }

    private void setupIncDecButtons(Button decButton, Button incButton, Runnable decAction, Runnable incAction) {
        if (decButton != null) {
            decButton.setOnAction(e -> decAction.run());
        }
        if (incButton != null) {
            incButton.setOnAction(e -> incAction.run());
        }
    }

    private void adjustPositionX(float delta) {
        Vector3f pos = modelTransform.getPosition();
        modelTransform.setPosition(new Vector3f(pos.x + delta, pos.y, pos.z));
        updateTransformUI();
    }

    private void adjustPositionY(float delta) {
        Vector3f pos = modelTransform.getPosition();
        modelTransform.setPosition(new Vector3f(pos.x, pos.y + delta, pos.z));
        updateTransformUI();
    }

    private void adjustPositionZ(float delta) {
        Vector3f pos = modelTransform.getPosition();
        modelTransform.setPosition(new Vector3f(pos.x, pos.y, pos.z + delta));
        updateTransformUI();
    }

    private void adjustRotationX(float delta) {
        Vector3f rot = modelTransform.getRotation();
        modelTransform.setRotation(new Vector3f(rot.x + delta, rot.y, rot.z));
        updateTransformUI();
    }

    private void adjustRotationY(float delta) {
        Vector3f rot = modelTransform.getRotation();
        modelTransform.setRotation(new Vector3f(rot.x, rot.y + delta, rot.z));
        updateTransformUI();
    }

    private void adjustRotationZ(float delta) {
        Vector3f rot = modelTransform.getRotation();
        modelTransform.setRotation(new Vector3f(rot.x, rot.y, rot.z + delta));
        updateTransformUI();
    }

    private void adjustScaleX(float delta) {
        Vector3f scale = modelTransform.getScale();
        float newValue = Math.max(0.01f, scale.x + delta);
        modelTransform.setScale(new Vector3f(newValue, scale.y, scale.z));
        updateTransformUI();
    }

    private void adjustScaleY(float delta) {
        Vector3f scale = modelTransform.getScale();
        float newValue = Math.max(0.01f, scale.y + delta);
        modelTransform.setScale(new Vector3f(scale.x, newValue, scale.z));
        updateTransformUI();
    }

    private void adjustScaleZ(float delta) {
        Vector3f scale = modelTransform.getScale();
        float newValue = Math.max(0.01f, scale.z + delta);
        modelTransform.setScale(new Vector3f(scale.x, scale.y, newValue));
        updateTransformUI();
    }

    private void updateTransformUI() {
        updateTransformFields();
        updateSceneInfo();
    }

    private void updateTransformFields() {
        Vector3f pos = modelTransform.getPosition();
        if (positionXField != null && !positionXField.isFocused()) {
            positionXField.setText(String.format("%.2f", pos.x));
        }
        if (positionYField != null && !positionYField.isFocused()) {
            positionYField.setText(String.format("%.2f", pos.y));
        }
        if (positionZField != null && !positionZField.isFocused()) {
            positionZField.setText(String.format("%.2f", pos.z));
        }

        Vector3f rot = modelTransform.getRotation();
        if (rotationXField != null && !rotationXField.isFocused()) {
            rotationXField.setText(String.format("%.1f", rot.x));
        }
        if (rotationYField != null && !rotationYField.isFocused()) {
            rotationYField.setText(String.format("%.1f", rot.y));
        }
        if (rotationZField != null && !rotationZField.isFocused()) {
            rotationZField.setText(String.format("%.1f", rot.z));
        }

        Vector3f scale = modelTransform.getScale();
        if (scaleXField != null && !scaleXField.isFocused()) {
            scaleXField.setText(String.format("%.2f", scale.x));
        }
        if (scaleYField != null && !scaleYField.isFocused()) {
            scaleYField.setText(String.format("%.2f", scale.y));
        }
        if (scaleZField != null && !scaleZField.isFocused()) {
            scaleZField.setText(String.format("%.2f", scale.z));
        }
    }

    private void updateSceneInfo() {
        if (scenePositionLabel != null) {
            Vector3f pos = modelTransform.getPosition();
            scenePositionLabel.setText(String.format("Position: (%.2f, %.2f, %.2f)", pos.x, pos.y, pos.z));
        }
        if (sceneRotationLabel != null) {
            Vector3f rot = modelTransform.getRotation();
            sceneRotationLabel.setText(String.format("Rotation: (%.1f°, %.1f°, %.1f°)", rot.x, rot.y, rot.z));
        }
        if (sceneScaleLabel != null) {
            Vector3f scale = modelTransform.getScale();
            sceneScaleLabel.setText(String.format("Scale: (%.2f, %.2f, %.2f)", scale.x, scale.y, scale.z));
        }
        if (sceneModelInfoLabel != null && mesh != null) {
            int vertexCount = mesh.vertices.size();
            int polygonCount = mesh.polygons.size();
            String fileName = loadedFileName != null ? loadedFileName : "Unknown";
            sceneModelInfoLabel.setText(String.format("Model: %s\nVertices: %d\nPolygons: %d", fileName, vertexCount, polygonCount));
        } else if (sceneModelInfoLabel != null) {
            sceneModelInfoLabel.setText("No model loaded");
        }
    }

    @FXML
    private void handleResetTransform() {
        modelTransform.reset();
        updateTransformUI();
    }

    @FXML
    private void handleSetMoveMode() {
        currentMode = TransformMode.MOVE;
        if (currentModeLabel != null) {
            currentModeLabel.setText("Mode: Move");
        }
        updateModeButtons();
    }

    @FXML
    private void handleSetRotateMode() {
        currentMode = TransformMode.ROTATE;
        if (currentModeLabel != null) {
            currentModeLabel.setText("Mode: Rotate");
        }
        updateModeButtons();
    }

    @FXML
    private void handleSetScaleMode() {
        currentMode = TransformMode.SCALE;
        if (currentModeLabel != null) {
            currentModeLabel.setText("Mode: Scale");
        }
        updateModeButtons();
    }

    private void updateModeButtons() {
        if (moveModeButton != null) {
            moveModeButton.setStyle(currentMode == TransformMode.MOVE ? "-fx-background-color: #4CAF50;" : "");
        }
        if (rotateModeButton != null) {
            rotateModeButton.setStyle(currentMode == TransformMode.ROTATE ? "-fx-background-color: #4CAF50;" : "");
        }
        if (scaleModeButton != null) {
            scaleModeButton.setStyle(currentMode == TransformMode.SCALE ? "-fx-background-color: #4CAF50;" : "");
        }
    }

    private void setupMenuAccelerators() {
        if (openMenuItem != null) {
            openMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        }
        if (saveMenuItem != null) {
            saveMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
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
            modelTransform.reset(); // Reset transform when loading new model
            updateStatusBar();
            updateTransformUI();
            updateSceneInfo();
            canvas.requestFocus();
        } catch (IOException exception) {
            showError("Error loading model", "Failed to read file: " + exception.getMessage());
        } catch (Exception exception) {
            showError("Error parsing model", "Failed to parse OBJ file: " + exception.getMessage());
        }
    }

    @FXML
    private void onSaveModelMenuItemClick() {
        if (mesh == null) {
            showError("No model to save", "Please load a model first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Save Model");
        
        // Set default filename if model was loaded from file
        if (loadedFileName != null) {
            fileChooser.setInitialFileName(loadedFileName);
        } else {
            fileChooser.setInitialFileName("model.obj");
        }

        File file = fileChooser.showSaveDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            ObjWriter.saveModel(mesh, file.getAbsolutePath());
            showSuccess("Model saved", "Model successfully saved to:\n" + file.getAbsolutePath());
        } catch (IOException exception) {
            showError("Error saving model", "Failed to save file: " + exception.getMessage());
        } catch (Exception exception) {
            showError("Error saving model", "Unexpected error: " + exception.getMessage());
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
            "  Space / Shift - Move camera up/down\n" +
            "  R - Reset camera\n" +
            "  Ctrl+O / O - Open model\n" +
            "  Q - Quit\n\n" +
            "Mouse Controls:\n" +
            "  Left Click + Drag - Rotate camera around target\n" +
            "  Scroll Wheel - Zoom in/out\n\n" +
            "Model Transform:\n" +
            "  Use left panel to transform loaded model:\n" +
            "  - Position: Move model in 3D space\n" +
            "  - Rotation: Rotate model around axes (in degrees)\n" +
            "  - Scale: Scale model along axes\n" +
            "  Use +/- buttons or type values directly\n" +
            "  Click 'Reset Transform' to restore default"
        );
        alert.showAndWait();
    }

    @FXML
    private void handleResetCamera() {
        if (cameraController != null) {
            cameraController.reset();
        } else {
            camera.setPosition(new Vector3f(initialCameraPosition));
            camera.setTarget(new Vector3f(initialCameraTarget));
        }
        updateStatusBar();
    }

    private void handleKeyPressed(KeyEvent event) {
        if (cameraController == null) return;
        
        KeyCode code = event.getCode();
        switch (code) {
            case UP:
            case W:
                cameraController.moveForward();
                break;
            case DOWN:
            case S:
                cameraController.moveBackward();
                break;
            case LEFT:
            case A:
                cameraController.moveLeft();
                break;
            case RIGHT:
            case D:
                cameraController.moveRight();
                break;
            case SPACE:
                cameraController.moveUp();
                break;
            case SHIFT:
                cameraController.moveDown();
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
        if (cameraController != null && event.isPrimaryButtonDown()) {
            cameraController.onMousePressed(event.getX(), event.getY());
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        if (cameraController != null && event.isPrimaryButtonDown()) {
            cameraController.onMouseDragged(event.getX(), event.getY());
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        if (cameraController != null) {
            cameraController.onMouseReleased();
        }
    }

    private void handleScroll(ScrollEvent event) {
        if (cameraController != null) {
            cameraController.onMouseScroll(event.getDeltaY());
        }
    }

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        if (cameraController != null) {
            cameraController.moveForward();
        }
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        if (cameraController != null) {
            cameraController.moveBackward();
        }
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        if (cameraController != null) {
            cameraController.moveLeft();
        }
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        if (cameraController != null) {
            cameraController.moveRight();
        }
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        if (cameraController != null) {
            cameraController.moveUp();
        }
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        if (cameraController != null) {
            cameraController.moveDown();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
