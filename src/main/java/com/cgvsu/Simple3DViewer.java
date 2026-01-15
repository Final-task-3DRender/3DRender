package com.cgvsu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Главный класс JavaFX приложения для просмотра и редактирования 3D моделей.
 * 
 * <p>Приложение предоставляет:
 * <ul>
 *   <li>Загрузку и сохранение 3D моделей в формате OBJ</li>
 *   <li>Интерактивное управление камерой (мышь и клавиатура)</li>
 *   <li>Трансформацию моделей (масштабирование, вращение, перенос)</li>
 *   <li>Настройки рендеринга (wireframe, filled, текстуры)</li>
 * </ul>
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class Simple3DViewer extends Application {

    /**
     * Инициализирует и отображает главное окно приложения.
     * 
     * @param stage главное окно приложения
     * @throws IOException если не удалось загрузить FXML файл интерфейса
     */
    @Override
    public void start(Stage stage) throws IOException {
        BorderPane viewport = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("fxml/gui.fxml")));

        Scene scene = new Scene(viewport);
        viewport.prefWidthProperty().bind(scene.widthProperty());
        viewport.prefHeightProperty().bind(scene.heightProperty());

        stage.setTitle("3D Viewer");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    /**
     * Точка входа в JavaFX приложение.
     * 
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        launch();
    }
}