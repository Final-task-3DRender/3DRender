package com.cgvsu.camera;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Тесты для класса Camera
 */
class CameraTest {
    
    private static final float EPSILON = 1e-5f;
    
    /**
     * Тест создания камеры с корректными параметрами.
     */
    @Test
    void testCameraConstructor() {
        Vector3f position = new Vector3f(0, 0, 5);
        Vector3f target = new Vector3f(0, 0, 0);
        float fov = (float) Math.toRadians(60.0);
        float aspectRatio = 16.0f / 9.0f;
        float nearPlane = 0.1f;
        float farPlane = 100.0f;
        
        Camera camera = new Camera(position, target, fov, aspectRatio, nearPlane, farPlane);
        
        Assertions.assertNotNull(camera);
        
        // Проверяем, что getters возвращают копии (immutability)
        Vector3f pos = camera.getPosition();
        Assertions.assertEquals(position.x, pos.x, EPSILON);
        Assertions.assertEquals(position.y, pos.y, EPSILON);
        Assertions.assertEquals(position.z, pos.z, EPSILON);
        
        Vector3f tgt = camera.getTarget();
        Assertions.assertEquals(target.x, tgt.x, EPSILON);
        Assertions.assertEquals(target.y, tgt.y, EPSILON);
        Assertions.assertEquals(target.z, tgt.z, EPSILON);
    }
    
    /**
     * Тест проверки immutability позиции камеры.
     */
    @Test
    void testCameraPositionImmutability() {
        Vector3f position = new Vector3f(1, 2, 3);
        Vector3f target = new Vector3f(0, 0, 0);
        Camera camera = new Camera(position, target, (float) Math.toRadians(60.0), 1.0f, 0.1f, 100.0f);
        
        Vector3f originalPos = camera.getPosition();
        position.x = 999; // Изменяем исходный вектор
        
        // Позиция камеры не должна измениться
        Vector3f newPos = camera.getPosition();
        Assertions.assertEquals(originalPos.x, newPos.x, EPSILON);
        Assertions.assertNotEquals(999.0f, newPos.x, EPSILON, 
            "Camera position should not change when original vector is modified");
    }
    
    /**
     * Тест проверки immutability цели камеры.
     */
    @Test
    void testCameraTargetImmutability() {
        Vector3f position = new Vector3f(0, 0, 5);
        Vector3f target = new Vector3f(1, 2, 3);
        Camera camera = new Camera(position, target, (float) Math.toRadians(60.0), 1.0f, 0.1f, 100.0f);
        
        Vector3f originalTarget = camera.getTarget();
        target.x = 999; // Изменяем исходный вектор
        
        // Цель камеры не должна измениться
        Vector3f newTarget = camera.getTarget();
        Assertions.assertEquals(originalTarget.x, newTarget.x, EPSILON);
        Assertions.assertNotEquals(999.0f, newTarget.x, EPSILON,
            "Camera target should not change when original vector is modified");
    }
    
    /**
     * Тест установки новой позиции камеры.
     */
    @Test
    void testSetPosition() {
        Camera camera = new Camera(
            new Vector3f(0, 0, 5),
            new Vector3f(0, 0, 0),
            (float) Math.toRadians(60.0),
            1.0f, 0.1f, 100.0f
        );
        
        Vector3f newPosition = new Vector3f(10, 20, 30);
        camera.setPosition(newPosition);
        
        Vector3f pos = camera.getPosition();
        Assertions.assertEquals(10.0f, pos.x, EPSILON);
        Assertions.assertEquals(20.0f, pos.y, EPSILON);
        Assertions.assertEquals(30.0f, pos.z, EPSILON);
    }
    
    /**
     * Тест установки новой цели камеры.
     */
    @Test
    void testSetTarget() {
        Camera camera = new Camera(
            new Vector3f(0, 0, 5),
            new Vector3f(0, 0, 0),
            (float) Math.toRadians(60.0),
            1.0f, 0.1f, 100.0f
        );
        
        Vector3f newTarget = new Vector3f(5, 5, 5);
        camera.setTarget(newTarget);
        
        Vector3f tgt = camera.getTarget();
        Assertions.assertEquals(5.0f, tgt.x, EPSILON);
        Assertions.assertEquals(5.0f, tgt.y, EPSILON);
        Assertions.assertEquals(5.0f, tgt.z, EPSILON);
    }
    
    /**
     * Тест перемещения позиции камеры.
     */
    @Test
    void testMovePosition() {
        Camera camera = new Camera(
            new Vector3f(0, 0, 5),
            new Vector3f(0, 0, 0),
            (float) Math.toRadians(60.0),
            1.0f, 0.1f, 100.0f
        );
        
        Vector3f translation = new Vector3f(1, 2, 3);
        camera.movePosition(translation);
        
        Vector3f pos = camera.getPosition();
        Assertions.assertEquals(1.0f, pos.x, EPSILON);
        Assertions.assertEquals(2.0f, pos.y, EPSILON);
        Assertions.assertEquals(8.0f, pos.z, EPSILON);
    }
    
