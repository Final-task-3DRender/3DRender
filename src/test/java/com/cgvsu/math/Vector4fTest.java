package com.cgvsu.math;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Тесты для класса Vector4f
 */
class Vector4fTest {

    private static final float EPSILON = 1e-5f;
    private static final float TEST_X = 1.0f;
    private static final float TEST_Y = 2.0f;
    private static final float TEST_Z = 3.0f;
    private static final float TEST_W = 4.0f;

    /**
     * Тест конструкторов
     */
    @Test
    public void testConstructors() {
        Vector4f v1 = new Vector4f();
        Assertions.assertEquals(0.0f, v1.getX(), EPSILON);
        Assertions.assertEquals(0.0f, v1.getY(), EPSILON);
        Assertions.assertEquals(0.0f, v1.getZ(), EPSILON);
        Assertions.assertEquals(0.0f, v1.getW(), EPSILON);
        
        Vector4f v2 = new Vector4f(1.0f, 2.0f, 3.0f, 4.0f);
        Assertions.assertEquals(1.0f, v2.getX(), EPSILON);
        Assertions.assertEquals(2.0f, v2.getY(), EPSILON);
        Assertions.assertEquals(3.0f, v2.getZ(), EPSILON);
        Assertions.assertEquals(4.0f, v2.getW(), EPSILON);
    }


    /**
     * Тест сложения четырехмерных векторов.
     * Проверяет, что (1,2,3,4) + (5,6,7,8) = (6,8,10,12).
     */
    @Test
    public void testAdd() {
        Vector4f v1 = new Vector4f(1.0f, 2.0f, 3.0f, 4.0f);
        Vector4f v2 = new Vector4f(5.0f, 6.0f, 7.0f, 8.0f);
        Vector4f result = v1.add(v2);
        Assertions.assertEquals(6.0f, result.getX(), EPSILON);
        Assertions.assertEquals(8.0f, result.getY(), EPSILON);
        Assertions.assertEquals(10.0f, result.getZ(), EPSILON);
        Assertions.assertEquals(12.0f, result.getW(), EPSILON);
    }

    /**
     * Тест вычитания четырехмерных векторов.
     * Проверяет, что (5,6,7,8) - (1,2,3,4) = (4,4,4,4).
     */
    @Test
    public void testSubtract() {
        Vector4f v1 = new Vector4f(5.0f, 6.0f, 7.0f, 8.0f);
        Vector4f v2 = new Vector4f(1.0f, 2.0f, 3.0f, 4.0f);
        Vector4f result = v1.subtract(v2);
        Assertions.assertEquals(4.0f, result.getX(), EPSILON);
        Assertions.assertEquals(4.0f, result.getY(), EPSILON);
        Assertions.assertEquals(4.0f, result.getZ(), EPSILON);
        Assertions.assertEquals(4.0f, result.getW(), EPSILON);
    }

    /**
     * Тест умножения четырехмерного вектора на скаляр.
     * Проверяет, что (2,3,4,5) * 2 = (4,6,8,10).
     */
    @Test
    public void testMultiply() {
        Vector4f v = new Vector4f(2.0f, 3.0f, 4.0f, 5.0f);
        Vector4f result = v.multiply(2.0f);
        Assertions.assertEquals(4.0f, result.getX(), EPSILON);
        Assertions.assertEquals(6.0f, result.getY(), EPSILON);
        Assertions.assertEquals(8.0f, result.getZ(), EPSILON);
        Assertions.assertEquals(10.0f, result.getW(), EPSILON);
    }

    /**
     * Тест деления четырехмерного вектора на скаляр.
     * Проверяет, что (4,6,8,10) / 2 = (2,3,4,5).
     */
    @Test
    public void testDivide() {
        Vector4f v = new Vector4f(4.0f, 6.0f, 8.0f, 10.0f);
        Vector4f result = v.divide(2.0f);
        Assertions.assertEquals(2.0f, result.getX(), EPSILON);
        Assertions.assertEquals(3.0f, result.getY(), EPSILON);
        Assertions.assertEquals(4.0f, result.getZ(), EPSILON);
        Assertions.assertEquals(5.0f, result.getW(), EPSILON);
    }

    /**
     * Тест обработки деления на ноль.
     * Проверяет, что деление на 0 вызывает ArithmeticException.
     */
    @Test
    public void testDivideByZero() {
        Vector4f v = new Vector4f(4.0f, 6.0f, 8.0f, 10.0f);
        Assertions.assertThrows(ArithmeticException.class, () -> {
            v.divide(0.0f);
        });
    }

    /**
     * Тест вычисления длины четырехмерного вектора.
     * Проверяет, что длина (2,3,6,0) равна √(2²+3²+6²) = √49 = 7.
     * Компонента w=0 не влияет на длину в данном случае.
     */
    @Test
    public void testLength() {
        Vector4f v = new Vector4f(2.0f, 3.0f, 6.0f, 0.0f);
        float expected = (float) Math.sqrt(2*2 + 3*3 + 6*6);
        Assertions.assertEquals(expected, v.length(), EPSILON);
    }

    /**
     * Тест нормализации четырехмерного вектора.
     * Проверяет, что нормализованный вектор (2,0,0,0) имеет длину 1.0 и координаты (1,0,0,0).
     */
    @Test
    public void testNormalize() {
        Vector4f v = new Vector4f(2.0f, 0.0f, 0.0f, 0.0f);
        Vector4f normalized = v.normalize();
        Assertions.assertEquals(1.0f, normalized.length(), EPSILON);
        Assertions.assertEquals(1.0f, normalized.getX(), EPSILON);
        Assertions.assertEquals(0.0f, normalized.getY(), EPSILON);
        Assertions.assertEquals(0.0f, normalized.getZ(), EPSILON);
        Assertions.assertEquals(0.0f, normalized.getW(), EPSILON);
    }

    /**
     * Тест обработки нормализации нулевого вектора.
     * Проверяет, что попытка нормализовать нулевой вектор вызывает ArithmeticException.
     */
    @Test
    public void testNormalizeZero() {
        Vector4f v = new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);
        Assertions.assertThrows(ArithmeticException.class, () -> {
            v.normalize();
        });
    }

    /**
     * Тест скалярного произведения четырехмерных векторов.
     * Проверяет, что (1,2,3,4) · (5,6,7,8) = 1*5 + 2*6 + 3*7 + 4*8 = 70.
     */
    @Test
    public void testDot() {
        Vector4f v1 = new Vector4f(1.0f, 2.0f, 3.0f, 4.0f);
        Vector4f v2 = new Vector4f(5.0f, 6.0f, 7.0f, 8.0f);
        float result = v1.dot(v2);
        Assertions.assertEquals(70.0f, result, EPSILON); // 1*5 + 2*6 + 3*7 + 4*8 = 5 + 12 + 21 + 32 = 70
    }

    /**
     * Тест сравнения векторов
     */
    @Test
    public void testEquals() {
        Vector4f v1 = new Vector4f(TEST_X, TEST_Y, TEST_Z, TEST_W);
        Vector4f v2 = new Vector4f(TEST_X, TEST_Y, TEST_Z, TEST_W);
        Vector4f v3 = new Vector4f(TEST_X + 1.0f, TEST_Y, TEST_Z, TEST_W);
        
        Assertions.assertTrue(v1.equals(v2));
        Assertions.assertTrue(v1.equals(v1));
        Assertions.assertFalse(v1.equals(v3));
        Assertions.assertFalse(v1.equals(null));
        Assertions.assertFalse(v1.equals("not a vector"));
    }



}
