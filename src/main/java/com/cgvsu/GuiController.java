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
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.util.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.ModelTransform;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.camera.Camera;
import com.cgvsu.camera.OrbitCameraController;
import com.cgvsu.model.ModelTransformer;
import com.cgvsu.transform.ModelMatrixBuilder;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.triangulation.SimpleTriangulator;
import com.cgvsu.triangulation.Triangulator;
import com.cgvsu.render_engine.NormalCalculator;
import java.util.Optional;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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

    @FXML
    private TextField positionXField, positionYField, positionZField;
    @FXML
    private Button positionXDecButton, positionXIncButton;
    @FXML
    private Button positionYDecButton, positionYIncButton;
    @FXML
    private Button positionZDecButton, positionZIncButton;

    @FXML
    private TextField rotationXField, rotationYField, rotationZField;
    @FXML
    private Button rotationXDecButton, rotationXIncButton;
    @FXML
    private Button rotationYDecButton, rotationYIncButton;
    @FXML
    private Button rotationZDecButton, rotationZIncButton;

    @FXML
    private TextField scaleXField, scaleYField, scaleZField;
    @FXML
    private Button scaleXDecButton, scaleXIncButton;
    @FXML
    private Button scaleYDecButton, scaleYIncButton;
    @FXML
    private Button scaleZDecButton, scaleZIncButton;

    @FXML
    private Button moveModeButton, rotateModeButton, scaleModeButton;
    @FXML
    private Label currentModeLabel;

    @FXML
    private Label sceneModelInfoLabel, scenePositionLabel, sceneRotationLabel, sceneScaleLabel;

    // Scene models management
    private static class SceneModel {
        private final Model model;
        private final ModelTransform transform;
        private final String name;
        private boolean active;

        private SceneModel(Model model, String name) {
            this.model = model;
            this.transform = new ModelTransform();
            this.name = name;
            this.active = true;
        }
    }

    @FXML
    private ListView<String> modelsListView;

    @FXML
    private CheckBox modelActiveCheckBox;

    @FXML
    private CheckBox showWireframeCheckBox;

    @FXML
    private CheckBox showFilledCheckBox;

    @FXML
    private ColorPicker fillColorPicker;

    @FXML
    private ColorPicker wireframeColorPicker;

    private com.cgvsu.render_engine.RenderSettings renderSettings = new com.cgvsu.render_engine.RenderSettings();

    private final List<SceneModel> sceneModels = new ArrayList<>();
    private final ObservableList<String> modelNames = FXCollections.observableArrayList();
    private int selectedModelIndex = -1;

    private enum TransformMode { MOVE, ROTATE, SCALE }
    private TransformMode currentMode = TransformMode.MOVE;

    private final float TRANSFORM_STEP = 1.0f;
    private final float ROTATION_STEP = 5.0f;
    private final float SCALE_STEP = 0.1f;

    private Camera camera = new Camera(
            new Vector3f(initialCameraPosition),
            new Vector3f(initialCameraTarget),
            (float) Math.toRadians(60.0), // FOV в радианах (60 градусов)
            1, 0.01F, 100);
    
    private OrbitCameraController cameraController;

    private Timeline timeline;

    @FXML
    private void initialize() {
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

        // Увеличен интервал рендеринга до 33 мс (~30 FPS) для лучшей производительности с большими моделями
        KeyFrame frame = new KeyFrame(Duration.millis(33), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            camera.setAspectRatio((float) (width / height));

            if (!sceneModels.isEmpty()) {
                for (SceneModel sceneModel : sceneModels) {
                    if (sceneModel != null && sceneModel.active) {
                        RenderEngine.render(
                                canvas.getGraphicsContext2D(),
                                camera,
                                sceneModel.model,
                                sceneModel.transform,
                                (int) width,
                                (int) height,
                                renderSettings
                        );
                    }
                }
            }

            updateStatusBar();
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();

        setupMenuAccelerators();

        setupTransformUI();
        setupSceneModelsUI();
        setupDisplaySettingsUI();

        // Initialize camera controller
        cameraController = new OrbitCameraController(camera, initialCameraPosition, initialCameraTarget);

        updateStatusBar();
        updateTransformUI();
    }

    private void setupTransformUI() {
        setupTextField(positionXField, () -> {
            try {
                float value = Float.parseFloat(positionXField.getText());
                ModelTransform modelTransform = getCurrentTransform();
                if (modelTransform != null) {
                    modelTransform.setPosition(new Vector3f(value, modelTransform.getPosition().y, modelTransform.getPosition().z));
                }
                updateTransformUI();
            } catch (NumberFormatException e) {
                updateTransformFields();
            }
        });
        setupTextField(positionYField, () -> {
            try {
                float value = Float.parseFloat(positionYField.getText());
                ModelTransform modelTransform = getCurrentTransform();
                if (modelTransform != null) {
                    modelTransform.setPosition(new Vector3f(modelTransform.getPosition().x, value, modelTransform.getPosition().z));
                }
                updateTransformUI();
            } catch (NumberFormatException e) {
                updateTransformFields();
            }
        });
        setupTextField(positionZField, () -> {
            try {
                float value = Float.parseFloat(positionZField.getText());
                ModelTransform modelTransform = getCurrentTransform();
                if (modelTransform != null) {
                    modelTransform.setPosition(new Vector3f(modelTransform.getPosition().x, modelTransform.getPosition().y, value));
                }
                updateTransformUI();
            } catch (NumberFormatException e) {
                updateTransformFields();
            }
        });

        setupTextField(rotationXField, () -> {
            try {
                float value = Float.parseFloat(rotationXField.getText());
                ModelTransform modelTransform = getCurrentTransform();
                if (modelTransform != null) {
                    modelTransform.setRotation(new Vector3f(value, modelTransform.getRotation().y, modelTransform.getRotation().z));
                }
                updateTransformUI();
            } catch (NumberFormatException e) {
                updateTransformFields();
            }
        });
        setupTextField(rotationYField, () -> {
            try {
                float value = Float.parseFloat(rotationYField.getText());
                ModelTransform modelTransform = getCurrentTransform();
                if (modelTransform != null) {
                    modelTransform.setRotation(new Vector3f(modelTransform.getRotation().x, value, modelTransform.getRotation().z));
                }
                updateTransformUI();
            } catch (NumberFormatException e) {
                updateTransformFields();
            }
        });
        setupTextField(rotationZField, () -> {
            try {
                float value = Float.parseFloat(rotationZField.getText());
                ModelTransform modelTransform = getCurrentTransform();
                if (modelTransform != null) {
                    modelTransform.setRotation(new Vector3f(modelTransform.getRotation().x, modelTransform.getRotation().y, value));
                }
                updateTransformUI();
            } catch (NumberFormatException e) {
                updateTransformFields();
            }
        });

        setupTextField(scaleXField, () -> {
            try {
                float value = Float.parseFloat(scaleXField.getText());
                if (value > 0) {
                    ModelTransform modelTransform = getCurrentTransform();
                    if (modelTransform != null) {
                        modelTransform.setScale(new Vector3f(value, modelTransform.getScale().y, modelTransform.getScale().z));
                    }
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
                    ModelTransform modelTransform = getCurrentTransform();
                    if (modelTransform != null) {
                        modelTransform.setScale(new Vector3f(modelTransform.getScale().x, value, modelTransform.getScale().z));
                    }
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
                    ModelTransform modelTransform = getCurrentTransform();
                    if (modelTransform != null) {
                        modelTransform.setScale(new Vector3f(modelTransform.getScale().x, modelTransform.getScale().y, value));
                    }
                    updateTransformUI();
                }
            } catch (NumberFormatException e) {
                updateTransformFields();
            }
        });

        setupIncDecButtons(positionXDecButton, positionXIncButton, () -> adjustPositionX(-TRANSFORM_STEP), () -> adjustPositionX(TRANSFORM_STEP));
        setupIncDecButtons(positionYDecButton, positionYIncButton, () -> adjustPositionY(-TRANSFORM_STEP), () -> adjustPositionY(TRANSFORM_STEP));
        setupIncDecButtons(positionZDecButton, positionZIncButton, () -> adjustPositionZ(-TRANSFORM_STEP), () -> adjustPositionZ(TRANSFORM_STEP));

        setupIncDecButtons(rotationXDecButton, rotationXIncButton, () -> adjustRotationX(-ROTATION_STEP), () -> adjustRotationX(ROTATION_STEP));
        setupIncDecButtons(rotationYDecButton, rotationYIncButton, () -> adjustRotationY(-ROTATION_STEP), () -> adjustRotationY(ROTATION_STEP));
        setupIncDecButtons(rotationZDecButton, rotationZIncButton, () -> adjustRotationZ(-ROTATION_STEP), () -> adjustRotationZ(ROTATION_STEP));

        setupIncDecButtons(scaleXDecButton, scaleXIncButton, () -> adjustScaleX(-SCALE_STEP), () -> adjustScaleX(SCALE_STEP));
        setupIncDecButtons(scaleYDecButton, scaleYIncButton, () -> adjustScaleY(-SCALE_STEP), () -> adjustScaleY(SCALE_STEP));
        setupIncDecButtons(scaleZDecButton, scaleZIncButton, () -> adjustScaleZ(-SCALE_STEP), () -> adjustScaleZ(SCALE_STEP));

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

    private ModelTransform getCurrentTransform() {
        SceneModel current = getSelectedSceneModel();
        return current != null ? current.transform : null;
    }

    private SceneModel getSelectedSceneModel() {
        if (selectedModelIndex < 0 || selectedModelIndex >= sceneModels.size()) {
            return null;
        }
        return sceneModels.get(selectedModelIndex);
    }

    private void adjustPositionX(float delta) {
        ModelTransform modelTransform = getCurrentTransform();
        if (modelTransform == null) return;
        Vector3f pos = modelTransform.getPosition();
        modelTransform.setPosition(new Vector3f(pos.x + delta, pos.y, pos.z));
        updateTransformUI();
    }

    private void adjustPositionY(float delta) {
        ModelTransform modelTransform = getCurrentTransform();
        if (modelTransform == null) return;
        Vector3f pos = modelTransform.getPosition();
        modelTransform.setPosition(new Vector3f(pos.x, pos.y + delta, pos.z));
        updateTransformUI();
    }

    private void adjustPositionZ(float delta) {
        ModelTransform modelTransform = getCurrentTransform();
        if (modelTransform == null) return;
        Vector3f pos = modelTransform.getPosition();
        modelTransform.setPosition(new Vector3f(pos.x, pos.y, pos.z + delta));
        updateTransformUI();
    }

    private void adjustRotationX(float delta) {
        ModelTransform modelTransform = getCurrentTransform();
        if (modelTransform == null) return;
        Vector3f rot = modelTransform.getRotation();
        modelTransform.setRotation(new Vector3f(rot.x + delta, rot.y, rot.z));
        updateTransformUI();
    }

    private void adjustRotationY(float delta) {
        ModelTransform modelTransform = getCurrentTransform();
        if (modelTransform == null) return;
        Vector3f rot = modelTransform.getRotation();
        modelTransform.setRotation(new Vector3f(rot.x, rot.y + delta, rot.z));
        updateTransformUI();
    }

    private void adjustRotationZ(float delta) {
        ModelTransform modelTransform = getCurrentTransform();
        if (modelTransform == null) return;
        Vector3f rot = modelTransform.getRotation();
        modelTransform.setRotation(new Vector3f(rot.x, rot.y, rot.z + delta));
        updateTransformUI();
    }

    private void adjustScaleX(float delta) {
        ModelTransform modelTransform = getCurrentTransform();
        if (modelTransform == null) return;
        Vector3f scale = modelTransform.getScale();
        float newValue = Math.max(0.01f, scale.x + delta);
        modelTransform.setScale(new Vector3f(newValue, scale.y, scale.z));
        updateTransformUI();
    }

    private void adjustScaleY(float delta) {
        ModelTransform modelTransform = getCurrentTransform();
        if (modelTransform == null) return;
        Vector3f scale = modelTransform.getScale();
        float newValue = Math.max(0.01f, scale.y + delta);
        modelTransform.setScale(new Vector3f(scale.x, newValue, scale.z));
        updateTransformUI();
    }

    private void adjustScaleZ(float delta) {
        ModelTransform modelTransform = getCurrentTransform();
        if (modelTransform == null) return;
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
        ModelTransform modelTransform = getCurrentTransform();
        if (modelTransform == null) {
            if (positionXField != null && !positionXField.isFocused()) positionXField.setText("");
            if (positionYField != null && !positionYField.isFocused()) positionYField.setText("");
            if (positionZField != null && !positionZField.isFocused()) positionZField.setText("");
            if (rotationXField != null && !rotationXField.isFocused()) rotationXField.setText("");
            if (rotationYField != null && !rotationYField.isFocused()) rotationYField.setText("");
            if (rotationZField != null && !rotationZField.isFocused()) rotationZField.setText("");
            if (scaleXField != null && !scaleXField.isFocused()) scaleXField.setText("");
            if (scaleYField != null && !scaleYField.isFocused()) scaleYField.setText("");
            if (scaleZField != null && !scaleZField.isFocused()) scaleZField.setText("");
            return;
        }

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
        SceneModel current = getSelectedSceneModel();

        if (scenePositionLabel != null) {
            if (current != null) {
                Vector3f pos = current.transform.getPosition();
                scenePositionLabel.setText(String.format("Position: (%.2f, %.2f, %.2f)", pos.x, pos.y, pos.z));
            } else {
                scenePositionLabel.setText("Position: —");
            }
        }
        if (sceneRotationLabel != null) {
            if (current != null) {
                Vector3f rot = current.transform.getRotation();
                sceneRotationLabel.setText(String.format("Rotation: (%.1f°, %.1f°, %.1f°)", rot.x, rot.y, rot.z));
            } else {
                sceneRotationLabel.setText("Rotation: —");
            }
        }
        if (sceneScaleLabel != null) {
            if (current != null) {
                Vector3f scale = current.transform.getScale();
                sceneScaleLabel.setText(String.format("Scale: (%.2f, %.2f, %.2f)", scale.x, scale.y, scale.z));
            } else {
                sceneScaleLabel.setText("Scale: —");
            }
        }
        if (sceneModelInfoLabel != null) {
            if (current != null) {
                int vertexCount = current.model.vertices.size();
                int polygonCount = current.model.polygons.size();
                sceneModelInfoLabel.setText(String.format("Model: %s\nVertices: %d\nPolygons: %d",
                        current.name, vertexCount, polygonCount));
            } else {
                sceneModelInfoLabel.setText("No model selected");
            }
        }
    }

    @FXML
    private void handleResetTransform() {
        ModelTransform modelTransform = getCurrentTransform();
        if (modelTransform == null) return;
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

        SceneModel current = getSelectedSceneModel();
        if (current != null) {
            int vertexCount = current.model.vertices.size();
            int polygonCount = current.model.polygons.size();
            modelInfoLabel.setText(String.format("Model: %s | Vertices: %d | Polygons: %d",
                    current.name, vertexCount, polygonCount));
        } else {
            if (sceneModels.isEmpty()) {
                modelInfoLabel.setText("No models in scene");
            } else {
                modelInfoLabel.setText("No model selected");
            }
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

        try {
            String fileContent = Files.readString(fileName);
            Model mesh = ObjReader.read(fileContent);

            // Триангулируем модель
            Triangulator triangulator = new SimpleTriangulator();
            triangulator.triangulateModel(mesh);

            // Пересчитываем нормали (даже если они были в файле, мы не можем им доверять)
            NormalCalculator.recalculateNormals(mesh);

            SceneModel sceneModel = new SceneModel(mesh, file.getName());
            sceneModels.add(sceneModel);
            modelNames.add(sceneModel.name);

            if (modelsListView != null && modelsListView.getItems() != modelNames) {
                modelsListView.setItems(modelNames);
            }

            // Select newly added model
            selectedModelIndex = sceneModels.size() - 1;
            if (modelsListView != null) {
                modelsListView.getSelectionModel().select(selectedModelIndex);
            }

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
        SceneModel current = getSelectedSceneModel();
        if (current == null) {
            showError("No model to save", "Please select a model first.");
            return;
        }

        // Создаем диалог выбора: сохранить исходную модель или с трансформациями
        Alert choiceDialog = new Alert(AlertType.CONFIRMATION);
        choiceDialog.setTitle("Save Model");
        choiceDialog.setHeaderText("Choose save option:");
        choiceDialog.setContentText("Save original model or model with applied transformations?");
        
        ButtonType originalButton = new ButtonType("Original Model", ButtonBar.ButtonData.YES);
        ButtonType transformedButton = new ButtonType("With Transformations", ButtonBar.ButtonData.NO);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        choiceDialog.getButtonTypes().setAll(originalButton, transformedButton, cancelButton);
        
        Optional<ButtonType> result = choiceDialog.showAndWait();
        
        if (result.isEmpty() || result.get() == cancelButton) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Save Model");
        
        // Set default filename to selected model name
        fileChooser.setInitialFileName(current.name != null ? current.name : "model.obj");

        File file = fileChooser.showSaveDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            Model modelToSave;
            if (result.get() == transformedButton) {
                // Применяем трансформации к модели
                Matrix4f transformMatrix = ModelMatrixBuilder.build(current.transform);
                modelToSave = ModelTransformer.applyTransform(current.model, transformMatrix);
            } else {
                // Сохраняем исходную модель
                modelToSave = current.model;
            }
            
            ObjWriter.saveModel(modelToSave, file.getAbsolutePath());
            
            String saveType = (result.get() == transformedButton) ? "with transformations" : "original";
            showSuccess("Model saved", String.format("Model (%s) successfully saved to:\n%s", saveType, file.getAbsolutePath()));
        } catch (IOException exception) {
            showError("Error saving model", "Failed to save file: " + exception.getMessage());
        } catch (Exception exception) {
            showError("Error saving model", "Unexpected error: " + exception.getMessage());
        }
    }

    private void setupDisplaySettingsUI() {
        if (showWireframeCheckBox != null) {
            showWireframeCheckBox.setSelected(renderSettings.isShowWireframe());
            showWireframeCheckBox.setOnAction(e -> {
                renderSettings.setShowWireframe(showWireframeCheckBox.isSelected());
            });
        }

        if (showFilledCheckBox != null) {
            showFilledCheckBox.setSelected(renderSettings.isShowFilled());
            showFilledCheckBox.setOnAction(e -> {
                renderSettings.setShowFilled(showFilledCheckBox.isSelected());
            });
        }

        if (fillColorPicker != null) {
            fillColorPicker.setValue(renderSettings.getFillColor());
            fillColorPicker.setOnAction(e -> {
                renderSettings.setFillColor(fillColorPicker.getValue());
            });
        }

        if (wireframeColorPicker != null) {
            wireframeColorPicker.setValue(renderSettings.getWireframeColor());
            wireframeColorPicker.setOnAction(e -> {
                renderSettings.setWireframeColor(wireframeColorPicker.getValue());
            });
        }
    }

    private void setupSceneModelsUI() {
        if (modelsListView != null) {
            modelsListView.setItems(modelNames);
            modelsListView.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
                selectedModelIndex = newVal.intValue();
                updateTransformUI();
                updateSceneInfo();
                updateStatusBar();
                updateModelActiveCheckBox();
            });
        }

        if (modelActiveCheckBox != null) {
            modelActiveCheckBox.setOnAction(e -> {
                SceneModel current = getSelectedSceneModel();
                if (current != null) {
                    current.active = modelActiveCheckBox.isSelected();
                }
            });
        }

        updateModelActiveCheckBox();
    }

    private void updateModelActiveCheckBox() {
        if (modelActiveCheckBox == null) return;
        SceneModel current = getSelectedSceneModel();
        if (current == null) {
            modelActiveCheckBox.setSelected(false);
            modelActiveCheckBox.setDisable(true);
        } else {
            modelActiveCheckBox.setDisable(false);
            modelActiveCheckBox.setSelected(current.active);
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
                break;
        }
    }

    private void handleKeyReleased(KeyEvent event) {

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
