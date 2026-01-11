package com.cgvsu.camera;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Тесты для класса CameraView
 */
class CameraViewTest {
    
    private static final float EPSILON = 1e-5f;
    
    /**
     * Тест создания матрицы вида для стандартного случая.
     * Камера в (0, 0, 5), смотрит на (0, 0, 0).
     */
    @Test
    void testLookAtStandardCase() {
        Vector3f eye = new Vector3f(0, 0, 5);
        Vector3f target = new Vector3f(0, 0, 0);
        Matrix4f viewMatrix = CameraView.lookAt(eye, target);
        
        // Проверяем, что матрица не null
        Assertions.assertNotNull(viewMatrix);
        
        // Применяем матрицу к точке target
        Vector4f targetPoint = new Vector4f(target, 1.0f);
        Vector4f transformed = viewMatrix.multiply(targetPoint);
        
        // После применения view матрицы, target должен быть на оси -Z в пространстве камеры
        // (на расстоянии от камеры, но в правильном направлении)
        Assertions.assertEquals(0.0f, transformed.x, EPSILON);
        Assertions.assertEquals(0.0f, transformed.y, EPSILON);
        // Z координата должна быть отрицательной (направление к камере по оси -Z)
        Assertions.assertTrue(transformed.z < 0, "Target should be on negative Z axis in camera space");
    }
    
    /**
     * Тест создания матрицы вида с явным up вектором.
     */
    @Test
    void testLookAtWithUpVector() {
        Vector3f eye = new Vector3f(0, 0, 5);
        Vector3f target = new Vector3f(0, 0, 0);
        Vector3f up = new Vector3f(0, 1, 0);
        Matrix4f viewMatrix = CameraView.lookAt(eye, target, up);
        
        Assertions.assertNotNull(viewMatrix);
        
        // Применяем матрицу к target
        Vector4f targetPoint = new Vector4f(target, 1.0f);
        Vector4f transformed = viewMatrix.multiply(targetPoint);
        
        // В пространстве камеры target должен быть на оси -Z
        Assertions.assertEquals(0.0f, transformed.x, EPSILON);
        Assertions.assertEquals(0.0f, transformed.y, EPSILON);
        Assertions.assertTrue(transformed.z < 0, "Target should be on negative Z axis");
    }
    
