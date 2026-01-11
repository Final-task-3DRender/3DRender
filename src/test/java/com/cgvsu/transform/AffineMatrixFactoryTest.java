package com.cgvsu.transform;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import org.junit.jupiter.api.Test;

import static com.cgvsu.transform.AffineMatrixFactory.*;
import static org.junit.jupiter.api.Assertions.*;

class AffineMatrixFactoryTest {
    private static final float EPSILON = 1e-6f;

    /**
     * Вспомогательный метод для применения матрицы к вектору и извлечения результата
     */
    private Vector3f applyMatrix(Matrix4f matrix, Vector3f point) {
        Vector4f result = matrix.multiply(point);
        // Нормализация однородных координат
        if (Math.abs(result.w) > EPSILON && result.w != 1.0f) {
            return new Vector3f(result.x / result.w, result.y / result.w, result.z / result.w);
        }
        return new Vector3f(result.x, result.y, result.z);
    }

    @Test
    void testScaleX() {
        Vector3f point = new Vector3f(2, 3, 4);
        Matrix4f scaleMatrix = createScaleMatrix(new Vector3f(5, 1, 1));
        Vector3f result = applyMatrix(scaleMatrix, point);
        assertEquals(10, result.x, EPSILON);
        assertEquals(3, result.y, EPSILON);
        assertEquals(4, result.z, EPSILON);
    }

    @Test
    void testScaleY() {
        Vector3f point = new Vector3f(2, 3, 4);
        Matrix4f scaleMatrix = createScaleMatrix(new Vector3f(1, 5, 1));
        Vector3f result = applyMatrix(scaleMatrix, point);
        assertEquals(2, result.x, EPSILON);
        assertEquals(15, result.y, EPSILON);
        assertEquals(4, result.z, EPSILON);
    }

    @Test
    void testScaleZ() {
        Vector3f point = new Vector3f(2, 3, 4);
        Matrix4f scaleMatrix = createScaleMatrix(new Vector3f(1, 1, 5));
        Vector3f result = applyMatrix(scaleMatrix, point);
        assertEquals(2, result.x, EPSILON);
        assertEquals(3, result.y, EPSILON);
        assertEquals(20, result.z, EPSILON);
    }

    @Test
    void testScaleUniform() {
        Vector3f point = new Vector3f(1, 2, 3);
        Matrix4f scaleMatrix = createScaleMatrix(new Vector3f(5, 5, 5));
        Vector3f result = applyMatrix(scaleMatrix, point);
        assertEquals(5, result.x, EPSILON);
        assertEquals(10, result.y, EPSILON);
        assertEquals(15, result.z, EPSILON);
    }

    @Test
    void testScaleZero() {
        Vector3f point = new Vector3f(1, 1, 1);
        Matrix4f scaleMatrix = createScaleMatrix(new Vector3f(0, 0, 0));
        Vector3f result = applyMatrix(scaleMatrix, point);
        assertEquals(0, result.x, EPSILON);
        assertEquals(0, result.y, EPSILON);
        assertEquals(0, result.z, EPSILON);
    }

    @Test
    void testRotateOnX() {
        Vector3f point = new Vector3f(0, 1, 0);
        Matrix4f rotationMatrix = createRotationXMatrix((float) Math.PI / 2);
        Vector3f result = applyMatrix(rotationMatrix, point);
        assertEquals(0, result.x, EPSILON);
        assertEquals(0, result.y, EPSILON);
        assertEquals(1, result.z, EPSILON);
    }

    @Test
    void testRotateOnY() {
        // В левосторонней системе координат (JavaFX): при повороте на 90° вокруг Y
        // точка (0, 0, 1) переходит в (1, 0, 0)
        Vector3f point = new Vector3f(0, 0, 1);
        Matrix4f rotationMatrix = createRotationYMatrix((float) Math.PI / 2);
        Vector3f result = applyMatrix(rotationMatrix, point);
        assertEquals(1, result.x, EPSILON);
        assertEquals(0, result.y, EPSILON);
        assertEquals(0, result.z, EPSILON);
    }

    @Test
    void testRotateOnZ() {
        Vector3f point = new Vector3f(1, 0, 0);
        Matrix4f rotationMatrix = createRotationZMatrix((float) Math.PI / 2);
        Vector3f result = applyMatrix(rotationMatrix, point);
        assertEquals(0, result.x, EPSILON);
        assertEquals(1, result.y, EPSILON);
        assertEquals(0, result.z, EPSILON);
    }

    @Test
    void testVerySmallAngles() {
        Vector3f point = new Vector3f(1, 0, 0);
        Matrix4f rotX = createRotationXMatrix(0.001f);
        Matrix4f rotY = createRotationYMatrix(0.001f);
        Matrix4f rotZ = createRotationZMatrix(0.001f);
        Matrix4f combined = rotX.multiply(rotY).multiply(rotZ);
        Vector3f result = applyMatrix(combined, point);

        assertTrue(Math.abs(result.x - 1) < 0.01);
        assertTrue(Math.abs(result.y) < 0.01);
        assertTrue(Math.abs(result.z) < 0.01);
    }

