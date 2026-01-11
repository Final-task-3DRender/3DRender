package com.cgvsu.math;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Тесты для класса Vector3f
 */
class Vector3fTest {

    private static final float EPSILON = 1e-5f;
    private static final float TEST_X = 1.0f;
    private static final float TEST_Y = 2.0f;
    private static final float TEST_Z = 3.0f;

    /**
     * Тест конструкторов
     */
    @Test
    public void testConstructors() {
        Vector3f v1 = new Vector3f();
        Assertions.assertEquals(0.0f, v1.getX(), EPSILON);
        Assertions.assertEquals(0.0f, v1.getY(), EPSILON);
        Assertions.assertEquals(0.0f, v1.getZ(), EPSILON);
        
        Vector3f v2 = new Vector3f(1.0f, 2.0f, 3.0f);
        Assertions.assertEquals(1.0f, v2.getX(), EPSILON);
        Assertions.assertEquals(2.0f, v2.getY(), EPSILON);
        Assertions.assertEquals(3.0f, v2.getZ(), EPSILON);
    }


    /**
     * Тест сложения трехмерных векторов.
     * Проверяет, что (1,2,3) + (4,5,6) = (5,7,9).
     */
    @Test
    public void testAdd() {
        Vector3f v1 = new Vector3f(1.0f, 2.0f, 3.0f);
        Vector3f v2 = new Vector3f(4.0f, 5.0f, 6.0f);
        Vector3f result = v1.add(v2);
        Assertions.assertEquals(5.0f, result.getX(), EPSILON);
        Assertions.assertEquals(7.0f, result.getY(), EPSILON);
        Assertions.assertEquals(9.0f, result.getZ(), EPSILON);
    }

    /**
     * Тест вычитания трехмерных векторов.
     * Проверяет, что (5,6,7) - (2,3,4) = (3,3,3).
     */
    @Test
    public void testSubtract() {
        Vector3f v1 = new Vector3f(5.0f, 6.0f, 7.0f);
        Vector3f v2 = new Vector3f(2.0f, 3.0f, 4.0f);
        Vector3f result = v1.subtract(v2);
        Assertions.assertEquals(3.0f, result.getX(), EPSILON);
        Assertions.assertEquals(3.0f, result.getY(), EPSILON);
        Assertions.assertEquals(3.0f, result.getZ(), EPSILON);
    }

    /**
     * Тест умножения трехмерного вектора на скаляр.
     * Проверяет, что (2,3,4) * 2 = (4,6,8).
     */
    @Test
    public void testMultiply() {
        Vector3f v = new Vector3f(2.0f, 3.0f, 4.0f);
        Vector3f result = v.multiply(2.0f);
        Assertions.assertEquals(4.0f, result.getX(), EPSILON);
        Assertions.assertEquals(6.0f, result.getY(), EPSILON);
        Assertions.assertEquals(8.0f, result.getZ(), EPSILON);
    }

    /**
     * Тест деления трехмерного вектора на скаляр.
     * Проверяет, что (4,6,8) / 2 = (2,3,4).
     */
    @Test
    public void testDivide() {
        Vector3f v = new Vector3f(4.0f, 6.0f, 8.0f);
        Vector3f result = v.divide(2.0f);
        Assertions.assertEquals(2.0f, result.getX(), EPSILON);
        Assertions.assertEquals(3.0f, result.getY(), EPSILON);
        Assertions.assertEquals(4.0f, result.getZ(), EPSILON);
    }

    /**
     * Тест обработки деления на ноль.
     * Проверяет, что деление на 0 вызывает ArithmeticException.
     */
    @Test
    public void testDivideByZero() {
        Vector3f v = new Vector3f(4.0f, 6.0f, 8.0f);
        Assertions.assertThrows(ArithmeticException.class, () -> {
            v.divide(0.0f);
        });
    }

    /**
     * Тест вычисления длины трехмерного вектора.
     * Проверяет, что длина (2,3,6) равна √(2²+3²+6²) = √49 = 7.
     */
    @Test
    public void testLength() {
        Vector3f v = new Vector3f(2.0f, 3.0f, 6.0f);
        float expected = (float) Math.sqrt(2*2 + 3*3 + 6*6);
        Assertions.assertEquals(expected, v.length(), EPSILON);
    }

    /**
     * Тест нормализации трехмерного вектора.
     * Проверяет, что нормализованный вектор (2,0,0) имеет длину 1.0 и координаты (1,0,0).
     */
    @Test
    public void testNormalize() {
        Vector3f v = new Vector3f(2.0f, 0.0f, 0.0f);
        Vector3f normalized = v.normalize();
        Assertions.assertEquals(1.0f, normalized.length(), EPSILON);
        Assertions.assertEquals(1.0f, normalized.getX(), EPSILON);
        Assertions.assertEquals(0.0f, normalized.getY(), EPSILON);
        Assertions.assertEquals(0.0f, normalized.getZ(), EPSILON);
    }

    /**
     * Тест обработки нормализации нулевого вектора.
     * Проверяет, что попытка нормализовать нулевой вектор вызывает ArithmeticException.
     */
    @Test
    public void testNormalizeZero() {
        Vector3f v = new Vector3f(0.0f, 0.0f, 0.0f);
        Assertions.assertThrows(ArithmeticException.class, () -> {
            v.normalize();
        });
    }

    /**
     * Тест скалярного произведения трехмерных векторов.
     * Проверяет, что (1,2,3) · (4,5,6) = 1*4 + 2*5 + 3*6 = 32.
     */
    @Test
    public void testDot() {
        Vector3f v1 = new Vector3f(1.0f, 2.0f, 3.0f);
        Vector3f v2 = new Vector3f(4.0f, 5.0f, 6.0f);
        float result = v1.dot(v2);
        Assertions.assertEquals(32.0f, result, EPSILON); // 1*4 + 2*5 + 3*6 = 4 + 10 + 18 = 32
    }

    /**
     * Тест векторного произведения базисных векторов.
     * Проверяет, что (1,0,0) × (0,1,0) = (0,0,1).
     * Результат перпендикулярен обоим исходным векторам.
     */
    @Test
    public void testCross() {
        Vector3f v1 = new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f v2 = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f result = v1.cross(v2);
        Assertions.assertEquals(0.0f, result.getX(), EPSILON);
        Assertions.assertEquals(0.0f, result.getY(), EPSILON);
        Assertions.assertEquals(1.0f, result.getZ(), EPSILON);
    }


    /**
     * Тест обработки ошибок при операциях с null.
     * Проверяет, что передача null вызывает IllegalArgumentException для всех операций.
     */
    @Test
    public void testOperationsWithNull() {
        Vector3f v1 = new Vector3f(1.0f, 2.0f, 3.0f);
        Assertions.assertThrows(IllegalArgumentException.class, () -> v1.add(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> v1.subtract(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> v1.dot(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> v1.cross(null));
    }


    /**
     * Тест сравнения векторов
     */
    @Test
    public void testEquals() {
        Vector3f v1 = new Vector3f(TEST_X, TEST_Y, TEST_Z);
        Vector3f v2 = new Vector3f(TEST_X, TEST_Y, TEST_Z);
        Vector3f v3 = new Vector3f(TEST_X + 1.0f, TEST_Y, TEST_Z);
        
        Assertions.assertTrue(v1.equals(v2));
        Assertions.assertTrue(v1.equals(v1));
        Assertions.assertFalse(v1.equals(v3));
        Assertions.assertFalse(v1.equals(null));
        Assertions.assertFalse(v1.equals("not a vector"));
    }



}
