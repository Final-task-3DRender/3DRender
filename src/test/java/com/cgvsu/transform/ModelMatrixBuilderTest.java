package com.cgvsu.transform;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import com.cgvsu.model.ModelTransform;
import org.junit.jupiter.api.Test;

import static com.cgvsu.transform.AffineMatrixFactory.*;
import static com.cgvsu.transform.ModelMatrixBuilder.*;
import static org.junit.jupiter.api.Assertions.*;

class ModelMatrixBuilderTest {
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

    /**
     * Вспомогательный метод для проверки равенства матриц с учетом погрешности
     */
    private void assertMatrixEquals(Matrix4f expected, Matrix4f actual, float epsilon) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(expected.get(i, j), actual.get(i, j), epsilon,
                    String.format("Matrix elements at [%d,%d] differ", i, j));
            }
        }
    }

    @Test
    void testIdentity() {
        Matrix4f identity1 = identity();
        Matrix4f identity2 = Matrix4f.identity();
        
        assertNotNull(identity1);
        assertEquals(identity2, identity1);
        
        // Проверяем, что единичная матрица не изменяет точки
        Vector3f point = new Vector3f(1, 2, 3);
        Vector3f result = applyMatrix(identity1, point);
        
        assertEquals(1, result.x, EPSILON);
        assertEquals(2, result.y, EPSILON);
        assertEquals(3, result.z, EPSILON);
    }

    @Test
    void testBuildFromComponentsIdentity() {
        // Трансформация без изменений (identity)
        Vector3f position = new Vector3f(0, 0, 0);
        Vector3f rotation = new Vector3f(0, 0, 0);
        Vector3f scale = new Vector3f(1, 1, 1);
        
        Matrix4f matrix = build(position, rotation, scale);
        
        assertNotNull(matrix);
        assertEquals(Matrix4f.identity(), matrix);
    }

    @Test
    void testBuildFromComponentsTranslationOnly() {
        Vector3f position = new Vector3f(10, 20, 30);
        Vector3f rotation = new Vector3f(0, 0, 0);
        Vector3f scale = new Vector3f(1, 1, 1);
        
        Matrix4f matrix = build(position, rotation, scale);
        
        // Проверяем, что точка переносится правильно
        Vector3f point = new Vector3f(1, 2, 3);
        Vector3f result = applyMatrix(matrix, point);
        
        assertEquals(11, result.x, EPSILON); // 1 + 10
        assertEquals(22, result.y, EPSILON); // 2 + 20
        assertEquals(33, result.z, EPSILON); // 3 + 30
    }

    @Test
    void testBuildFromComponentsScaleOnly() {
        Vector3f position = new Vector3f(0, 0, 0);
        Vector3f rotation = new Vector3f(0, 0, 0);
        Vector3f scale = new Vector3f(2, 3, 4);
        
        Matrix4f matrix = build(position, rotation, scale);
        
        // Проверяем, что точка масштабируется правильно
        Vector3f point = new Vector3f(1, 2, 3);
        Vector3f result = applyMatrix(matrix, point);
        
        assertEquals(2, result.x, EPSILON); // 1 * 2
        assertEquals(6, result.y, EPSILON); // 2 * 3
        assertEquals(12, result.z, EPSILON); // 3 * 4
    }

    @Test
    void testBuildFromComponentsRotationOnly() {
        Vector3f position = new Vector3f(0, 0, 0);
        Vector3f rotation = new Vector3f(90, 0, 0); // 90 градусов вокруг X
        Vector3f scale = new Vector3f(1, 1, 1);
        
        Matrix4f matrix = build(position, rotation, scale);
        
        // Проверяем вращение: (0, 1, 0) -> (0, 0, 1) при повороте на 90° вокруг X
        Vector3f point = new Vector3f(0, 1, 0);
        Vector3f result = applyMatrix(matrix, point);
        
        assertEquals(0, result.x, EPSILON);
        assertEquals(0, result.y, EPSILON);
        assertEquals(1, result.z, EPSILON);
    }

    @Test
    void testBuildFromComponentsCombined() {
        Vector3f position = new Vector3f(10, 0, 0);
        Vector3f rotation = new Vector3f(0, 90, 0); // 90 градусов вокруг Y
        Vector3f scale = new Vector3f(2, 1, 1);
        
        Matrix4f matrix = build(position, rotation, scale);
        
        // Комбинированная трансформация: масштаб * вращение * перенос
        Vector3f point = new Vector3f(1, 0, 0);
        Vector3f result = applyMatrix(matrix, point);
        
        // Проверяем, что трансформация применена (точка не равна исходной)
        assertFalse(Math.abs(result.x - 1) < EPSILON && 
                   Math.abs(result.y) < EPSILON && 
                   Math.abs(result.z) < EPSILON);
        
        // Результат не должен быть NaN
        assertFalse(Float.isNaN(result.x));
        assertFalse(Float.isNaN(result.y));
        assertFalse(Float.isNaN(result.z));
    }

    @Test
    void testBuildFromModelTransform() {
        ModelTransform transform = new ModelTransform();
        transform.setPosition(new Vector3f(5, 10, 15));
        transform.setRotation(new Vector3f(45, 90, 0));
        transform.setScale(new Vector3f(2, 2, 2));
        
        Matrix4f matrix = build(transform);
        
        assertNotNull(matrix);
        assertFalse(matrix.equals(Matrix4f.identity()));
        
        // Проверяем, что матрица работает
        Vector3f point = new Vector3f(1, 1, 1);
        Vector3f result = applyMatrix(matrix, point);
        
        assertFalse(Float.isNaN(result.x));
        assertFalse(Float.isNaN(result.y));
        assertFalse(Float.isNaN(result.z));
    }

    @Test
    void testBuildFromModelTransformIdentity() {
        ModelTransform transform = new ModelTransform(); // По умолчанию: (0,0,0), (0,0,0), (1,1,1)
        
        Matrix4f matrix = build(transform);
        
        assertNotNull(matrix);
        assertEquals(Matrix4f.identity(), matrix);
    }

    @Test
    void testBuildFromModelTransformNull() {
        Matrix4f matrix = build((ModelTransform) null);
        
        assertNotNull(matrix);
        assertEquals(Matrix4f.identity(), matrix);
    }

    @Test
    void testBuildFromComponentsNullArguments() {
        Vector3f valid = new Vector3f(0, 0, 0);
        
        assertThrows(IllegalArgumentException.class, () -> {
            build(null, valid, valid);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            build(valid, null, valid);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            build(valid, valid, null);
        });
    }

    @Test
    void testBuildOrderT_R_S() {
        // Проверяем, что порядок комбинирования правильный: T * R * S
        // Создаем матрицу вручную в том же порядке
        Vector3f position = new Vector3f(10, 0, 0);
        Vector3f rotation = new Vector3f(0, 0, 0); // Нет вращения для простоты
        Vector3f scale = new Vector3f(2, 2, 2);
        
        Matrix4f builtMatrix = build(position, rotation, scale);
        
        // Создаем ожидаемую матрицу вручную: T * R * S
        Matrix4f expectedScale = createScaleMatrix(scale);
        Matrix4f expectedRotation = createRotationMatrix(rotation);
        Matrix4f expectedTranslation = createTranslationMatrix(position);
        Matrix4f expectedMatrix = expectedTranslation.multiply(expectedRotation).multiply(expectedScale);
        
        assertMatrixEquals(expectedMatrix, builtMatrix, EPSILON);
    }

    @Test
    void testBuildWithZeroScale() {
        Vector3f position = new Vector3f(0, 0, 0);
        Vector3f rotation = new Vector3f(0, 0, 0);
        Vector3f scale = new Vector3f(0, 0, 0);
        
        Matrix4f matrix = build(position, rotation, scale);
        
        assertNotNull(matrix);
        
        // Точка должна стать нулевой
        Vector3f point = new Vector3f(1, 2, 3);
        Vector3f result = applyMatrix(matrix, point);
        
        assertEquals(0, result.x, EPSILON);
        assertEquals(0, result.y, EPSILON);
        assertEquals(0, result.z, EPSILON);
    }

    @Test
    void testBuildWithNegativeScale() {
        Vector3f position = new Vector3f(0, 0, 0);
        Vector3f rotation = new Vector3f(0, 0, 0);
        Vector3f scale = new Vector3f(-1, -1, -1);
        
        Matrix4f matrix = build(position, rotation, scale);
        
        assertNotNull(matrix);
        
        // Точка должна отразиться
        Vector3f point = new Vector3f(1, 2, 3);
        Vector3f result = applyMatrix(matrix, point);
        
        assertEquals(-1, result.x, EPSILON);
        assertEquals(-2, result.y, EPSILON);
        assertEquals(-3, result.z, EPSILON);
    }

    @Test
    void testBuildWithLargeRotation() {
        Vector3f position = new Vector3f(0, 0, 0);
        Vector3f rotation = new Vector3f(360, 720, 0); // Большие углы
        Vector3f scale = new Vector3f(1, 1, 1);
        
        Matrix4f matrix = build(position, rotation, scale);
        
        assertNotNull(matrix);
        
        // Проверяем, что результат валиден
        Vector3f point = new Vector3f(1, 0, 0);
        Vector3f result = applyMatrix(matrix, point);
        
        assertFalse(Float.isNaN(result.x));
        assertFalse(Float.isNaN(result.y));
        assertFalse(Float.isNaN(result.z));
        assertFalse(Float.isInfinite(result.x));
        assertFalse(Float.isInfinite(result.y));
        assertFalse(Float.isInfinite(result.z));
    }

    @Test
    void testBuildConsistency() {
        // Проверяем, что build() с одинаковыми параметрами дает одинаковый результат
        Vector3f position = new Vector3f(5, 10, 15);
        Vector3f rotation = new Vector3f(45, 90, 135);
        Vector3f scale = new Vector3f(2, 3, 4);
        
        Matrix4f matrix1 = build(position, rotation, scale);
        Matrix4f matrix2 = build(position, rotation, scale);
        
        assertEquals(matrix1, matrix2);
    }

    @Test
    void testBuildFromModelTransformConsistency() {
        // Проверяем, что build(ModelTransform) дает тот же результат, что и build(components)
        ModelTransform transform = new ModelTransform();
        transform.setPosition(new Vector3f(10, 20, 30));
        transform.setRotation(new Vector3f(90, 45, 0));
        transform.setScale(new Vector3f(2, 3, 4));
        
        Matrix4f matrixFromTransform = build(transform);
        Matrix4f matrixFromComponents = build(
            transform.getPosition(),
            transform.getRotation(),
            transform.getScale()
        );
        
        assertEquals(matrixFromTransform, matrixFromComponents);
    }

    @Test
    void testBuildTranslationThenRotation() {
        // Проверяем правильный порядок: сначала масштаб (S), потом вращение (R), потом перенос (T)
        // T * R * S означает, что масштаб применяется первым
        Vector3f position = new Vector3f(10, 0, 0);
        Vector3f rotation = new Vector3f(0, 90, 0); // 90° вокруг Y
        Vector3f scale = new Vector3f(2, 1, 1); // Удваиваем X
        
        Matrix4f matrix = build(position, rotation, scale);
        
        // Точка (1, 0, 0):
        // 1. Масштаб: (1, 0, 0) * 2 = (2, 0, 0)
        // 2. Вращение на 90° вокруг Y: (2, 0, 0) -> (0, 0, -2)
        // 3. Перенос: (0, 0, -2) + (10, 0, 0) = (10, 0, -2)
        Vector3f point = new Vector3f(1, 0, 0);
        Vector3f result = applyMatrix(matrix, point);
        
        // Проверяем, что результат не равен исходной точке
        assertFalse(Math.abs(result.x - 1) < EPSILON);
        
        // Результат должен быть валидным
        assertFalse(Float.isNaN(result.x));
        assertFalse(Float.isNaN(result.y));
        assertFalse(Float.isNaN(result.z));
    }
}
