package com.cgvsu.math;

/**
 * Класс для представления двумерных точек на экране.
 * 
 * <p>Используется для хранения экранных координат после преобразования
 * вершин через графический конвейер. Отличается от Vector2f тем, что
 * представляет именно точку, а не вектор (направление).
 * 
 * <p>Предоставляет методы сравнения с учетом погрешности вычислений.
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class Point2f {
    
    /**
     * Эпсилон для сравнения чисел с плавающей точкой.
     */
    private static final float EPSILON = 1e-7f;
    
    /**
     * X-координата точки.
     */
    public float x;
    
    /**
     * Y-координата точки.
     */
    public float y;

    /**
     * Создает точку в начале координат
     */
    public Point2f() {
        this(0.0f, 0.0f);
    }

    /**
     * Создает точку с заданными координатами
     * @param x координата x
     * @param y координата y
     */
    public Point2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Создает копию точки
     * @param other исходная точка
     */
    public Point2f(Point2f other) {
        if (other == null) {
            throw new IllegalArgumentException("Point не может быть null");
        }
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Point2f point2f = (Point2f) obj;
        return Math.abs(this.x - point2f.x) < EPSILON && Math.abs(this.y - point2f.y) < EPSILON;
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
        return String.format("Point2f(%.3f, %.3f)", x, y);
    }
}

