package com.cgvsu.camera;

import com.cgvsu.math.Matrix4f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Тесты для класса CameraProjection
 */
class CameraProjectionTest {
    
    private static final float EPSILON = 1e-5f;
    
    /**
     * Тест создания матрицы проекции с корректными параметрами.
     */
    @Test
    void testPerspectiveValidParameters() {
        float fov = (float) Math.toRadians(60.0); // 60 градусов в радианах
        float aspectRatio = 16.0f / 9.0f; // 16:9
        float nearPlane = 0.1f;
        float farPlane = 100.0f;
        
        Matrix4f projectionMatrix = CameraProjection.perspective(fov, aspectRatio, nearPlane, farPlane);
        
        Assertions.assertNotNull(projectionMatrix);
        
        // Проверяем структуру матрицы проекции (некоторые элементы должны быть ненулевыми)
        // Элемент [0][0] должен быть положительным (зависит от aspectRatio)
        Assertions.assertTrue(projectionMatrix.get(0, 0) > 0, 
            "Element [0][0] should be positive");
        
        // Элемент [1][1] должен быть положительным
        Assertions.assertTrue(projectionMatrix.get(1, 1) > 0,
            "Element [1][1] should be positive");
        
        // Элемент [2][2] должен быть не нулевым (для перспективной проекции)
        Assertions.assertNotEquals(0.0f, projectionMatrix.get(2, 2), EPSILON,
            "Element [2][2] should not be zero");
    }
    
    /**
     * Тест автоматического преобразования FOV из градусов в радианы.
     * Если fov > 10, считается что это градусы.
     */
    @Test
    void testPerspectiveFovInDegrees() {
        float fovDegrees = 60.0f; // > 10, будет преобразовано в радианы
        float aspectRatio = 1.0f;
        float nearPlane = 0.1f;
        float farPlane = 100.0f;
        
        // Не должно выбрасывать исключение
        Matrix4f projectionMatrix = CameraProjection.perspective(fovDegrees, aspectRatio, nearPlane, farPlane);
        Assertions.assertNotNull(projectionMatrix);
    }
    
    /**
     * Тест с FOV в радианах (< 10).
     */
    @Test
    void testPerspectiveFovInRadians() {
        float fovRadians = (float) Math.toRadians(45.0); // < 10 радиан
        float aspectRatio = 1.0f;
        float nearPlane = 0.1f;
        float farPlane = 100.0f;
        
        Matrix4f projectionMatrix = CameraProjection.perspective(fovRadians, aspectRatio, nearPlane, farPlane);
        Assertions.assertNotNull(projectionMatrix);
    }
    
