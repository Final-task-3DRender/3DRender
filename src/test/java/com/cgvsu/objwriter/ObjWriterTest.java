package com.cgvsu.objwriter;

import org.junit.jupiter.api.Test;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.objwriter.ObjWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class ObjWriterTest {

    @Test
    void testFullPolygon() throws IOException {
        // Полный полигон со всеми данными (вершины, текстуры, нормали)
        // Проверяет формат v/vt/vn
        Model model = new Model();
        for (Vector3f v : Arrays.asList(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 0, 0),
                new Vector3f(0, 1, 0)
        )) {
            model.addVertex(v);
        }

        for (Vector2f tv : Arrays.asList(
                new Vector2f(0, 0),
                new Vector2f(1, 0),
                new Vector2f(0, 1)
        )) {
            model.addTextureVertex(tv);
        }

        model.addNormal(new Vector3f(0, 0, 1));

        Polygon poly = new Polygon();
        poly.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        poly.setTextureVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        poly.setNormalIndices(new ArrayList<>(Arrays.asList(0, 0, 0)));
        model.addPolygon(poly);

        ObjWriter.saveModel(model, "test_full.obj");
        String content = Files.readString(Path.of("test_full.obj"));

        assertTrue(content.contains("f 1/1/1 2/2/1 3/3/1"));
        assertTrue(content.contains("# Created by ObjWriter"));
        assertTrue(content.contains("v 0.000000 0.000000 0.000000"));
        assertTrue(content.contains("vt 0.000000 0.000000"));
        assertTrue(content.contains("vn 0.000000 0.000000 1.000000"));
    }

    @Test
    void testVertexOnly() throws IOException {
        // Только вершины
        // Проверяет формат f v1 v2 v3
        Model model = new Model();
        for (Vector3f v : Arrays.asList(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 0, 0),
                new Vector3f(0, 1, 0)
        )) {
            model.addVertex(v);
        }

        Polygon poly = new Polygon();
        poly.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        model.addPolygon(poly);

        ObjWriter.saveModel(model, "test_vertex_only.obj");
        String content = Files.readString(Path.of("test_vertex_only.obj"));

        assertTrue(content.contains("f 1 2 3"));
        // Не должно быть слешей (нет текстур/нормалей)
        assertFalse(content.contains("/"));
    }

    @Test
    void testTextureOnly() throws IOException {
        // Вершины + текстуры
        // Проверяет формат v/vt
        Model model = new Model();
        for (Vector3f v : Arrays.asList(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 0, 0),
                new Vector3f(0, 1, 0)
        )) {
            model.addVertex(v);
        }

        for (Vector2f tv : Arrays.asList(
                new Vector2f(0, 0),
                new Vector2f(1, 0),
                new Vector2f(0, 1)
        )) {
            model.addTextureVertex(tv);
        }

        Polygon poly = new Polygon();
        poly.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        poly.setTextureVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        model.addPolygon(poly);

        ObjWriter.saveModel(model, "test_texture_only.obj");
        String content = Files.readString(Path.of("test_texture_only.obj"));

        assertTrue(content.contains("f 1/1 2/2 3/3"));
        // Не должно быть двойных слешей (нет нормалей)
        assertFalse(content.contains("//"));
    }

    @Test
    void testNormalOnly() throws IOException {
        // Вершины + нормали
        // Проверяет формат v//vn
        Model model = new Model();
        for (Vector3f v : Arrays.asList(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 0, 0),
                new Vector3f(0, 1, 0)
        )) {
            model.addVertex(v);
        }

        model.addNormal(new Vector3f(0, 0, 1));

        Polygon poly = new Polygon();
        poly.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        poly.setNormalIndices(new ArrayList<>(Arrays.asList(0, 0, 0)));
        model.addPolygon(poly);

        ObjWriter.saveModel(model, "test_normal_only.obj");
        String content = Files.readString(Path.of("test_normal_only.obj"));

        assertTrue(content.contains("f 1//1 2//1 3//1"));
    }

    @Test
    void testNullModel() {
        // Проверка обработки null модели
        Exception exception = assertThrows(IOException.class, () -> {
            ObjWriter.saveModel(null, "test_null.obj");
        });
        assertTrue(exception.getMessage().contains("Invalid model provided") || 
                   exception.getMessage().contains("Model doesn't exist"));
    }

    @Test
    void testPolygonWithWrongTextureCount() {
        // Проверка валидации - несовпадение количества текстур и вершин
        Model model = new Model();
        for (Vector3f v : Arrays.asList(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 0, 0),
                new Vector3f(0, 1, 0)
        )) {
            model.addVertex(v);
        }

        for (Vector2f tv : Arrays.asList(
                new Vector2f(0, 0),
                new Vector2f(1, 0)
        )) {
            model.addTextureVertex(tv);
        }

        Polygon poly = new Polygon();
        poly.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2))); // 3 вершины
        poly.setTextureVertexIndices(new ArrayList<>(Arrays.asList(0, 1))); // 2 текстуры - ошибка!

        model.addPolygon(poly);

        Exception exception = assertThrows(IOException.class, () -> {
            ObjWriter.saveModel(model, "test_wrong_texture.obj");
        });

        assertTrue(exception.getMessage().contains("doesn't match"));
    }

    @Test
    void testEmptyModel() throws IOException {
        // Пустая модель (нет вершин, текстур, нормалей, полигонов)
        // Проверяет, что код корректно обрабатывает пустые данные
        Model model = new Model();
        ObjWriter.saveModel(model, "test_empty.obj");

        assertTrue(Files.exists(Path.of("test_empty.obj")));
        String content = Files.readString(Path.of("test_empty.obj"));

        assertTrue(content.contains("# Created by ObjWriter"));
        // Не должно быть данных о вершинах и полигонах
        assertFalse(content.contains("v "));
        assertFalse(content.contains("f "));
    }

    @org.junit.jupiter.api.AfterEach
    void cleanup() throws IOException {
        String[] testFiles = {
                "test_full.obj", "test_vertex_only.obj", "test_texture_only.obj",
                "test_normal_only.obj", "test_wrong_texture.obj", "test_empty.obj"
        };

        for (String filename : testFiles) {
            Files.deleteIfExists(Path.of(filename));
        }
    }
}


