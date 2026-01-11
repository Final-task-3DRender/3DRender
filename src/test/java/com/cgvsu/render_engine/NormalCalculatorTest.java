package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * Тесты для NormalCalculator
 */
class NormalCalculatorTest {
    
    private static final float EPSILON = 1e-5f;
    
    @Test
    void testRecalculateNormals_SimpleTriangle() {
        // Создаем простой треугольник в плоскости XY
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        model.vertices.add(new Vector3f(0, 1, 0));
        
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(java.util.Arrays.asList(0, 1, 2)));
        model.polygons.add(polygon);
        
        // Пересчитываем нормали
        NormalCalculator.recalculateNormals(model);
        
        // Проверяем, что нормаль была создана
        Assertions.assertEquals(1, model.normals.size());
        
        // Нормаль должна быть направлена вдоль оси Z (вверх)
        Vector3f normal = model.normals.get(0);
        Assertions.assertEquals(0.0, normal.x, EPSILON);
        Assertions.assertEquals(0.0, normal.y, EPSILON);
        Assertions.assertEquals(1.0, Math.abs(normal.z), EPSILON);
        
        // Проверяем, что индексы нормалей установлены
        Assertions.assertEquals(3, polygon.getNormalIndices().size());
        Assertions.assertEquals(0, polygon.getNormalIndices().get(0).intValue());
        Assertions.assertEquals(0, polygon.getNormalIndices().get(1).intValue());
        Assertions.assertEquals(0, polygon.getNormalIndices().get(2).intValue());
    }
    
    @Test
    void testRecalculateNormals_MultipleTriangles() {
        // Создаем два треугольника
        Model model = new Model();
        // Первый треугольник в плоскости XY
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        model.vertices.add(new Vector3f(0, 1, 0));
        // Второй треугольник в плоскости XZ
        model.vertices.add(new Vector3f(0, 0, 1));
        model.vertices.add(new Vector3f(1, 0, 1));
        
        Polygon polygon1 = new Polygon();
        polygon1.setVertexIndices(new ArrayList<>(java.util.Arrays.asList(0, 1, 2)));
        model.polygons.add(polygon1);
        
        Polygon polygon2 = new Polygon();
        polygon2.setVertexIndices(new ArrayList<>(java.util.Arrays.asList(0, 3, 4)));
        model.polygons.add(polygon2);
        
        // Пересчитываем нормали
        NormalCalculator.recalculateNormals(model);
        
        // Проверяем, что создано 2 нормали
        Assertions.assertEquals(2, model.normals.size());
        
        // Первая нормаль должна быть направлена вдоль Z
        Vector3f normal1 = model.normals.get(0);
        Assertions.assertEquals(1.0, Math.abs(normal1.z), EPSILON, "First normal should point along Z axis");
        
        // Вторая нормаль должна быть направлена вдоль Y
        Vector3f normal2 = model.normals.get(1);
        Assertions.assertEquals(1.0, Math.abs(normal2.y), EPSILON, "Second normal should point along Y axis");
    }
    
    @Test
    void testRecalculateNormals_ClearsOldNormals() {
        // Создаем модель с существующими нормалями
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        model.vertices.add(new Vector3f(0, 1, 0));
        model.normals.add(new Vector3f(999, 999, 999)); // Старая нормаль
        
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(java.util.Arrays.asList(0, 1, 2)));
        model.polygons.add(polygon);
        
        // Пересчитываем нормали
        NormalCalculator.recalculateNormals(model);
        
        // Проверяем, что старая нормаль была удалена
        Assertions.assertEquals(1, model.normals.size());
        Vector3f normal = model.normals.get(0);
        Assertions.assertNotEquals(999.0, normal.x, EPSILON, "Old normal should be cleared");
    }
    
    @Test
    void testRecalculateNormals_EmptyModel() {
        Model model = new Model();
        
        // Не должно быть исключений
        Assertions.assertDoesNotThrow(() -> NormalCalculator.recalculateNormals(model));
        Assertions.assertTrue(model.normals.isEmpty());
    }
    
    @Test
    void testRecalculateNormals_NullModel() {
        // Не должно быть исключений
        Assertions.assertDoesNotThrow(() -> NormalCalculator.recalculateNormals(null));
    }
    
    @Test
    void testRecalculateNormals_EmptyPolygons() {
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        
        // Модель без полигонов
        Assertions.assertDoesNotThrow(() -> NormalCalculator.recalculateNormals(model));
        Assertions.assertTrue(model.normals.isEmpty());
    }
    
    @Test
    void testCalculatePolygonNormal_SimpleTriangle() {
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        model.vertices.add(new Vector3f(0, 1, 0));
        
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(java.util.Arrays.asList(0, 1, 2)));
        
        Vector3f normal = NormalCalculator.calculatePolygonNormal(model, polygon);
        
        // Нормаль должна быть направлена вдоль оси Z
        Assertions.assertEquals(0.0, normal.x, EPSILON);
        Assertions.assertEquals(0.0, normal.y, EPSILON);
        Assertions.assertEquals(1.0, Math.abs(normal.z), EPSILON);
        
        // Нормаль должна быть нормализована
        float length = normal.length();
        Assertions.assertEquals(1.0, length, EPSILON);
    }
    
    @Test
    void testCalculatePolygonNormal_DifferentOrientation() {
        // Треугольник в плоскости XZ
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        model.vertices.add(new Vector3f(0, 0, 1));
        
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(java.util.Arrays.asList(0, 1, 2)));
        
        Vector3f normal = NormalCalculator.calculatePolygonNormal(model, polygon);
        
        // Нормаль должна быть направлена вдоль оси Y (вверх или вниз)
        Assertions.assertEquals(0.0, normal.x, EPSILON);
        Assertions.assertEquals(1.0, Math.abs(normal.y), EPSILON);
        Assertions.assertEquals(0.0, normal.z, EPSILON);
    }
    
    @Test
    void testCalculatePolygonNormal_NullModel() {
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(java.util.Arrays.asList(0, 1, 2)));
        
        Vector3f normal = NormalCalculator.calculatePolygonNormal(null, polygon);
        
        // Должна вернуться нормаль по умолчанию
        Assertions.assertNotNull(normal);
        Assertions.assertEquals(0.0, normal.x, EPSILON);
        Assertions.assertEquals(0.0, normal.y, EPSILON);
        Assertions.assertEquals(1.0, normal.z, EPSILON);
    }
    
    @Test
    void testCalculatePolygonNormal_NullPolygon() {
        Model model = new Model();
        
        Vector3f normal = NormalCalculator.calculatePolygonNormal(model, null);
        
        // Должна вернуться нормаль по умолчанию
        Assertions.assertNotNull(normal);
        Assertions.assertEquals(0.0, normal.x, EPSILON);
        Assertions.assertEquals(0.0, normal.y, EPSILON);
        Assertions.assertEquals(1.0, normal.z, EPSILON);
    }
    
    @Test
    void testRecalculateNormals_Cube() {
        // Создаем простой куб (8 вершин)
        Model model = new Model();
        // Вершины куба
        model.vertices.add(new Vector3f(-1, -1, -1)); // 0
        model.vertices.add(new Vector3f(1, -1, -1));  // 1
        model.vertices.add(new Vector3f(1, 1, -1));   // 2
        model.vertices.add(new Vector3f(-1, 1, -1));  // 3
        model.vertices.add(new Vector3f(-1, -1, 1));  // 4
        model.vertices.add(new Vector3f(1, -1, 1));   // 5
        model.vertices.add(new Vector3f(1, 1, 1));    // 6
        model.vertices.add(new Vector3f(-1, 1, 1));   // 7
        
        // Передняя грань (Z = 1)
        Polygon front1 = new Polygon();
        front1.setVertexIndices(new ArrayList<>(java.util.Arrays.asList(4, 5, 6)));
        model.polygons.add(front1);
        
        Polygon front2 = new Polygon();
        front2.setVertexIndices(new ArrayList<>(java.util.Arrays.asList(4, 6, 7)));
        model.polygons.add(front2);
        
        // Пересчитываем нормали
        NormalCalculator.recalculateNormals(model);
        
        // Проверяем, что создано 2 нормали
        Assertions.assertEquals(2, model.normals.size());
        
        // Обе нормали должны быть направлены вдоль оси Z (вперед)
        for (Vector3f normal : model.normals) {
            Assertions.assertEquals(1.0, Math.abs(normal.z), EPSILON, "Normal should point along Z axis");
            Assertions.assertEquals(1.0, normal.length(), EPSILON, "Normal should be normalized");
        }
    }
    
    @Test
    void testCalculatePolygonNormal_Normalized() {
        // Создаем треугольник с большими координатами
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(100, 0, 0));
        model.vertices.add(new Vector3f(0, 100, 0));
        
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(java.util.Arrays.asList(0, 1, 2)));
        
        Vector3f normal = NormalCalculator.calculatePolygonNormal(model, polygon);
        
        // Нормаль должна быть нормализована (длина = 1)
        float length = normal.length();
        Assertions.assertEquals(1.0, length, EPSILON);
    }
}
