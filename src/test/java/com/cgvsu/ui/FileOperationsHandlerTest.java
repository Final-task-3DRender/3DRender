package com.cgvsu.ui;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.NormalCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Тесты для FileOperationsHandler.
 * 
 * Проверяют загрузку моделей и пересчет нормалей.
 */
class FileOperationsHandlerTest {
    
    @TempDir
    Path tempDir;
    
    @Test
    void testLoadModel_SimpleTriangle() throws IOException {
        String objContent = """
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            f 1 2 3
            """;
        
        File objFile = tempDir.resolve("triangle.obj").toFile();
        Files.writeString(objFile.toPath(), objContent);
        
        Model model = FileOperationsHandler.loadModel(objFile);
        
        Assertions.assertNotNull(model);
        Assertions.assertEquals(3, model.getVertexCount());
        Assertions.assertEquals(1, model.getPolygonCount());
        Assertions.assertTrue(model.getNormalCount() > 0, "Нормали должны быть пересчитаны");
    }
    
    @Test
    void testLoadModel_NormalsAlwaysRecalculated() throws IOException {
        String objContent = """
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            vn 0.0 0.0 1.0
            f 1//1 2//1 3//1
            """;
        
        File objFile = tempDir.resolve("with_normals.obj").toFile();
        Files.writeString(objFile.toPath(), objContent);
        
        Model model = FileOperationsHandler.loadModel(objFile);
        
        Assertions.assertTrue(model.getNormalCount() > 0, "Нормали должны быть пересчитаны");
        
        if (model.getNormalCount() > 0) {
            var normal = model.getNormal(0);
            Assertions.assertNotNull(normal);
            Assertions.assertTrue(Math.abs(Math.abs(normal.z) - 1.0f) < 0.1f, 
                "Нормаль треугольника в плоскости XY должна быть вдоль оси Z");
        }
    }
    
    @Test
    void testLoadModel_FileNotExists() {
        File nonExistentFile = new File("non_existent_file.obj");
        Assertions.assertThrows(IOException.class, () -> {
            FileOperationsHandler.loadModel(nonExistentFile);
        });
    }
    
    @Test
    void testLoadModel_NullFile() {
        Assertions.assertThrows(IOException.class, () -> {
            FileOperationsHandler.loadModel(null);
        });
    }
    
    @Test
    void testLoadModel_InvalidObjFormat() throws IOException {
        String invalidContent = "This is not a valid OBJ file";
        File objFile = tempDir.resolve("invalid.obj").toFile();
        Files.writeString(objFile.toPath(), invalidContent);
        
        Assertions.assertThrows(com.cgvsu.objreader.ObjReaderException.class, () -> {
            FileOperationsHandler.loadModel(objFile);
        });
    }
}
