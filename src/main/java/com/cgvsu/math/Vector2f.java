package com.cgvsu.math;

/**
 * Класс для работы с двумерными векторами
 */
public class Vector2f {
    private static final float EPSILON = 1e-7f;
    
    public float x;
    public float y;

    /**
     * Создает нулевой вектор
     */
    public Vector2f() {
        this(0.0f, 0.0f);
    }

    /**
     * Создает вектор с заданными координатами
     * @param x координата x
     * @param y координата y
     */
    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Создает копию вектора
     * @param other исходный вектор
     */
    public Vector2f(Vector2f other) {
        requireNonNull(other, "Vector");
        this.x = other.x;
        this.y = other.y;
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
     * Устанавливает координату x
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * Устанавливает координату y
     */
    public void setY(float y) {
        this.y = y;
    }


    private static void requireNonNull(Vector2f vector, String paramName) {
        if (vector == null) {
            throw new IllegalArgumentException(paramName + " не может быть null");
        }
    }

    /**
     * Сложение векторов
     * @param other другой вектор
     * @return новый вектор
     */
    public Vector2f add(Vector2f other) {
        requireNonNull(other, "Vector");
        return new Vector2f(this.x + other.x, this.y + other.y);
    }

    /**
     * Вычитание векторов
     * @param other другой вектор
     * @return новый вектор
     */
    public Vector2f subtract(Vector2f other) {
        requireNonNull(other, "Vector");
        return new Vector2f(this.x - other.x, this.y - other.y);
    }

    /**
     * Умножение на скаляр
     * @param scalar скалярное значение
     * @return новый вектор
     */
    public Vector2f multiply(float scalar) {
        return new Vector2f(this.x * scalar, this.y * scalar);
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
    public Vector2f divide(float scalar) {
        checkNonZero(scalar);
        return new Vector2f(this.x / scalar, this.y / scalar);
    }

    /**
     * Вычисление длины вектора
     * @return длина вектора
     */
    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Нормализация вектора
     * @return новый нормализованный вектор
     */
    public Vector2f normalize() {
        float len = length();
        if (len < EPSILON) {
            throw new ArithmeticException("Невозможно нормализовать нулевой вектор");
        }
        return new Vector2f(this.x / len, this.y / len);
    }

    /**
     * Скалярное произведение
     * @param other другой вектор
     * @return скалярное произведение
     */
    public float dot(Vector2f other) {
        requireNonNull(other, "Vector");
        return this.x * other.x + this.y * other.y;
    }

    /**
     * Сравнение векторов
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector2f vector2f = (Vector2f) obj;
        return Math.abs(this.x - vector2f.x) < EPSILON && Math.abs(this.y - vector2f.y) < EPSILON;
    }

    @Override
    public int hashCode() {
        float scale = 1.0f / EPSILON;
        float maxValue = Integer.MAX_VALUE / scale;
        float safeX = Math.max(-maxValue, Math.min(maxValue, x));
        float safeY = Math.max(-maxValue, Math.min(maxValue, y));
        return Integer.hashCode(Math.round(safeX * scale)) * 31 
             + Integer.hashCode(Math.round(safeY * scale));
    }

    @Override
    public String toString() {
        return String.format("Vector2f(%.3f, %.3f)", x, y);
    }
}
