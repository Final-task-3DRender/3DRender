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
import java.util.Set;
import java.util.HashSet;

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
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.Scene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.cgvsu.ui.SceneModel;
import com.cgvsu.ui.FileOperationsHandler;
import com.cgvsu.ui.ModelTransformController;
import com.cgvsu.removers.PolygonRemover;
import com.cgvsu.removers.VertexRemover;
import com.cgvsu.math.Vector4f;
import com.cgvsu.math.Point2f;
import com.cgvsu.model.Polygon;
import static com.cgvsu.render_engine.GraphicConveyor.vertexToPoint;

/**
 * Главный контроллер JavaFX приложения для просмотра и редактирования 3D моделей.
 * 
 * <p>Координирует работу всех компонентов приложения:
 * <ul>
 *   <li>Управление моделями в сцене (загрузка, сохранение, выбор)</li>
 *   <li>Трансформации моделей (позиция, вращение, масштаб)</li>
 *   <li>Управление камерой (мышь и клавиатура)</li>
 *   <li>Настройки рендеринга (wireframe, filled, текстуры, Z-buffer)</li>
 *   <li>Цикл рендеринга (анимация)</li>
 * </ul>
 * 
 * <p>Использует FXML для описания интерфейса (gui.fxml).
 * 
 * @author CGVSU Team
 * @version 1.0
 */
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
    private MenuItem toggleThemeMenuItem;

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

    private final List<SceneModel> sceneModels = new ArrayList<>();
    private final ObservableList<String> modelNames = FXCollections.observableArrayList();
    private int selectedModelIndex = -1;

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

    @FXML
    private CheckBox useTextureCheckBox;

    @FXML
    private Button loadTextureButton;

    @FXML
    private Label textureNameLabel;

    private com.cgvsu.render_engine.RenderSettings renderSettings = new com.cgvsu.render_engine.RenderSettings();
    
    private ModelTransformController transformController;
    
    private boolean isDarkTheme = false;

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
        canvas.heightProperty().bind(borderPane.heightProperty().subtract(85));

        canvas.setFocusTraversable(true);
        canvas.requestFocus();
        canvas.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        canvas.addEventHandler(KeyEvent.KEY_RELEASED, this::handleKeyReleased);

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
        canvas.addEventHandler(ScrollEvent.SCROLL, this::handleScroll);

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(33), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            camera.setAspectRatio((float) (width / height));

            if (!sceneModels.isEmpty()) {
                for (SceneModel sceneModel : sceneModels) {
                    if (sceneModel != null && sceneModel.isActive()) {
                        RenderEngine.render(
                                canvas.getGraphicsContext2D(),
                                camera,
                                sceneModel.getModel(),
                                sceneModel.getTransform(),
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

        cameraController = new OrbitCameraController(camera, initialCameraPosition, initialCameraTarget);

        // Применяем тему при инициализации
        applyTheme();

        updateStatusBar();
        updateTransformUI();
    }

    /**
     * Настраивает UI для управления трансформациями модели.
     * 
     * <p>Инициализирует ModelTransformController с UI элементами.
     */
    private void setupTransformUI() {
        transformController = new ModelTransformController(
            positionXField, positionYField, positionZField,
            positionXDecButton, positionXIncButton,
            positionYDecButton, positionYIncButton,
            positionZDecButton, positionZIncButton,
            rotationXField, rotationYField, rotationZField,
            rotationXDecButton, rotationXIncButton,
            rotationYDecButton, rotationYIncButton,
            rotationZDecButton, rotationZIncButton,
            scaleXField, scaleYField, scaleZField,
            scaleXDecButton, scaleXIncButton,
            scaleYDecButton, scaleYIncButton,
            scaleZDecButton, scaleZIncButton,
            moveModeButton, rotateModeButton, scaleModeButton,
            currentModeLabel
        );
        
        ModelTransform currentTransform = getCurrentTransform();
        if (currentTransform != null) {
            transformController.setup(currentTransform, this::updateTransformUI);
        }
    }

    /**
     * Возвращает трансформации текущей выбранной модели.
     * 
     * @return трансформации модели или null, если модель не выбрана
     */
    private ModelTransform getCurrentTransform() {
        SceneModel current = getSelectedSceneModel();
        return current != null ? current.getTransform() : null;
    }

    /**
     * Возвращает текущую выбранную модель в сцене.
     * 
     * @return выбранная модель или null, если модель не выбрана
     */
    private SceneModel getSelectedSceneModel() {
        if (selectedModelIndex < 0 || selectedModelIndex >= sceneModels.size()) {
            return null;
        }
        return sceneModels.get(selectedModelIndex);
    }

    /**
     * Обновляет UI трансформаций и информацию о сцене.
     */
    private void updateTransformUI() {
        ModelTransform currentTransform = getCurrentTransform();
        if (transformController != null && currentTransform != null) {
            transformController.updateFields(currentTransform);
        }
        updateSceneInfo();
    }

    /**
     * Обновляет информацию о выбранной модели в UI.
     */
    private void updateSceneInfo() {
        SceneModel current = getSelectedSceneModel();

        if (scenePositionLabel != null) {
            if (current != null) {
                Vector3f pos = current.getTransform().getPosition();
                scenePositionLabel.setText(String.format("Position: (%.2f, %.2f, %.2f)", pos.x, pos.y, pos.z));
            } else {
                scenePositionLabel.setText("Position: —");
            }
        }
        if (sceneRotationLabel != null) {
            if (current != null) {
                Vector3f rot = current.getTransform().getRotation();
                sceneRotationLabel.setText(String.format("Rotation: (%.1f°, %.1f°, %.1f°)", rot.x, rot.y, rot.z));
            } else {
                sceneRotationLabel.setText("Rotation: —");
            }
        }
        if (sceneScaleLabel != null) {
            if (current != null) {
                Vector3f scale = current.getTransform().getScale();
                sceneScaleLabel.setText(String.format("Scale: (%.2f, %.2f, %.2f)", scale.x, scale.y, scale.z));
            } else {
                sceneScaleLabel.setText("Scale: —");
            }
        }
        if (sceneModelInfoLabel != null) {
            if (current != null) {
                int vertexCount = current.getModel().getVertexCount();
                int polygonCount = current.getModel().getPolygonCount();
                sceneModelInfoLabel.setText(String.format("Model: %s\nVertices: %d\nPolygons: %d",
                        current.getName(), vertexCount, polygonCount));
            } else {
                sceneModelInfoLabel.setText("No model selected");
            }
        }
    }

    /**
     * Сбрасывает трансформации текущей модели к начальным значениям.
     */
    @FXML
    private void handleResetTransform() {
        ModelTransform modelTransform = getCurrentTransform();
        if (modelTransform != null && transformController != null) {
            transformController.resetTransform(modelTransform);
            updateTransformUI();
        }
    }

    /**
     * Устанавливает режим трансформации: перенос (MOVE).
     */
    @FXML
    private void handleSetMoveMode() {
        if (transformController != null) {
            transformController.setMode(ModelTransformController.TransformMode.MOVE);
        }
    }

    /**
     * Устанавливает режим трансформации: вращение (ROTATE).
     */
    @FXML
    private void handleSetRotateMode() {
        if (transformController != null) {
            transformController.setMode(ModelTransformController.TransformMode.ROTATE);
        }
    }

    /**
     * Устанавливает режим трансформации: масштабирование (SCALE).
     */
    @FXML
    private void handleSetScaleMode() {
        if (transformController != null) {
            transformController.setMode(ModelTransformController.TransformMode.SCALE);
        }
    }

    /**
     * Настраивает горячие клавиши для меню.
     */
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

    /**
     * Обновляет информацию в строке состояния (status bar).
     * Отображает позицию камеры, цель камеры и информацию о выбранной модели.
     */
    private void updateStatusBar() {
        Vector3f pos = camera.getPosition();
        Vector3f target = camera.getTarget();
        cameraPositionLabel.setText(String.format("Camera Position: (%.1f, %.1f, %.1f)", pos.x, pos.y, pos.z));
        cameraTargetLabel.setText(String.format("Camera Target: (%.1f, %.1f, %.1f)", target.x, target.y, target.z));

        SceneModel current = getSelectedSceneModel();
        if (current != null) {
            int vertexCount = current.getModel().getVertexCount();
            int polygonCount = current.getModel().getPolygonCount();
            modelInfoLabel.setText(String.format("Model: %s | Vertices: %d | Polygons: %d",
                    current.getName(), vertexCount, polygonCount));
        } else {
            if (sceneModels.isEmpty()) {
                modelInfoLabel.setText("No models in scene");
            } else {
                modelInfoLabel.setText("No model selected");
            }
        }
    }

    /**
     * Обрабатывает загрузку модели из файла.
     * 
     * <p>Показывает диалог выбора файла, загружает модель, выполняет триангуляцию
     * и пересчет нормалей, затем добавляет модель в сцену.
     */
    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            Model mesh = FileOperationsHandler.loadModel(file);

            SceneModel sceneModel = new SceneModel(mesh, file.getName());
            sceneModels.add(sceneModel);
            modelNames.add(sceneModel.getName());

            if (modelsListView != null && modelsListView.getItems() != modelNames) {
                modelsListView.setItems(modelNames);
            }

            selectedModelIndex = sceneModels.size() - 1;
            if (modelsListView != null) {
                modelsListView.getSelectionModel().select(selectedModelIndex);
            }
            
            if (transformController != null) {
                transformController.setup(sceneModel.getTransform(), this::updateTransformUI);
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

    /**
     * Обрабатывает сохранение модели в файл.
     * 
     * <p>Показывает диалог выбора: сохранить исходную модель или с примененными трансформациями,
     * затем показывает диалог выбора файла и сохраняет модель.
     */
    @FXML
    private void onSaveModelMenuItemClick() {
        SceneModel current = getSelectedSceneModel();
        if (current == null) {
            showError("No model to save", "Please select a model first.");
            return;
        }

        try {
            boolean saved = FileOperationsHandler.saveModelWithChoice(
                current.getModel(),
                current.getTransform(),
                current.getName(),
                (Stage) canvas.getScene().getWindow()
            );
            
            if (saved) {
                showSuccess("Model saved", "Model successfully saved.");
            }
        } catch (IOException exception) {
            showError("Error saving model", "Failed to save file: " + exception.getMessage());
        } catch (Exception exception) {
            showError("Error saving model", "Unexpected error: " + exception.getMessage());
        }
    }

    /**
     * Настраивает UI для настроек рендеринга (wireframe, filled, цвета, текстуры).
     */
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

        if (useTextureCheckBox != null) {
            useTextureCheckBox.setSelected(renderSettings.isUseTexture());
            useTextureCheckBox.setOnAction(e -> {
                renderSettings.setUseTexture(useTextureCheckBox.isSelected());
            });
        }

        if (textureNameLabel != null) {
            updateTextureLabel();
        }
    }

    /**
     * Обрабатывает загрузку текстуры из файла.
     * 
     * <p>Показывает диалог выбора изображения и загружает его как текстуру.
     */
    @FXML
    private void onLoadTextureButtonClick() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Load Texture");
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
        );

        javafx.stage.Window window = canvas.getScene().getWindow();
        java.io.File file = fileChooser.showOpenDialog(window);
        
        if (file != null) {
            try {
                com.cgvsu.render_engine.Texture texture = com.cgvsu.render_engine.Texture.loadFromFile(file.getAbsolutePath());
                renderSettings.setTexture(texture);
                updateTextureLabel();
            } catch (java.io.IOException e) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to load texture");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    /**
     * Обновляет метку с информацией о загруженной текстуре.
     */
    private void updateTextureLabel() {
        if (textureNameLabel != null) {
            com.cgvsu.render_engine.Texture texture = renderSettings.getTexture();
            if (texture != null && texture.isValid()) {
                textureNameLabel.setText("Texture: " + texture.getWidth() + "x" + texture.getHeight());
            } else {
                textureNameLabel.setText("No texture loaded");
            }
        }
    }

    /**
     * Настраивает UI для управления списком моделей в сцене.
     */
    private void setupSceneModelsUI() {
        if (modelsListView != null) {
            modelsListView.setItems(modelNames);
            modelsListView.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
                selectedModelIndex = newVal.intValue();
                if (transformController != null) {
                    ModelTransform currentTransform = getCurrentTransform();
                    if (currentTransform != null) {
                        transformController.setup(currentTransform, this::updateTransformUI);
                    } else {
                        transformController.updateFields(null);
                    }
                }
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
                    current.setActive(modelActiveCheckBox.isSelected());
                }
            });
        }

        updateModelActiveCheckBox();
    }

    /**
     * Обновляет состояние чекбокса активности модели.
     */
    private void updateModelActiveCheckBox() {
        if (modelActiveCheckBox == null) return;
        SceneModel current = getSelectedSceneModel();
        if (current == null) {
            modelActiveCheckBox.setSelected(false);
            modelActiveCheckBox.setDisable(true);
        } else {
            modelActiveCheckBox.setDisable(false);
            modelActiveCheckBox.setSelected(current.isActive());
        }
    }

    /**
     * Обрабатывает выход из приложения.
     */
    @FXML
    private void onExitMenuItemClick() {
        Stage stage = (Stage) canvas.getScene().getWindow();
        stage.close();
    }

    /**
     * Показывает справку по управлению приложением.
     */
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

    /**
     * Сбрасывает камеру в начальное положение.
     */
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

    /**
     * Обрабатывает нажатие клавиши для управления камерой.
     * 
     * @param event событие нажатия клавиши
     */
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

    /**
     * Обрабатывает отпускание клавиши.
     * 
     * @param event событие отпускания клавиши
     */
    private void handleKeyReleased(KeyEvent event) {

    }

    /**
     * Обрабатывает нажатие кнопки мыши для управления камерой.
     * 
     * @param event событие нажатия мыши
     */
    private void handleMousePressed(MouseEvent event) {
        if (event.isSecondaryButtonDown()) {
            // Правая кнопка мыши - показываем контекстное меню
            handleRightClick(event.getX(), event.getY());
        } else if (cameraController != null && event.isPrimaryButtonDown()) {
            cameraController.onMousePressed(event.getX(), event.getY());
        }
    }

    /**
     * Обрабатывает перетаскивание мыши для поворота камеры.
     * 
     * @param event событие перетаскивания мыши
     */
    private void handleMouseDragged(MouseEvent event) {
        if (cameraController != null && event.isPrimaryButtonDown()) {
            cameraController.onMouseDragged(event.getX(), event.getY());
        }
    }

    /**
     * Обрабатывает отпускание кнопки мыши.
     * 
     * @param event событие отпускания мыши
     */
    private void handleMouseReleased(MouseEvent event) {
        if (cameraController != null) {
            cameraController.onMouseReleased();
        }
    }

    /**
     * Обрабатывает прокрутку колесика мыши для зума камеры.
     * 
     * @param event событие прокрутки
     */
    private void handleScroll(ScrollEvent event) {
        if (cameraController != null) {
            cameraController.onMouseScroll(event.getDeltaY());
        }
    }

    /**
     * Обрабатывает движение камеры вперед (по направлению взгляда).
     * 
     * @param actionEvent событие действия (не используется)
     */
    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        if (cameraController != null) {
            cameraController.moveForward();
        }
    }

    /**
     * Обрабатывает движение камеры назад (против направления взгляда).
     * 
     * @param actionEvent событие действия (не используется)
     */
    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        if (cameraController != null) {
            cameraController.moveBackward();
        }
    }

    /**
     * Обрабатывает движение камеры влево.
     * 
     * @param actionEvent событие действия (не используется)
     */
    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        if (cameraController != null) {
            cameraController.moveLeft();
        }
    }

    /**
     * Обрабатывает движение камеры вправо.
     * 
     * @param actionEvent событие действия (не используется)
     */
    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        if (cameraController != null) {
            cameraController.moveRight();
        }
    }

    /**
     * Обрабатывает движение камеры вверх.
     * 
     * @param actionEvent событие действия (не используется)
     */
    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        if (cameraController != null) {
            cameraController.moveUp();
        }
    }

    /**
     * Обрабатывает движение камеры вниз.
     * 
     * @param actionEvent событие действия (не используется)
     */
    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        if (cameraController != null) {
            cameraController.moveDown();
        }
    }

    /**
     * Показывает диалог с сообщением об ошибке.
     * 
     * @param title заголовок диалога
     * @param message текст сообщения
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Показывает диалог с сообщением об успешной операции.
     * 
     * @param title заголовок диалога
     * @param message текст сообщения
     */
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Обрабатывает удаление полигона из модели.
     * 
     * <p>Показывает диалог для ввода индекса полигона и удаляет его из модели.
     * Также удаляет неиспользуемые вершины, если пользователь выберет эту опцию.
     */
    @FXML
    private void handleDeletePolygon() {
        SceneModel current = getSelectedSceneModel();
        if (current == null) {
            showError("No model selected", "Please select a model first.");
            return;
        }

        Model model = current.getModel();
        int polygonCount = model.getPolygonCount();
        
        if (polygonCount == 0) {
            showError("No polygons", "The model has no polygons to delete.");
            return;
        }

        // Создаем диалог для ввода индекса полигона
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Delete Polygon");
        dialog.setHeaderText("Enter polygon index to delete");
        dialog.setContentText(String.format("Polygon index (0-%d):", polygonCount - 1));

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            try {
                int index = Integer.parseInt(result.get().trim());
                
                if (index < 0 || index >= polygonCount) {
                    showError("Invalid index", String.format("Index must be between 0 and %d.", polygonCount - 1));
                    return;
                }

                // Спрашиваем, удалять ли неиспользуемые вершины
                Alert confirmDialog = new Alert(AlertType.CONFIRMATION);
                confirmDialog.setTitle("Delete Free Vertices");
                confirmDialog.setHeaderText("Delete unused vertices?");
                confirmDialog.setContentText("Do you want to delete vertices that are no longer used by any polygon?");
                
                ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
                ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                
                confirmDialog.getButtonTypes().setAll(yesButton, noButton, cancelButton);
                
                Optional<ButtonType> confirmResult = confirmDialog.showAndWait();
                
                if (confirmResult.isPresent() && confirmResult.get() == cancelButton) {
                    return; // Пользователь отменил операцию
                }
                
                boolean deleteFreeVertices = confirmResult.isPresent() && confirmResult.get() == yesButton;
                
                // Удаляем полигон
                Set<Integer> polygonIndicesToDelete = new HashSet<>();
                polygonIndicesToDelete.add(index);
                
                PolygonRemover.deletePolygons(model, polygonIndicesToDelete, deleteFreeVertices);
                
                // Пересчитываем нормали после удаления
                NormalCalculator.recalculateNormals(model);
                
                showSuccess("Polygon deleted", String.format("Polygon %d has been deleted.", index));
                
                // Обновляем UI
                updateStatusBar();
                updateSceneInfo();
                updateTransformUI();
                
            } catch (NumberFormatException e) {
                showError("Invalid input", "Please enter a valid number.");
            } catch (Exception e) {
                showError("Error deleting polygon", "Failed to delete polygon: " + e.getMessage());
            }
        }
    }

    /**
     * Обрабатывает удаление вершины из модели.
     * 
     * <p>Показывает диалог для ввода индекса вершины и удаляет её из модели.
     * Также удаляет все полигоны, использующие эту вершину.
     */
    @FXML
    private void handleDeleteVertex() {
        SceneModel current = getSelectedSceneModel();
        if (current == null) {
            showError("No model selected", "Please select a model first.");
            return;
        }

        Model model = current.getModel();
        int vertexCount = model.getVertexCount();
        
        if (vertexCount == 0) {
            showError("No vertices", "The model has no vertices to delete.");
            return;
        }

        // Создаем диалог для ввода индекса вершины
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Delete Vertex");
        dialog.setHeaderText("Enter vertex index to delete");
        dialog.setContentText(String.format("Vertex index (0-%d):", vertexCount - 1));

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            try {
                int index = Integer.parseInt(result.get().trim());
                
                if (index < 0 || index >= vertexCount) {
                    showError("Invalid index", String.format("Index must be between 0 and %d.", vertexCount - 1));
                    return;
                }

                // Предупреждаем, что будут удалены полигоны
                Alert warningDialog = new Alert(AlertType.CONFIRMATION);
                warningDialog.setTitle("Delete Vertex");
                warningDialog.setHeaderText("Warning: Polygons will be deleted");
                warningDialog.setContentText(
                    "Deleting a vertex will also delete all polygons that use this vertex.\n" +
                    "Do you want to continue?"
                );
                
                Optional<ButtonType> confirmResult = warningDialog.showAndWait();
                
                if (!confirmResult.isPresent() || confirmResult.get() != ButtonType.OK) {
                    return; // Пользователь отменил операцию
                }
                
                // Спрашиваем, удалять ли изначально свободные вершины
                Alert freeVerticesDialog = new Alert(AlertType.CONFIRMATION);
                freeVerticesDialog.setTitle("Delete Free Vertices");
                freeVerticesDialog.setHeaderText("Delete initially free vertices?");
                freeVerticesDialog.setContentText("Do you want to delete vertices that were never used by any polygon?");
                
                ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
                ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
                
                freeVerticesDialog.getButtonTypes().setAll(yesButton, noButton);
                
                Optional<ButtonType> freeVerticesResult = freeVerticesDialog.showAndWait();
                boolean removeInitiallyFreeVertices = freeVerticesResult.isPresent() && freeVerticesResult.get() == yesButton;
                
                // Удаляем вершину
                Set<Integer> verticesToDelete = new HashSet<>();
                verticesToDelete.add(index);
                
                int polygonsBefore = model.getPolygonCount();
                VertexRemover.deleteVertices(model, verticesToDelete, removeInitiallyFreeVertices);
                int polygonsAfter = model.getPolygonCount();
                int deletedPolygons = polygonsBefore - polygonsAfter;
                
                // Пересчитываем нормали после удаления
                NormalCalculator.recalculateNormals(model);
                
                showSuccess("Vertex deleted", 
                    String.format("Vertex %d has been deleted.\n%d polygon(s) were also deleted.", 
                        index, deletedPolygons));
                
                // Обновляем UI
                updateStatusBar();
                updateSceneInfo();
                updateTransformUI();
                
            } catch (NumberFormatException e) {
                showError("Invalid input", "Please enter a valid number.");
            } catch (Exception e) {
                showError("Error deleting vertex", "Failed to delete vertex: " + e.getMessage());
            }
        }
    }

    /**
     * Обрабатывает клик правой кнопкой мыши и показывает контекстное меню.
     * 
     * @param mouseX координата X мыши
     * @param mouseY координата Y мыши
     */
    private void handleRightClick(double mouseX, double mouseY) {
        SceneModel current = getSelectedSceneModel();
        if (current == null) {
            return; // Нет модели для работы
        }

        // Определяем полигон и вершину под курсором
        PickResult pickResult = pickPolygonOrVertex(current, mouseX, mouseY);
        
        ContextMenu contextMenu = new ContextMenu();
        
        if (pickResult.polygonIndex >= 0) {
            javafx.scene.control.MenuItem deletePolygonItem = new javafx.scene.control.MenuItem("Delete Polygon");
            deletePolygonItem.setOnAction(e -> deletePolygonByIndex(current.getModel(), pickResult.polygonIndex));
            contextMenu.getItems().add(deletePolygonItem);
        }
        
        if (pickResult.vertexIndex >= 0) {
            javafx.scene.control.MenuItem deleteVertexItem = new javafx.scene.control.MenuItem("Delete Vertex");
            deleteVertexItem.setOnAction(e -> deleteVertexByIndex(current.getModel(), pickResult.vertexIndex));
            contextMenu.getItems().add(deleteVertexItem);
        }
        
        if (contextMenu.getItems().isEmpty()) {
            return; // Нет элементов для удаления
        }
        
        contextMenu.show(canvas, mouseX, mouseY);
    }

    /**
     * Результат выбора полигона или вершины.
     */
    private static class PickResult {
        int polygonIndex = -1;
        int vertexIndex = -1;
        double polygonDistance = Double.MAX_VALUE;
        double vertexDistance = Double.MAX_VALUE;
    }

    /**
     * Определяет полигон или вершину под курсором мыши.
     * 
     * @param sceneModel модель сцены
     * @param mouseX координата X мыши
     * @param mouseY координата Y мыши
     * @return результат выбора с индексами полигона и вершины
     */
    private PickResult pickPolygonOrVertex(SceneModel sceneModel, double mouseX, double mouseY) {
        PickResult result = new PickResult();
        Model model = sceneModel.getModel();
        ModelTransform transform = sceneModel.getTransform();
        
        if (model.getPolygonCount() == 0 && model.getVertexCount() == 0) {
            return result;
        }

        int width = (int) canvas.getWidth();
        int height = (int) canvas.getHeight();
        
        // Строим матрицы преобразования
        Matrix4f modelMatrix = ModelMatrixBuilder.build(transform);
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();
        Matrix4f mvpMatrix = projectionMatrix.multiply(viewMatrix).multiply(modelMatrix);

        final double PICK_TOLERANCE = 10.0; // Радиус выбора в пикселях

        // Ищем ближайший полигон
        for (int i = 0; i < model.getPolygonCount(); i++) {
            Polygon polygon = model.getPolygon(i);
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            
            if (vertexIndices.size() < 3) {
                continue;
            }

            // Преобразуем вершины полигона в экранные координаты
            ArrayList<Point2f> screenPoints = new ArrayList<>();
            for (int vertexIdx : vertexIndices) {
                if (vertexIdx < 0 || vertexIdx >= model.getVertexCount()) {
                    continue;
                }
                Vector3f vertex = model.getVertex(vertexIdx);
                if (vertex == null) {
                    continue;
                }

                Vector4f homogeneousVertex = new Vector4f(vertex, 1.0f);
                Vector4f transformed = mvpMatrix.multiply(homogeneousVertex);
                
                if (Math.abs(transformed.w) > 1e-7f) {
                    transformed = transformed.divide(transformed.w);
                }
                
                Point2f screenPoint = vertexToPoint(transformed, width, height);
                screenPoints.add(screenPoint);
            }

            if (screenPoints.size() < 3) {
                continue;
            }

            // Проверяем, находится ли точка внутри полигона или близко к нему
            double distance = distanceToPolygon(screenPoints, mouseX, mouseY);
            if (distance < PICK_TOLERANCE && distance < result.polygonDistance) {
                result.polygonIndex = i;
                result.polygonDistance = distance;
            }
        }

        // Ищем ближайшую вершину
        for (int i = 0; i < model.getVertexCount(); i++) {
            Vector3f vertex = model.getVertex(i);
            if (vertex == null) {
                continue;
            }

            Vector4f homogeneousVertex = new Vector4f(vertex, 1.0f);
            Vector4f transformed = mvpMatrix.multiply(homogeneousVertex);
            
            if (Math.abs(transformed.w) > 1e-7f) {
                transformed = transformed.divide(transformed.w);
            }
            
            Point2f screenPoint = vertexToPoint(transformed, width, height);
            
            double distance = Math.sqrt(
                Math.pow(screenPoint.x - mouseX, 2) + 
                Math.pow(screenPoint.y - mouseY, 2)
            );
            
            if (distance < PICK_TOLERANCE && distance < result.vertexDistance) {
                result.vertexIndex = i;
                result.vertexDistance = distance;
            }
        }

        return result;
    }

    /**
     * Вычисляет минимальное расстояние от точки до полигона.
     * 
     * @param polygonPoints точки полигона в экранных координатах
     * @param x координата X точки
     * @param y координата Y точки
     * @return минимальное расстояние
     */
    private double distanceToPolygon(ArrayList<Point2f> polygonPoints, double x, double y) {
        if (polygonPoints.isEmpty()) {
            return Double.MAX_VALUE;
        }

        // Проверяем расстояние до каждой стороны полигона
        double minDistance = Double.MAX_VALUE;
        
        for (int i = 0; i < polygonPoints.size(); i++) {
            Point2f p1 = polygonPoints.get(i);
            Point2f p2 = polygonPoints.get((i + 1) % polygonPoints.size());
            
            double distance = distanceToLineSegment(p1.x, p1.y, p2.x, p2.y, x, y);
            minDistance = Math.min(minDistance, distance);
        }

        // Проверяем, находится ли точка внутри полигона
        if (isPointInPolygon(polygonPoints, x, y)) {
            return 0.0;
        }

        return minDistance;
    }

    /**
     * Вычисляет расстояние от точки до отрезка.
     */
    private double distanceToLineSegment(double x1, double y1, double x2, double y2, double px, double py) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double lengthSquared = dx * dx + dy * dy;
        
        if (lengthSquared == 0) {
            // Отрезок вырожден в точку
            return Math.sqrt(Math.pow(px - x1, 2) + Math.pow(py - y1, 2));
        }
        
        double t = Math.max(0, Math.min(1, ((px - x1) * dx + (py - y1) * dy) / lengthSquared));
        double projX = x1 + t * dx;
        double projY = y1 + t * dy;
        
        return Math.sqrt(Math.pow(px - projX, 2) + Math.pow(py - projY, 2));
    }

    /**
     * Проверяет, находится ли точка внутри полигона (алгоритм ray casting).
     */
    private boolean isPointInPolygon(ArrayList<Point2f> polygonPoints, double x, double y) {
        if (polygonPoints.size() < 3) {
            return false;
        }

        boolean inside = false;
        for (int i = 0, j = polygonPoints.size() - 1; i < polygonPoints.size(); j = i++) {
            double xi = polygonPoints.get(i).x;
            double yi = polygonPoints.get(i).y;
            double xj = polygonPoints.get(j).x;
            double yj = polygonPoints.get(j).y;
            
            boolean intersect = ((yi > y) != (yj > y)) && 
                               (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
            if (intersect) {
                inside = !inside;
            }
        }
        
        return inside;
    }

    /**
     * Удаляет полигон по индексу.
     * 
     * @param model модель
     * @param polygonIndex индекс полигона
     */
    private void deletePolygonByIndex(Model model, int polygonIndex) {
        try {
            // Спрашиваем, удалять ли неиспользуемые вершины
            Alert confirmDialog = new Alert(AlertType.CONFIRMATION);
            confirmDialog.setTitle("Delete Polygon");
            confirmDialog.setHeaderText("Delete unused vertices?");
            confirmDialog.setContentText("Do you want to delete vertices that are no longer used by any polygon?");
            
            ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            
            confirmDialog.getButtonTypes().setAll(yesButton, noButton, cancelButton);
            
            Optional<ButtonType> confirmResult = confirmDialog.showAndWait();
            
            if (confirmResult.isPresent() && confirmResult.get() == cancelButton) {
                return;
            }
            
            boolean deleteFreeVertices = confirmResult.isPresent() && confirmResult.get() == yesButton;
            
            Set<Integer> polygonIndicesToDelete = new HashSet<>();
            polygonIndicesToDelete.add(polygonIndex);
            
            PolygonRemover.deletePolygons(model, polygonIndicesToDelete, deleteFreeVertices);
            NormalCalculator.recalculateNormals(model);
            
            updateStatusBar();
            updateSceneInfo();
            updateTransformUI();
            
        } catch (Exception e) {
            showError("Error deleting polygon", "Failed to delete polygon: " + e.getMessage());
        }
    }

    /**
     * Удаляет вершину по индексу.
     * 
     * @param model модель
     * @param vertexIndex индекс вершины
     */
    private void deleteVertexByIndex(Model model, int vertexIndex) {
        try {
            Alert warningDialog = new Alert(AlertType.CONFIRMATION);
            warningDialog.setTitle("Delete Vertex");
            warningDialog.setHeaderText("Warning: Polygons will be deleted");
            warningDialog.setContentText(
                "Deleting a vertex will also delete all polygons that use this vertex.\n" +
                "Do you want to continue?"
            );
            
            Optional<ButtonType> confirmResult = warningDialog.showAndWait();
            
            if (!confirmResult.isPresent() || confirmResult.get() != ButtonType.OK) {
                return;
            }
            
            Alert freeVerticesDialog = new Alert(AlertType.CONFIRMATION);
            freeVerticesDialog.setTitle("Delete Free Vertices");
            freeVerticesDialog.setHeaderText("Delete initially free vertices?");
            freeVerticesDialog.setContentText("Do you want to delete vertices that were never used by any polygon?");
            
            ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
            
            freeVerticesDialog.getButtonTypes().setAll(yesButton, noButton);
            
            Optional<ButtonType> freeVerticesResult = freeVerticesDialog.showAndWait();
            boolean removeInitiallyFreeVertices = freeVerticesResult.isPresent() && freeVerticesResult.get() == yesButton;
            
            Set<Integer> verticesToDelete = new HashSet<>();
            verticesToDelete.add(vertexIndex);
            
            int polygonsBefore = model.getPolygonCount();
            VertexRemover.deleteVertices(model, verticesToDelete, removeInitiallyFreeVertices);
            int polygonsAfter = model.getPolygonCount();
            int deletedPolygons = polygonsBefore - polygonsAfter;
            
            NormalCalculator.recalculateNormals(model);
            
            showSuccess("Vertex deleted", 
                String.format("Vertex %d has been deleted.\n%d polygon(s) were also deleted.", 
                    vertexIndex, deletedPolygons));
            
            updateStatusBar();
            updateSceneInfo();
            updateTransformUI();
            
        } catch (Exception e) {
            showError("Error deleting vertex", "Failed to delete vertex: " + e.getMessage());
        }
    }

    /**
     * Переключает тему приложения между светлой и темной.
     */
    @FXML
    private void handleToggleTheme() {
        isDarkTheme = !isDarkTheme;
        applyTheme();
    }

    /**
     * Применяет выбранную тему к сцене.
     */
    private void applyTheme() {
        if (borderPane == null) {
            return;
        }

        Scene scene = borderPane.getScene();
        if (scene == null) {
            return;
        }

        if (isDarkTheme) {
            try {
                String darkThemePath = getClass().getResource("/com/cgvsu/styles/dark-theme.css").toExternalForm();
                scene.getStylesheets().clear();
                scene.getStylesheets().add(darkThemePath);
            } catch (Exception e) {
                // Если не удалось загрузить тему, просто очищаем стили
                scene.getStylesheets().clear();
            }
        } else {
            scene.getStylesheets().clear();
        }
        
        // Обновляем текст меню
        if (toggleThemeMenuItem != null) {
            toggleThemeMenuItem.setText(isDarkTheme ? "Toggle Light Theme" : "Toggle Dark Theme");
        }
    }
}