    @Test
    void testLargeAngles() {
        Vector3f point = new Vector3f(1, 0, 0);
        Matrix4f rotX = createRotationXMatrix((float) (Math.PI * 3));
        Matrix4f rotY = createRotationYMatrix((float) (Math.PI * 2));
        Matrix4f combined = rotX.multiply(rotY);
        Vector3f result = applyMatrix(combined, point);

        assertFalse(Float.isNaN(result.x));
        assertFalse(Float.isNaN(result.y));
        assertFalse(Float.isNaN(result.z));
    }

    @Test
    void testScaleThenTranslate() {
        Vector3f point = new Vector3f(1, 1, 1);
        Matrix4f scaleMatrix = createScaleMatrix(new Vector3f(2, 2, 2));
        Matrix4f translateMatrix = createTranslationMatrix(new Vector3f(10, 10, 10));
        // Порядок: сначала scale, потом translate = T * S
        Matrix4f transformation = translateMatrix.multiply(scaleMatrix);
        Vector3f result = applyMatrix(transformation, point);

        assertEquals(12, result.x, EPSILON);
        assertEquals(12, result.y, EPSILON);
        assertEquals(12, result.z, EPSILON);
    }

    @Test
    void testTranslateThenScale() {
        Vector3f point = new Vector3f(1, 1, 1);
        Matrix4f translateMatrix = createTranslationMatrix(new Vector3f(10, 10, 10));
        Matrix4f scaleMatrix = createScaleMatrix(new Vector3f(2, 2, 2));
        // Порядок: сначала translate, потом scale = S * T
        Matrix4f transformation = scaleMatrix.multiply(translateMatrix);
        Vector3f result = applyMatrix(transformation, point);

        assertEquals(22, result.x, EPSILON);
        assertEquals(22, result.y, EPSILON);
        assertEquals(22, result.z, EPSILON);
    }

    @Test
    void testComplexTransformation() {
        Vector3f point = new Vector3f(1, 2, 3);

        Matrix4f translate1 = createTranslationMatrix(new Vector3f(5, 10, 15));
        Matrix4f rotX = createRotationXMatrix((float) (Math.PI / 4));
        Matrix4f rotY = createRotationYMatrix((float) (Math.PI / 3));
        Matrix4f scale = createScaleMatrix(new Vector3f(2, 0.5f, 3));
        Matrix4f translate2 = createTranslationMatrix(new Vector3f(-1, -2, -3));

        // Комбинируем: T2 * RY * RX * S * T1
        Matrix4f transformation = translate2
                .multiply(rotY)
                .multiply(rotX)
                .multiply(scale)
                .multiply(translate1);

        Vector3f result = applyMatrix(transformation, point);

        assertFalse(Float.isNaN(result.x));
        assertFalse(Float.isNaN(result.y));
        assertFalse(Float.isNaN(result.z));
    }

    @Test
    void testMultipleScaleOperations() {
        Vector3f point = new Vector3f(2, 3, 4);

        Matrix4f scaleX = createScaleMatrix(new Vector3f(2, 1, 1));
        Matrix4f scaleY = createScaleMatrix(new Vector3f(1, 3, 1));
        Matrix4f scaleZ = createScaleMatrix(new Vector3f(1, 1, 4));
        Matrix4f scaleUniform = createScaleMatrix(new Vector3f(0.5f, 0.5f, 0.5f));

        Matrix4f transformation = scaleX.multiply(scaleY).multiply(scaleZ).multiply(scaleUniform);
        Vector3f result = applyMatrix(transformation, point);

        assertEquals(2, result.x, EPSILON);  // 2 * 2 * 0.5 = 2
        assertEquals(4.5, result.y, EPSILON); // 3 * 3 * 0.5 = 4.5
        assertEquals(8, result.z, EPSILON);   // 4 * 4 * 0.5 = 8
    }

    @Test
    void testRotationCombination() {
        Vector3f point = new Vector3f(2, 3, 4);

        Matrix4f rotX = createRotationXMatrix((float) (Math.PI / 3));
        Matrix4f rotY = createRotationYMatrix((float) (Math.PI / 4));
        Matrix4f rotZ = createRotationZMatrix((float) (Math.PI / 6));

        Matrix4f combined = rotX.multiply(rotY).multiply(rotZ);
        Vector3f result = applyMatrix(combined, point);

        assertFalse(Float.isNaN(result.x));
        assertFalse(Float.isNaN(result.y));
        assertFalse(Float.isNaN(result.z));
        // Проверяем, что точка не стала нулевой
        assertTrue(Math.abs(result.x) > EPSILON || Math.abs(result.y) > EPSILON || Math.abs(result.z) > EPSILON);
    }

    @Test
    void testIdentity() {
        Vector3f point = new Vector3f(1, 2, 3);
        Matrix4f identity = Matrix4f.identity();
        Vector3f result = applyMatrix(identity, point);

        assertEquals(1, result.x, EPSILON);
        assertEquals(2, result.y, EPSILON);
        assertEquals(3, result.z, EPSILON);
    }

