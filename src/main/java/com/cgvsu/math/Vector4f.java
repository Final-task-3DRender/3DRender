package com.cgvsu.math;

/**
 * Класс для работы с четырехмерными векторами
 */
public class Vector4f {
    private static final float EPSILON = 1e-7f;
    
    public float x;
    public float y;
    public float z;
    public float w;

    /**
     * Создает нулевой вектор
     */
    public Vector4f() {
        this(0.0f, 0.0f, 0.0f, 0.0f);
    }

    /**
     * Создает вектор с заданными координатами
     * @param x координата x
     * @param y координата y
     * @param z координата z
     * @param w координата w
     */
    public Vector4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    private static void requireNonNull(Vector4f vector, String paramName) {
        if (vector == null) {
            throw new IllegalArgumentException(paramName + " не может быть null");
        }
    }

    private static void requireNonNull(Vector3f vector, String paramName) {
        if (vector == null) {
            throw new IllegalArgumentException(paramName + " не может быть null");
        }
    }

    /**
     * Создает вектор из Vector3f с заданным w
     * @param v трехмерный вектор
     * @param w координата w
     */
    public Vector4f(Vector3f v, float w) {
        requireNonNull(v, "Vector3f");
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.w = w;
    }

    /**
     * Создает копию вектора
     * @param other исходный вектор
     */
    public Vector4f(Vector4f other) {
        requireNonNull(other, "Vector");
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        this.w = other.w;
    }

    /**
     * Возвращает координату x
     * @return координата x
     */
    public float getX() {
        return x;
    }

    /**
     * Возвращает координату y
     * @return координата y
     */
    public float getY() {
        return y;
    }

    /**
     * Возвращает координату z
     * @return координата z
     */
    public float getZ() {
        return z;
    }

    /**
     * Возвращает координату w
     * @return координата w
     */
    public float getW() {
        return w;
    }

    /**
     * Сложение векторов
     * @param other другой вектор
     * @return новый вектор
     */
    public Vector4f add(Vector4f other) {
        requireNonNull(other, "Vector");
        return new Vector4f(this.x + other.x, this.y + other.y, this.z + other.z, this.w + other.w);
    }

    /**
     * Вычитание векторов
     * @param other другой вектор
     * @return новый вектор
     */
    public Vector4f subtract(Vector4f other) {
        requireNonNull(other, "Vector");
        return new Vector4f(this.x - other.x, this.y - other.y, this.z - other.z, this.w - other.w);
    }

    /**
     * Умножение на скаляр
     * @param scalar скалярное значение
     * @return новый вектор
     */
    public Vector4f multiply(float scalar) {
        return new Vector4f(this.x * scalar, this.y * scalar, this.z * scalar, this.w * scalar);
    }

    private static void checkNonZero(float scalar) {
        if (Math.abs(scalar) < EPSILON) {
            throw new ArithmeticException("Деление на ноль");
        }
    }

    /**
     * Деление на скаляр
     * @param scalar скалярное значение
     * @return новый вектор
     */
    public Vector4f divide(float scalar) {
        checkNonZero(scalar);
        return new Vector4f(this.x / scalar, this.y / scalar, this.z / scalar, this.w / scalar);
    }

    /**
     * Вычисление длины вектора
     * @return длина вектора
     */
    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z + w * w);
    }

    /**
     * Нормализация вектора
     * @return новый нормализованный вектор
     */
    public Vector4f normalize() {
        float len = length();
        if (len < EPSILON) {
            throw new ArithmeticException("Невозможно нормализовать нулевой вектор");
        }
        return new Vector4f(this.x / len, this.y / len, this.z / len, this.w / len);
    }

    /**
     * Скалярное произведение
     * @param other другой вектор
     * @return скалярное произведение
     */
    public float dot(Vector4f other) {
        requireNonNull(other, "Vector");
        return this.x * other.x + this.y * other.y + this.z * other.z + this.w * other.w;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector4f vector4f = (Vector4f) obj;
        return Math.abs(this.x - vector4f.x) < EPSILON 
            && Math.abs(this.y - vector4f.y) < EPSILON 
            && Math.abs(this.z - vector4f.z) < EPSILON
            && Math.abs(this.w - vector4f.w) < EPSILON;
    }

    @Override
    public int hashCode() {
        float scale = 1.0f / EPSILON;
        float maxValue = Integer.MAX_VALUE / scale;
        float safeX = Math.max(-maxValue, Math.min(maxValue, x));
        float safeY = Math.max(-maxValue, Math.min(maxValue, y));
        float safeZ = Math.max(-maxValue, Math.min(maxValue, z));
        float safeW = Math.max(-maxValue, Math.min(maxValue, w));
        return Integer.hashCode(Math.round(safeX * scale)) * 31 * 31 * 31 
             + Integer.hashCode(Math.round(safeY * scale)) * 31 * 31 
             + Integer.hashCode(Math.round(safeZ * scale)) * 31 
             + Integer.hashCode(Math.round(safeW * scale));
    }

    @Override
    public String toString() {
        return String.format("Vector4f(%.3f, %.3f, %.3f, %.3f)", x, y, z, w);
    }
}

