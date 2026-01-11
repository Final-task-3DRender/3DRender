package com.cgvsu.math;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Тесты для класса Vector2f
 */
class Vector2Test {

    private static final float EPSILON = 1e-5f;
    private static final float TEST_X = 1.0f;
    private static final float TEST_Y = 2.0f;

    /**
     * Тест конструкторов
     */
    @Test
    public void testConstructors() {
        Vector2f v1 = new Vector2f();
        Assertions.assertEquals(0.0f, v1.getX(), EPSILON);
        Assertions.assertEquals(0.0f, v1.getY(), EPSILON);
        
        Vector2f v2 = new Vector2f(TEST_X, TEST_Y);
        Assertions.assertEquals(TEST_X, v2.getX(), EPSILON);
        Assertions.assertEquals(TEST_Y, v2.getY(), EPSILON);
    }


    /**
     * Тест сложения векторов.
     * Проверяет, что (1,2) + (3,4) = (4,6).
     */
    @Test
    public void testAdd() {
        Vector2f v1 = new Vector2f(1.0f, 2.0f);
        Vector2f v2 = new Vector2f(3.0f, 4.0f);
        Vector2f result = v1.add(v2);
        Assertions.assertEquals(4.0f, result.getX(), EPSILON);
        Assertions.assertEquals(6.0f, result.getY(), EPSILON);
    }

    /**
     * Тест вычитания векторов.
     * Проверяет, что (5,6) - (2,3) = (3,3).
     */
    @Test
    public void testSubtract() {
        Vector2f v1 = new Vector2f(5.0f, 6.0f);
        Vector2f v2 = new Vector2f(2.0f, 3.0f);
        Vector2f result = v1.subtract(v2);
        Assertions.assertEquals(3.0f, result.getX(), EPSILON);
        Assertions.assertEquals(3.0f, result.getY(), EPSILON);
    }

    /**
     * Тест обработки ошибок при операциях с null.
     * Проверяет, что передача null вызывает IllegalArgumentException для всех операций.
     */
    @Test
    public void testOperationsWithNull() {
        Vector2f v1 = new Vector2f(1.0f, 2.0f);
        Assertions.assertThrows(IllegalArgumentException.class, () -> v1.add(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> v1.subtract(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> v1.dot(null));
    }

    /**
     * Тест умножения вектора на скаляр
     */
    @Test
    public void testMultiply() {
        Vector2f v = new Vector2f(2.0f, 3.0f);
        Vector2f result = v.multiply(2.0f);
        Assertions.assertEquals(4.0f, result.getX(), EPSILON);
        Assertions.assertEquals(6.0f, result.getY(), EPSILON);
    }

    /**
     * Тест деления вектора на скаляр.
     * Проверяет, что (4,6) / 2 = (2,3).
     */
    @Test
    public void testDivide() {
        Vector2f v = new Vector2f(4.0f, 6.0f);
        Vector2f result = v.divide(2.0f);
        Assertions.assertEquals(2.0f, result.getX(), EPSILON);
        Assertions.assertEquals(3.0f, result.getY(), EPSILON);
    }

    /**
     * Тест обработки деления на ноль.
     * Проверяет, что деление на 0 вызывает ArithmeticException.
     */
    @Test
    public void testDivideByZero() {
        Vector2f v = new Vector2f(4.0f, 6.0f);
        Assertions.assertThrows(ArithmeticException.class, () -> {
            v.divide(0.0f);
        });
    }

    /**
     * Тест вычисления длины вектора
     */
    @Test
    public void testLength() {
        Vector2f v = new Vector2f(3.0f, 4.0f);
        Assertions.assertEquals(5.0f, v.length(), EPSILON);
    }


    /**
     * Тест нормализации вектора.
     * Проверяет, что нормализованный вектор (3,4) имеет длину 1.0 и координаты (0.6, 0.8).
     * Нормализация: v / |v| = (3/5, 4/5) = (0.6, 0.8).
     */
    @Test
    public void testNormalize() {
        Vector2f v = new Vector2f(3.0f, 4.0f);
        Vector2f normalized = v.normalize();
        Assertions.assertEquals(1.0f, normalized.length(), EPSILON);
        Assertions.assertEquals(0.6f, normalized.getX(), EPSILON);
        Assertions.assertEquals(0.8f, normalized.getY(), EPSILON);
    }

    /**
     * Тест обработки нормализации нулевого вектора.
     * Проверяет, что попытка нормализовать нулевой вектор вызывает ArithmeticException.
     */
    @Test
    public void testNormalizeZero() {
        Vector2f v = new Vector2f(0.0f, 0.0f);
        Assertions.assertThrows(ArithmeticException.class, () -> {
            v.normalize();
        });
    }


    /**
     * Тест скалярного произведения векторов
     */
    @Test
    public void testDot() {
        Vector2f v1 = new Vector2f(1.0f, 2.0f);
        Vector2f v2 = new Vector2f(3.0f, 4.0f);
        float result = v1.dot(v2);
        Assertions.assertEquals(11.0f, result, EPSILON);
    }

    /**
     * Тест сравнения векторов
     */
    @Test
    public void testEquals() {
        Vector2f v1 = new Vector2f(1.0f, 2.0f);
        Vector2f v2 = new Vector2f(1.0f, 2.0f);
        Vector2f v3 = new Vector2f(1.0f, 3.0f);
        
        Assertions.assertTrue(v1.equals(v2));
        Assertions.assertTrue(v1.equals(v1));
        Assertions.assertFalse(v1.equals(v3));
        Assertions.assertFalse(v1.equals(null));
        Assertions.assertFalse(v1.equals("not a vector"));
    }

}