    /**
     * Тест обработки некорректного aspectRatio (равен 0).
     * Должно выбрасывать IllegalArgumentException.
     */
    @Test
    void testPerspectiveInvalidAspectRatio() {
        float fov = (float) Math.toRadians(60.0);
        float aspectRatio = 0.0f; // Некорректный
        float nearPlane = 0.1f;
        float farPlane = 100.0f;
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            CameraProjection.perspective(fov, aspectRatio, nearPlane, farPlane);
        }, "Should throw IllegalArgumentException for aspectRatio = 0");
    }
    
    /**
     * Тест обработки случая, когда near == far.
     * Должно выбрасывать IllegalArgumentException.
     */
    @Test
    void testPerspectiveNearEqualsFar() {
        float fov = (float) Math.toRadians(60.0);
        float aspectRatio = 1.0f;
        float nearPlane = 10.0f;
        float farPlane = 10.0f; // Равно near
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            CameraProjection.perspective(fov, aspectRatio, nearPlane, farPlane);
        }, "Should throw IllegalArgumentException when near == far");
    }
    
    /**
     * Тест обработки очень маленького FOV (близко к 0).
     */
    @Test
    void testPerspectiveVerySmallFov() {
        float fov = 0.001f; // Очень маленький FOV
        float aspectRatio = 1.0f;
        float nearPlane = 0.1f;
        float farPlane = 100.0f;
        
        // Не должно выбрасывать исключение
        Matrix4f projectionMatrix = CameraProjection.perspective(fov, aspectRatio, nearPlane, farPlane);
        Assertions.assertNotNull(projectionMatrix);
    }
    
    /**
     * Тест с разными соотношениями сторон.
     */
    @Test
    void testPerspectiveDifferentAspectRatios() {
        float fov = (float) Math.toRadians(60.0);
        float nearPlane = 0.1f;
        float farPlane = 100.0f;
        
        // Тестируем разные aspect ratios
        float[] aspectRatios = {1.0f, 16.0f/9.0f, 4.0f/3.0f, 21.0f/9.0f};
        
        for (float aspectRatio : aspectRatios) {
            Matrix4f projectionMatrix = CameraProjection.perspective(fov, aspectRatio, nearPlane, farPlane);
            Assertions.assertNotNull(projectionMatrix, 
                String.format("Projection matrix should not be null for aspectRatio = %.2f", aspectRatio));
        }
    }
    
    /**
     * Тест проверки структуры матрицы проекции.
     * Некоторые элементы должны быть нулевыми, некоторые - нет.
     */
    @Test
    void testPerspectiveMatrixStructure() {
        float fov = (float) Math.toRadians(60.0);
        float aspectRatio = 1.0f;
        float nearPlane = 0.1f;
        float farPlane = 100.0f;
        
        Matrix4f projectionMatrix = CameraProjection.perspective(fov, aspectRatio, nearPlane, farPlane);
        
        // Элемент [0][0] не должен быть нулевым
        Assertions.assertNotEquals(0.0f, projectionMatrix.get(0, 0), EPSILON);
        
        // Элемент [1][1] не должен быть нулевым
        Assertions.assertNotEquals(0.0f, projectionMatrix.get(1, 1), EPSILON);
        
        // Элемент [2][3] должен быть 1.0 (для перспективной проекции)
        Assertions.assertEquals(1.0f, projectionMatrix.get(2, 3), EPSILON);
        
        // Элемент [3][2] не должен быть нулевым (для перспективного деления)
        Assertions.assertNotEquals(0.0f, projectionMatrix.get(3, 2), EPSILON);
    }
    
    /**
     * Тест с очень большим farPlane.
     */
    @Test
    void testPerspectiveLargeFarPlane() {
        float fov = (float) Math.toRadians(60.0);
        float aspectRatio = 1.0f;
        float nearPlane = 0.1f;
        float farPlane = 10000.0f; // Очень большое значение
        
        Matrix4f projectionMatrix = CameraProjection.perspective(fov, aspectRatio, nearPlane, farPlane);
        Assertions.assertNotNull(projectionMatrix);
    }
    
    /**
     * Тест с очень маленьким nearPlane.
     */
    @Test
    void testPerspectiveVerySmallNearPlane() {
        float fov = (float) Math.toRadians(60.0);
        float aspectRatio = 1.0f;
        float nearPlane = 0.001f; // Очень маленькое значение
        float farPlane = 100.0f;
        
        Matrix4f projectionMatrix = CameraProjection.perspective(fov, aspectRatio, nearPlane, farPlane);
        Assertions.assertNotNull(projectionMatrix);
    }
    
    /**
     * Тест проверки, что элементы матрицы имеют правильные знаки.
     */
    @Test
    void testPerspectiveMatrixSigns() {
        float fov = (float) Math.toRadians(60.0);
        float aspectRatio = 1.0f;
        float nearPlane = 0.1f;
        float farPlane = 100.0f;
        
        Matrix4f projectionMatrix = CameraProjection.perspective(fov, aspectRatio, nearPlane, farPlane);
        
        // [0][0] и [1][1] должны быть положительными
        Assertions.assertTrue(projectionMatrix.get(0, 0) > 0);
        Assertions.assertTrue(projectionMatrix.get(1, 1) > 0);
        
        // [3][2] должен быть отрицательным (для правильного перспективного деления в левосторонней системе)
        Assertions.assertTrue(projectionMatrix.get(3, 2) < 0);
    }
}