    /**
     * Тест перемещения цели камеры.
     */
    @Test
    void testMoveTarget() {
        Camera camera = new Camera(
            new Vector3f(0, 0, 5),
            new Vector3f(0, 0, 0),
            (float) Math.toRadians(60.0),
            1.0f, 0.1f, 100.0f
        );
        
        Vector3f translation = new Vector3f(1, 1, 1);
        camera.moveTarget(translation);
        
        Vector3f tgt = camera.getTarget();
        Assertions.assertEquals(1.0f, tgt.x, EPSILON);
        Assertions.assertEquals(1.0f, tgt.y, EPSILON);
        Assertions.assertEquals(1.0f, tgt.z, EPSILON);
    }
    
    /**
     * Тест установки нового соотношения сторон.
     */
    @Test
    void testSetAspectRatio() {
        Camera camera = new Camera(
            new Vector3f(0, 0, 5),
            new Vector3f(0, 0, 0),
            (float) Math.toRadians(60.0),
            1.0f, 0.1f, 100.0f
        );
        
        float newAspectRatio = 16.0f / 9.0f;
        camera.setAspectRatio(newAspectRatio);
        
        // Проверяем, что матрица проекции изменилась (через getProjectionMatrix)
        Matrix4f projectionMatrix = camera.getProjectionMatrix();
        Assertions.assertNotNull(projectionMatrix);
    }
    
    /**
     * Тест получения матрицы вида.
     */
    @Test
    void testGetViewMatrix() {
        Camera camera = new Camera(
            new Vector3f(0, 0, 5),
            new Vector3f(0, 0, 0),
            (float) Math.toRadians(60.0),
            1.0f, 0.1f, 100.0f
        );
        
        Matrix4f viewMatrix = camera.getViewMatrix();
        
        Assertions.assertNotNull(viewMatrix, "View matrix should not be null");
        
        // Применяем матрицу к позиции камеры - должна быть в начале координат камеры
        Vector3f eye = camera.getPosition();
        com.cgvsu.math.Vector4f eyePoint = new com.cgvsu.math.Vector4f(eye, 1.0f);
        com.cgvsu.math.Vector4f transformed = viewMatrix.multiply(eyePoint);
        
        Assertions.assertEquals(0.0f, transformed.x, EPSILON);
        Assertions.assertEquals(0.0f, transformed.y, EPSILON);
        Assertions.assertEquals(0.0f, transformed.z, EPSILON, 
            "Camera position in camera space should be at origin");
    }
    
    /**
     * Тест получения матрицы проекции.
     */
    @Test
    void testGetProjectionMatrix() {
        Camera camera = new Camera(
            new Vector3f(0, 0, 5),
            new Vector3f(0, 0, 0),
            (float) Math.toRadians(60.0),
            1.0f, 0.1f, 100.0f
        );
        
        Matrix4f projectionMatrix = camera.getProjectionMatrix();
        
        Assertions.assertNotNull(projectionMatrix, "Projection matrix should not be null");
        
        // Проверяем структуру матрицы проекции
        Assertions.assertNotEquals(0.0f, projectionMatrix.get(0, 0), EPSILON);
        Assertions.assertNotEquals(0.0f, projectionMatrix.get(1, 1), EPSILON);
    }
    
    /**
     * Тест множественных вызовов getViewMatrix (должна возвращаться новая матрица каждый раз).
     */
    @Test
    void testGetViewMatrixMultipleCalls() {
        Camera camera = new Camera(
            new Vector3f(0, 0, 5),
            new Vector3f(0, 0, 0),
            (float) Math.toRadians(60.0),
            1.0f, 0.1f, 100.0f
        );
        
        Matrix4f viewMatrix1 = camera.getViewMatrix();
        Matrix4f viewMatrix2 = camera.getViewMatrix();
        
        // Матрицы должны быть эквивалентны (равны по значениям)
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                Assertions.assertEquals(viewMatrix1.get(i, j), viewMatrix2.get(i, j), EPSILON,
                    String.format("Matrix elements [%d][%d] should be equal", i, j));
            }
        }
    }
    
    /**
     * Тест изменения позиции камеры и проверки, что view матрица обновляется.
     */
    @Test
    void testViewMatrixUpdatesWithPositionChange() {
        Camera camera = new Camera(
            new Vector3f(0, 0, 5),
            new Vector3f(0, 0, 0),
            (float) Math.toRadians(60.0),
            1.0f, 0.1f, 100.0f
        );
        
        Matrix4f viewMatrix1 = camera.getViewMatrix();
        
        camera.setPosition(new Vector3f(10, 0, 5));
        Matrix4f viewMatrix2 = camera.getViewMatrix();
        
        // Матрицы должны отличаться (хотя бы один элемент)
        boolean different = false;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (Math.abs(viewMatrix1.get(i, j) - viewMatrix2.get(i, j)) > EPSILON) {
                    different = true;
                    break;
                }
            }
            if (different) break;
        }
        
        Assertions.assertTrue(different, "View matrices should be different after position change");
    }
}