    /**
     * Тест случая, когда eye == target (камера в точке цели).
     * Должна возвращаться единичная матрица.
     */
    @Test
    void testLookAtEyeEqualsTarget() {
        Vector3f eye = new Vector3f(1, 2, 3);
        Vector3f target = new Vector3f(1, 2, 3);
        Matrix4f viewMatrix = CameraView.lookAt(eye, target);
        
        Assertions.assertNotNull(viewMatrix);
        
        // Должна быть единичная матрица
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                float expected = (i == j) ? 1.0f : 0.0f;
                Assertions.assertEquals(expected, viewMatrix.get(i, j), EPSILON,
                    String.format("Element [%d][%d] should be %.1f for identity matrix", i, j, expected));
            }
        }
    }
    
    /**
     * Тест для камеры, смотрящей вдоль оси X.
     */
    @Test
    void testLookAtAlongXAxis() {
        Vector3f eye = new Vector3f(5, 0, 0);
        Vector3f target = new Vector3f(0, 0, 0);
        Matrix4f viewMatrix = CameraView.lookAt(eye, target);
        
        Assertions.assertNotNull(viewMatrix);
        
        // Применяем к target
        Vector4f targetPoint = new Vector4f(target, 1.0f);
        Vector4f transformed = viewMatrix.multiply(targetPoint);
        
        // В пространстве камеры target должен быть на оси -Z
        Assertions.assertEquals(0.0f, transformed.x, EPSILON);
        Assertions.assertEquals(0.0f, transformed.y, EPSILON);
        Assertions.assertTrue(transformed.z < 0, "Target should be on negative Z axis");
    }
    
    /**
     * Тест для камеры, смотрящей вдоль оси Y.
     */
    @Test
    void testLookAtAlongYAxis() {
        Vector3f eye = new Vector3f(0, 5, 0);
        Vector3f target = new Vector3f(0, 0, 0);
        Matrix4f viewMatrix = CameraView.lookAt(eye, target);
        
        Assertions.assertNotNull(viewMatrix);
        
        Vector4f targetPoint = new Vector4f(target, 1.0f);
        Vector4f transformed = viewMatrix.multiply(targetPoint);
        
        // В пространстве камеры target должен быть на оси -Z
        Assertions.assertEquals(0.0f, transformed.x, EPSILON);
        Assertions.assertEquals(0.0f, transformed.y, EPSILON);
        Assertions.assertTrue(transformed.z < 0, "Target should be on negative Z axis");
    }
    
    /**
     * Тест для камеры с произвольными координатами.
     */
    @Test
    void testLookAtArbitraryPosition() {
        Vector3f eye = new Vector3f(10, 5, 3);
        Vector3f target = new Vector3f(1, 2, -1);
        Matrix4f viewMatrix = CameraView.lookAt(eye, target);
        
        Assertions.assertNotNull(viewMatrix);
        
        // Применяем матрицу к target
        Vector4f targetPoint = new Vector4f(target, 1.0f);
        Vector4f transformed = viewMatrix.multiply(targetPoint);
        
        // В пространстве камеры target должен быть на оси -Z (в направлении камеры)
        Assertions.assertEquals(0.0f, transformed.x, EPSILON);
        Assertions.assertEquals(0.0f, transformed.y, EPSILON);
        Assertions.assertTrue(transformed.z < 0, "Target should be on negative Z axis in camera space");
    }
    
    /**
     * Тест для камеры с коллинеарными forward и up векторами.
     * Должна обрабатываться корректно (возвращается единичная или альтернативная матрица).
     */
    @Test
    void testLookAtCollinearVectors() {
        Vector3f eye = new Vector3f(0, 0, 0);
        Vector3f target = new Vector3f(0, 1, 0); // Смотрим вдоль Y
        Vector3f up = new Vector3f(0, 2, 0); // up параллелен forward
        
        // Не должно выбрасывать исключение
        Matrix4f viewMatrix = CameraView.lookAt(eye, target, up);
        Assertions.assertNotNull(viewMatrix);
    }
    
    /**
     * Тест проверки преобразования позиции камеры.
     * После применения view матрицы, позиция камеры должна быть в начале координат камеры.
     */
    @Test
    void testLookAtInverseTransformation() {
        Vector3f eye = new Vector3f(3, 4, 5);
        Vector3f target = new Vector3f(0, 0, 0);
        Matrix4f viewMatrix = CameraView.lookAt(eye, target);
        
        // Применяем матрицу к eye позиции
        Vector4f eyePoint = new Vector4f(eye, 1.0f);
        Vector4f transformed = viewMatrix.multiply(eyePoint);
        
        // Eye позиция в пространстве камеры должна быть в (0, 0, 0) - начало координат камеры
        Assertions.assertEquals(0.0f, transformed.x, EPSILON);
        Assertions.assertEquals(0.0f, transformed.y, EPSILON);
        Assertions.assertEquals(0.0f, transformed.z, EPSILON,
            "Camera eye position in camera space should be at origin (0, 0, 0)");
    }
    
    /**
     * Тест для случая с близкими координатами (очень маленькое расстояние).
     */
    @Test
    void testLookAtVeryClose() {
        Vector3f eye = new Vector3f(0.0001f, 0.0001f, 0.0001f);
        Vector3f target = new Vector3f(0, 0, 0);
        
        // Не должно выбрасывать исключение
        Matrix4f viewMatrix = CameraView.lookAt(eye, target);
        Assertions.assertNotNull(viewMatrix);
    }
    
    /**
     * Тест проверки, что матрица вида является ортогональной матрицей (для поворота).
     * Детерминант должен быть 1 или -1.
     */
    @Test
    void testLookAtMatrixProperties() {
        Vector3f eye = new Vector3f(5, 3, 2);
        Vector3f target = new Vector3f(0, 0, 0);
        Matrix4f viewMatrix = CameraView.lookAt(eye, target);
        
        // Проверяем, что матрица не null
        Assertions.assertNotNull(viewMatrix);
        
        // Детерминант должен быть близок к 1 или -1 (для ортогональных матриц)
        float det = viewMatrix.determinant();
        Assertions.assertTrue(Math.abs(Math.abs(det) - 1.0f) < 0.1f,
            String.format("Determinant should be close to 1 or -1, got: %.6f", det));
    }
}