    @Test
    void testTranslation() {
        Vector3f point = new Vector3f(1, 2, 3);
        Matrix4f translateMatrix = createTranslationMatrix(new Vector3f(5, 10, 15));
        Vector3f result = applyMatrix(translateMatrix, point);

        assertEquals(6, result.x, EPSILON);
        assertEquals(12, result.y, EPSILON);
        assertEquals(18, result.z, EPSILON);
    }

    @Test
    void testRotationDegreesConversion() {
        // Тест для createRotationMatrix с градусами
        Vector3f point = new Vector3f(0, 1, 0);
        Vector3f rotationDegrees = new Vector3f(90, 0, 0); // 90 градусов вокруг X
        Matrix4f rotationMatrix = createRotationMatrix(rotationDegrees);
        Vector3f result = applyMatrix(rotationMatrix, point);

        // 90 градусов вокруг X: (0, 1, 0) -> (0, 0, 1)
        assertEquals(0, result.x, EPSILON);
        assertEquals(0, result.y, EPSILON);
        assertEquals(1, result.z, EPSILON);
    }

    @Test
    void testNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> {
            createTranslationMatrix(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            createRotationMatrix(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            createScaleMatrix(null);
        });
    }

    @Test
    void testZeroRotation() {
        Vector3f point = new Vector3f(1, 2, 3);
        Matrix4f rotX = createRotationXMatrix(0);
        Matrix4f rotY = createRotationYMatrix(0);
        Matrix4f rotZ = createRotationZMatrix(0);
        Vector3f resultX = applyMatrix(rotX, point);
        Vector3f resultY = applyMatrix(rotY, point);
        Vector3f resultZ = applyMatrix(rotZ, point);

        assertEquals(point.x, resultX.x, EPSILON);
        assertEquals(point.y, resultX.y, EPSILON);
        assertEquals(point.z, resultX.z, EPSILON);

        assertEquals(point.x, resultY.x, EPSILON);
        assertEquals(point.y, resultY.y, EPSILON);
        assertEquals(point.z, resultY.z, EPSILON);

        assertEquals(point.x, resultZ.x, EPSILON);
        assertEquals(point.y, resultZ.y, EPSILON);
        assertEquals(point.z, resultZ.z, EPSILON);
    }

    @Test
    void testRotation90DegreesX() {
        // Поворот на 90 градусов вокруг X: Y -> Z, Z -> -Y
        Vector3f point = new Vector3f(0, 1, 0);
        Matrix4f rotX = createRotationXMatrix((float) (Math.PI / 2));
        Vector3f result = applyMatrix(rotX, point);

        assertEquals(0, result.x, EPSILON);
        assertEquals(0, result.y, EPSILON);
        assertEquals(1, result.z, EPSILON);
    }

    @Test
    void testRotation90DegreesY() {
        // В левосторонней системе координат (JavaFX): поворот на 90° вокруг Y
        // Точка (0, 0, 1) переходит в (1, 0, 0)
        Vector3f point = new Vector3f(0, 0, 1);
        Matrix4f rotY = createRotationYMatrix((float) (Math.PI / 2));
        Vector3f result = applyMatrix(rotY, point);

        assertEquals(1, result.x, EPSILON);
        assertEquals(0, result.y, EPSILON);
        assertEquals(0, result.z, EPSILON);
    }

    @Test
    void testRotation90DegreesZ() {
        // Поворот на 90 градусов вокруг Z: X -> Y, Y -> -X
        Vector3f point = new Vector3f(1, 0, 0);
        Matrix4f rotZ = createRotationZMatrix((float) (Math.PI / 2));
        Vector3f result = applyMatrix(rotZ, point);

        assertEquals(0, result.x, EPSILON);
        assertEquals(1, result.y, EPSILON);
        assertEquals(0, result.z, EPSILON);
    }

    @Test
    void testRotation180Degrees() {
        // Поворот на 180 градусов должен инвертировать координаты
        Vector3f point = new Vector3f(1, 2, 3);
        Matrix4f rotX = createRotationXMatrix((float) Math.PI);
        Matrix4f rotY = createRotationYMatrix((float) Math.PI);
        Matrix4f rotZ = createRotationZMatrix((float) Math.PI);

        Vector3f resultX = applyMatrix(rotX, point);
        Vector3f resultY = applyMatrix(rotY, point);
        Vector3f resultZ = applyMatrix(rotZ, point);

        // При повороте на 180° вокруг X: (x, y, z) -> (x, -y, -z)
        assertEquals(1, resultX.x, EPSILON);
        assertEquals(-2, resultX.y, EPSILON);
        assertEquals(-3, resultX.z, EPSILON);

        // При повороте на 180° вокруг Y: (x, y, z) -> (-x, y, -z)
        assertEquals(-1, resultY.x, EPSILON);
        assertEquals(2, resultY.y, EPSILON);
        assertEquals(-3, resultY.z, EPSILON);

        // При повороте на 180° вокруг Z: (x, y, z) -> (-x, -y, z)
        assertEquals(-1, resultZ.x, EPSILON);
        assertEquals(-2, resultZ.y, EPSILON);
        assertEquals(3, resultZ.z, EPSILON);
    }
}
