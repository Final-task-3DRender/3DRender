package com.cgvsu.ui;

import com.cgvsu.model.Model;
import com.cgvsu.model.ModelTransform;
import com.cgvsu.model.ModelTransformer;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.render_engine.NormalCalculator;
import com.cgvsu.triangulation.EarCuttingTriangulator;
import com.cgvsu.triangulation.Triangulator;
import com.cgvsu.transform.ModelMatrixBuilder;
import com.cgvsu.math.Matrix4f;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Обработчик операций с файлами (загрузка и сохранение моделей).
 * 
 * <p>Предоставляет методы для:
 * <ul>
 *   <li>Загрузки моделей из OBJ файлов</li>
 *   <li>Сохранения моделей в OBJ файлы (с выбором: исходная или с трансформациями)</li>
 * </ul>
 * 
 * <p>Автоматически выполняет триангуляцию и пересчет нормалей при загрузке.
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class FileOperationsHandler {
    
    /**
     * Загружает модель из OBJ файла.
     * 
     * <p>Выполняет:
     * <ol>
     *   <li>Чтение и парсинг OBJ файла</li>
     *   <li>Триангуляцию модели (разбиение полигонов на треугольники), если включена</li>
     *   <li>Пересчет нормалей для правильного отображения</li>
     * </ol>
     * 
     * @param file файл OBJ для загрузки
     * @param enableTriangulation если true, выполняется триангуляция модели
     * @return загруженная и обработанная модель
     * @throws IOException если произошла ошибка при чтении файла
     * @throws com.cgvsu.objreader.ObjReaderException если файл содержит ошибки формата
     */
    public static Model loadModel(File file, boolean enableTriangulation) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("File does not exist: " + (file != null ? file.getAbsolutePath() : "null"));
        }
        
        Path fileName = Path.of(file.getAbsolutePath());
        String fileContent = Files.readString(fileName);
        
        Model mesh = ObjReader.read(fileContent);
        
        // НЕ триангулируем при загрузке - триангуляция выполняется динамически в RenderEngine
        // Это позволяет включать/выключать триангуляцию без перезагрузки модели
        
        NormalCalculator.recalculateNormals(mesh);
        
        return mesh;
    }
    
    /**
     * Загружает модель из OBJ файла с триангуляцией по умолчанию (включена).
     * 
     * @param file файл OBJ для загрузки
     * @return загруженная и обработанная модель
     * @throws IOException если произошла ошибка при чтении файла
     * @throws com.cgvsu.objreader.ObjReaderException если файл содержит ошибки формата
     */
    public static Model loadModel(File file) throws IOException {
        return loadModel(file, true);
    }
    
    /**
     * Сохраняет модель в OBJ файл с выбором: исходная модель или с примененными трансформациями.
     * 
     * <p>Показывает диалог выбора пользователю:
     * <ul>
     *   <li>Сохранить исходную модель (без трансформаций)</li>
     *   <li>Сохранить модель с примененными трансформациями</li>
     * </ul>
     * 
     * @param model модель для сохранения
     * @param transform трансформации модели (может быть null)
     * @param modelName имя модели (для предложения имени файла)
     * @param parentWindow родительское окно для диалога
     * @return true если сохранение прошло успешно, false если пользователь отменил операцию
     * @throws IOException если произошла ошибка при записи файла
     */
    public static boolean saveModelWithChoice(
            Model model, 
            ModelTransform transform, 
            String modelName,
            Stage parentWindow) throws IOException {
        
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        
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
            return false;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Save Model");
        fileChooser.setInitialFileName(modelName != null ? modelName : "model.obj");
        
        File file = fileChooser.showSaveDialog(parentWindow);
        if (file == null) {
            return false;
        }
        
        Model modelToSave;
        if (result.get() == transformedButton && transform != null) {
            Matrix4f transformMatrix = ModelMatrixBuilder.build(transform);
            modelToSave = ModelTransformer.applyTransform(model, transformMatrix);
        } else {
            modelToSave = model;
        }
        
        ObjWriter.saveModel(modelToSave, file.getAbsolutePath());
        return true;
    }
    
    /**
     * Сохраняет модель в OBJ файл без диалога выбора.
     * 
     * @param model модель для сохранения
     * @param file файл для сохранения
     * @throws IOException если произошла ошибка при записи файла
     */
    public static void saveModel(Model model, File file) throws IOException {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        ObjWriter.saveModel(model, file.getAbsolutePath());
    }
}
