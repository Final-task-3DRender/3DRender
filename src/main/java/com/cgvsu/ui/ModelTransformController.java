package com.cgvsu.ui;

import com.cgvsu.model.ModelTransform;
import com.cgvsu.math.Vector3f;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Контроллер для управления трансформациями модели через UI.
 * 
 * <p>Предоставляет методы для:
 * <ul>
 *   <li>Настройки UI элементов (поля ввода, кнопки)</li>
 *   <li>Обновления полей ввода значениями из трансформаций</li>
 *   <li>Обработки изменений трансформаций</li>
 * </ul>
 * 
 * <p>Поддерживает три режима трансформации: MOVE (перенос), ROTATE (вращение), SCALE (масштаб).
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class ModelTransformController {
    
    /**
     * Режимы трансформации модели.
     */
    public enum TransformMode {
        /** Режим переноса (изменение позиции) */
        MOVE,
        /** Режим вращения (изменение углов поворота) */
        ROTATE,
        /** Режим масштабирования (изменение масштаба) */
        SCALE
    }
    
    private static final float TRANSFORM_STEP = 1.0f;
    private static final float ROTATION_STEP = 5.0f;
    private static final float SCALE_STEP = 0.1f;
    
    private TransformMode currentMode = TransformMode.MOVE;
    
    // Поля ввода для позиции
    private TextField positionXField, positionYField, positionZField;
    private Button positionXDecButton, positionXIncButton;
    private Button positionYDecButton, positionYIncButton;
    private Button positionZDecButton, positionZIncButton;
    
    // Поля ввода для вращения
    private TextField rotationXField, rotationYField, rotationZField;
    private Button rotationXDecButton, rotationXIncButton;
    private Button rotationYDecButton, rotationYIncButton;
    private Button rotationZDecButton, rotationZIncButton;
    
    // Поля ввода для масштаба
    private TextField scaleXField, scaleYField, scaleZField;
    private Button scaleXDecButton, scaleXIncButton;
    private Button scaleYDecButton, scaleYIncButton;
    private Button scaleZDecButton, scaleZIncButton;
    
    // Кнопки режимов и метка текущего режима
    private Button moveModeButton, rotateModeButton, scaleModeButton;
    private Label currentModeLabel;
    
    /**
     * Инициализирует контроллер с UI элементами.
     * 
     * @param positionXField поле ввода позиции X
     * @param positionYField поле ввода позиции Y
     * @param positionZField поле ввода позиции Z
     * @param positionXDecButton кнопка уменьшения позиции X
     * @param positionXIncButton кнопка увеличения позиции X
     * @param positionYDecButton кнопка уменьшения позиции Y
     * @param positionYIncButton кнопка увеличения позиции Y
     * @param positionZDecButton кнопка уменьшения позиции Z
     * @param positionZIncButton кнопка увеличения позиции Z
     * @param rotationXField поле ввода вращения X
     * @param rotationYField поле ввода вращения Y
     * @param rotationZField поле ввода вращения Z
     * @param rotationXDecButton кнопка уменьшения вращения X
     * @param rotationXIncButton кнопка увеличения вращения X
     * @param rotationYDecButton кнопка уменьшения вращения Y
     * @param rotationYIncButton кнопка увеличения вращения Y
     * @param rotationZDecButton кнопка уменьшения вращения Z
     * @param rotationZIncButton кнопка увеличения вращения Z
     * @param scaleXField поле ввода масштаба X
     * @param scaleYField поле ввода масштаба Y
     * @param scaleZField поле ввода масштаба Z
     * @param scaleXDecButton кнопка уменьшения масштаба X
     * @param scaleXIncButton кнопка увеличения масштаба X
     * @param scaleYDecButton кнопка уменьшения масштаба Y
     * @param scaleYIncButton кнопка увеличения масштаба Y
     * @param scaleZDecButton кнопка уменьшения масштаба Z
     * @param scaleZIncButton кнопка увеличения масштаба Z
     * @param moveModeButton кнопка режима переноса
     * @param rotateModeButton кнопка режима вращения
     * @param scaleModeButton кнопка режима масштабирования
     * @param currentModeLabel метка текущего режима
     */
    public ModelTransformController(
            TextField positionXField, TextField positionYField, TextField positionZField,
            Button positionXDecButton, Button positionXIncButton,
            Button positionYDecButton, Button positionYIncButton,
            Button positionZDecButton, Button positionZIncButton,
            TextField rotationXField, TextField rotationYField, TextField rotationZField,
            Button rotationXDecButton, Button rotationXIncButton,
            Button rotationYDecButton, Button rotationYIncButton,
            Button rotationZDecButton, Button rotationZIncButton,
            TextField scaleXField, TextField scaleYField, TextField scaleZField,
            Button scaleXDecButton, Button scaleXIncButton,
            Button scaleYDecButton, Button scaleYIncButton,
            Button scaleZDecButton, Button scaleZIncButton,
            Button moveModeButton, Button rotateModeButton, Button scaleModeButton,
            Label currentModeLabel) {
        
        this.positionXField = positionXField;
        this.positionYField = positionYField;
        this.positionZField = positionZField;
        this.positionXDecButton = positionXDecButton;
        this.positionXIncButton = positionXIncButton;
        this.positionYDecButton = positionYDecButton;
        this.positionYIncButton = positionYIncButton;
        this.positionZDecButton = positionZDecButton;
        this.positionZIncButton = positionZIncButton;
        
        this.rotationXField = rotationXField;
        this.rotationYField = rotationYField;
        this.rotationZField = rotationZField;
        this.rotationXDecButton = rotationXDecButton;
        this.rotationXIncButton = rotationXIncButton;
        this.rotationYDecButton = rotationYDecButton;
        this.rotationYIncButton = rotationYIncButton;
        this.rotationZDecButton = rotationZDecButton;
        this.rotationZIncButton = rotationZIncButton;
        
        this.scaleXField = scaleXField;
        this.scaleYField = scaleYField;
        this.scaleZField = scaleZField;
        this.scaleXDecButton = scaleXDecButton;
        this.scaleXIncButton = scaleXIncButton;
        this.scaleYDecButton = scaleYDecButton;
        this.scaleYIncButton = scaleYIncButton;
        this.scaleZDecButton = scaleZDecButton;
        this.scaleZIncButton = scaleZIncButton;
        
        this.moveModeButton = moveModeButton;
        this.rotateModeButton = rotateModeButton;
        this.scaleModeButton = scaleModeButton;
        this.currentModeLabel = currentModeLabel;
    }
    
    /**
     * Настраивает UI элементы для работы с трансформациями.
     * 
     * @param transform трансформации модели для управления
     * @param onTransformChanged callback, вызываемый при изменении трансформаций
     */
    public void setup(ModelTransform transform, Runnable onTransformChanged) {
        if (transform == null) {
            return;
        }
        
        setupPositionControls(transform, onTransformChanged);
        setupRotationControls(transform, onTransformChanged);
        setupScaleControls(transform, onTransformChanged);
        setupModeButtons(onTransformChanged);
        updateFields(transform);
    }
    
    /**
     * Обновляет поля ввода значениями из трансформаций.
     * 
     * @param transform трансформации для отображения
     */
    public void updateFields(ModelTransform transform) {
        if (transform == null) {
            clearFields();
            return;
        }
        
        updatePositionFields(transform);
        updateRotationFields(transform);
        updateScaleFields(transform);
    }
    
    /**
     * Сбрасывает трансформации к начальным значениям.
     * 
     * @param transform трансформации для сброса
     */
    public void resetTransform(ModelTransform transform) {
        if (transform != null) {
            transform.reset();
        }
    }
    
    /**
     * Устанавливает режим трансформации.
     * 
     * @param mode новый режим
     */
    public void setMode(TransformMode mode) {
        this.currentMode = mode;
        updateModeButtons();
    }
    
    /**
     * Возвращает текущий режим трансформации.
     * 
     * @return текущий режим
     */
    public TransformMode getMode() {
        return currentMode;
    }
    
    private void setupPositionControls(ModelTransform transform, Runnable onChanged) {
        setupTextField(positionXField, () -> {
            try {
                float value = Float.parseFloat(positionXField.getText());
                transform.setPosition(transform.getPosition().add(
                    new Vector3f(value - transform.getPosition().x, 0, 0)));
                onChanged.run();
            } catch (NumberFormatException e) {
                updatePositionFields(transform);
            }
        });
        setupIncDecButtons(positionXDecButton, positionXIncButton,
            () -> adjustPosition(transform, 'x', -TRANSFORM_STEP, onChanged),
            () -> adjustPosition(transform, 'x', TRANSFORM_STEP, onChanged));
        
        setupTextField(positionYField, () -> {
            try {
                float value = Float.parseFloat(positionYField.getText());
                transform.setPosition(transform.getPosition().add(
                    new Vector3f(0, value - transform.getPosition().y, 0)));
                onChanged.run();
            } catch (NumberFormatException e) {
                updatePositionFields(transform);
            }
        });
        setupIncDecButtons(positionYDecButton, positionYIncButton,
            () -> adjustPosition(transform, 'y', -TRANSFORM_STEP, onChanged),
            () -> adjustPosition(transform, 'y', TRANSFORM_STEP, onChanged));
        
        setupTextField(positionZField, () -> {
            try {
                float value = Float.parseFloat(positionZField.getText());
                transform.setPosition(transform.getPosition().add(
                    new Vector3f(0, 0, value - transform.getPosition().z)));
                onChanged.run();
            } catch (NumberFormatException e) {
                updatePositionFields(transform);
            }
        });
        setupIncDecButtons(positionZDecButton, positionZIncButton,
            () -> adjustPosition(transform, 'z', -TRANSFORM_STEP, onChanged),
            () -> adjustPosition(transform, 'z', TRANSFORM_STEP, onChanged));
    }
    
    private void setupRotationControls(ModelTransform transform, Runnable onChanged) {
        setupTextField(rotationXField, () -> {
            try {
                float value = Float.parseFloat(rotationXField.getText());
                transform.setRotation(new Vector3f(
                    value, transform.getRotation().y, transform.getRotation().z));
                onChanged.run();
            } catch (NumberFormatException e) {
                updateRotationFields(transform);
            }
        });
        setupIncDecButtons(rotationXDecButton, rotationXIncButton,
            () -> adjustRotation(transform, 'x', -ROTATION_STEP, onChanged),
            () -> adjustRotation(transform, 'x', ROTATION_STEP, onChanged));
        
        setupTextField(rotationYField, () -> {
            try {
                float value = Float.parseFloat(rotationYField.getText());
                transform.setRotation(new Vector3f(
                    transform.getRotation().x, value, transform.getRotation().z));
                onChanged.run();
            } catch (NumberFormatException e) {
                updateRotationFields(transform);
            }
        });
        setupIncDecButtons(rotationYDecButton, rotationYIncButton,
            () -> adjustRotation(transform, 'y', -ROTATION_STEP, onChanged),
            () -> adjustRotation(transform, 'y', ROTATION_STEP, onChanged));
        
        setupTextField(rotationZField, () -> {
            try {
                float value = Float.parseFloat(rotationZField.getText());
                transform.setRotation(new Vector3f(
                    transform.getRotation().x, transform.getRotation().y, value));
                onChanged.run();
            } catch (NumberFormatException e) {
                updateRotationFields(transform);
            }
        });
        setupIncDecButtons(rotationZDecButton, rotationZIncButton,
            () -> adjustRotation(transform, 'z', -ROTATION_STEP, onChanged),
            () -> adjustRotation(transform, 'z', ROTATION_STEP, onChanged));
    }
    
    private void setupScaleControls(ModelTransform transform, Runnable onChanged) {
        setupTextField(scaleXField, () -> {
            try {
                float value = Float.parseFloat(scaleXField.getText());
                float newValue = Math.max(0.01f, value);
                transform.setScale(new Vector3f(
                    newValue, transform.getScale().y, transform.getScale().z));
                onChanged.run();
            } catch (NumberFormatException e) {
                updateScaleFields(transform);
            }
        });
        setupIncDecButtons(scaleXDecButton, scaleXIncButton,
            () -> adjustScale(transform, 'x', -SCALE_STEP, onChanged),
            () -> adjustScale(transform, 'x', SCALE_STEP, onChanged));
        
        setupTextField(scaleYField, () -> {
            try {
                float value = Float.parseFloat(scaleYField.getText());
                float newValue = Math.max(0.01f, value);
                transform.setScale(new Vector3f(
                    transform.getScale().x, newValue, transform.getScale().z));
                onChanged.run();
            } catch (NumberFormatException e) {
                updateScaleFields(transform);
            }
        });
        setupIncDecButtons(scaleYDecButton, scaleYIncButton,
            () -> adjustScale(transform, 'y', -SCALE_STEP, onChanged),
            () -> adjustScale(transform, 'y', SCALE_STEP, onChanged));
        
        setupTextField(scaleZField, () -> {
            try {
                float value = Float.parseFloat(scaleZField.getText());
                float newValue = Math.max(0.01f, value);
                transform.setScale(new Vector3f(
                    transform.getScale().x, transform.getScale().y, newValue));
                onChanged.run();
            } catch (NumberFormatException e) {
                updateScaleFields(transform);
            }
        });
        setupIncDecButtons(scaleZDecButton, scaleZIncButton,
            () -> adjustScale(transform, 'z', -SCALE_STEP, onChanged),
            () -> adjustScale(transform, 'z', SCALE_STEP, onChanged));
    }
    
    private void setupModeButtons(Runnable onChanged) {
        if (moveModeButton != null) {
            moveModeButton.setOnAction(e -> {
                setMode(TransformMode.MOVE);
                onChanged.run();
            });
        }
        if (rotateModeButton != null) {
            rotateModeButton.setOnAction(e -> {
                setMode(TransformMode.ROTATE);
                onChanged.run();
            });
        }
        if (scaleModeButton != null) {
            scaleModeButton.setOnAction(e -> {
                setMode(TransformMode.SCALE);
                onChanged.run();
            });
        }
        updateModeButtons();
    }
    
    private void setupTextField(TextField field, Runnable onAction) {
        if (field != null) {
            field.setOnAction(e -> onAction.run());
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
    
    private void adjustPosition(ModelTransform transform, char axis, float delta, Runnable onChanged) {
        Vector3f pos = transform.getPosition();
        switch (axis) {
            case 'x' -> transform.setPosition(new Vector3f(pos.x + delta, pos.y, pos.z));
            case 'y' -> transform.setPosition(new Vector3f(pos.x, pos.y + delta, pos.z));
            case 'z' -> transform.setPosition(new Vector3f(pos.x, pos.y, pos.z + delta));
        }
        updatePositionFields(transform);
        onChanged.run();
    }
    
    private void adjustRotation(ModelTransform transform, char axis, float delta, Runnable onChanged) {
        Vector3f rot = transform.getRotation();
        switch (axis) {
            case 'x' -> transform.setRotation(new Vector3f(rot.x + delta, rot.y, rot.z));
            case 'y' -> transform.setRotation(new Vector3f(rot.x, rot.y + delta, rot.z));
            case 'z' -> transform.setRotation(new Vector3f(rot.x, rot.y, rot.z + delta));
        }
        updateRotationFields(transform);
        onChanged.run();
    }
    
    private void adjustScale(ModelTransform transform, char axis, float delta, Runnable onChanged) {
        Vector3f scale = transform.getScale();
        switch (axis) {
            case 'x' -> {
                float newValue = Math.max(0.01f, scale.x + delta);
                transform.setScale(new Vector3f(newValue, scale.y, scale.z));
            }
            case 'y' -> {
                float newValue = Math.max(0.01f, scale.y + delta);
                transform.setScale(new Vector3f(scale.x, newValue, scale.z));
            }
            case 'z' -> {
                float newValue = Math.max(0.01f, scale.z + delta);
                transform.setScale(new Vector3f(scale.x, scale.y, newValue));
            }
        }
        updateScaleFields(transform);
        onChanged.run();
    }
    
    private void updatePositionFields(ModelTransform transform) {
        Vector3f pos = transform.getPosition();
        if (positionXField != null && !positionXField.isFocused()) {
            positionXField.setText(String.format("%.2f", pos.x));
        }
        if (positionYField != null && !positionYField.isFocused()) {
            positionYField.setText(String.format("%.2f", pos.y));
        }
        if (positionZField != null && !positionZField.isFocused()) {
            positionZField.setText(String.format("%.2f", pos.z));
        }
    }
    
    private void updateRotationFields(ModelTransform transform) {
        Vector3f rot = transform.getRotation();
        if (rotationXField != null && !rotationXField.isFocused()) {
            rotationXField.setText(String.format("%.1f", rot.x));
        }
        if (rotationYField != null && !rotationYField.isFocused()) {
            rotationYField.setText(String.format("%.1f", rot.y));
        }
        if (rotationZField != null && !rotationZField.isFocused()) {
            rotationZField.setText(String.format("%.1f", rot.z));
        }
    }
    
    private void updateScaleFields(ModelTransform transform) {
        Vector3f scale = transform.getScale();
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
    
    private void clearFields() {
        if (positionXField != null && !positionXField.isFocused()) positionXField.setText("");
        if (positionYField != null && !positionYField.isFocused()) positionYField.setText("");
        if (positionZField != null && !positionZField.isFocused()) positionZField.setText("");
        if (rotationXField != null && !rotationXField.isFocused()) rotationXField.setText("");
        if (rotationYField != null && !rotationYField.isFocused()) rotationYField.setText("");
        if (rotationZField != null && !rotationZField.isFocused()) rotationZField.setText("");
        if (scaleXField != null && !scaleXField.isFocused()) scaleXField.setText("");
        if (scaleYField != null && !scaleYField.isFocused()) scaleYField.setText("");
        if (scaleZField != null && !scaleZField.isFocused()) scaleZField.setText("");
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
        if (currentModeLabel != null) {
            String modeText = switch (currentMode) {
                case MOVE -> "Mode: Move";
                case ROTATE -> "Mode: Rotate";
                case SCALE -> "Mode: Scale";
            };
            currentModeLabel.setText(modeText);
        }
    }
}
