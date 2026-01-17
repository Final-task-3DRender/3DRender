package com.cgvsu.math;

/**
 * Двумерный вектор для работы с UV координатами и 2D точками.
 */
public class Vector2f {
    
    private static final float EPSILON = 1e-7f;
    
    public float x;
    public float y;

    public Vector2f() {
        this(0.0f, 0.0f);
    }

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2f(Vector2f other) {
        requireNonNull(other, "Vector");
        this.x = other.x;
        this.y = other.y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }


    private static void requireNonNull(Vector2f vector, String paramName) {
        if (vector == null) {
            throw new IllegalArgumentException(paramName + " не может быть null");
        }
    }

    public Vector2f add(Vector2f other) {
        requireNonNull(other, "Vector");
        return new Vector2f(this.x + other.x, this.y + other.y);
    }

    public Vector2f subtract(Vector2f other) {
        requireNonNull(other, "Vector");
        return new Vector2f(this.x - other.x, this.y - other.y);
    }

    public Vector2f multiply(float scalar) {
        return new Vector2f(this.x * scalar, this.y * scalar);
    }

    private static void checkNonZero(float scalar) {
        if (Math.abs(scalar) < EPSILON) {
            throw new ArithmeticException("Деление на ноль");
        }
    }

    public Vector2f divide(float scalar) {
        checkNonZero(scalar);
        return new Vector2f(this.x / scalar, this.y / scalar);
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public Vector2f normalize() {
        float len = length();
        if (len < EPSILON) {
            throw new ArithmeticException("Невозможно нормализовать нулевой вектор");
        }
        return new Vector2f(this.x / len, this.y / len);
    }

    public float dot(Vector2f other) {
        requireNonNull(other, "Vector");
        return this.x * other.x + this.y * other.y;
    }

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
