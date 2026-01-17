package com.cgvsu.math;

/**
 * Двумерная точка для экранных координат.
 */
public class Point2f {
    
    private static final float EPSILON = 1e-7f;
    
    public float x;
    public float y;

    public Point2f() {
        this(0.0f, 0.0f);
    }

    public Point2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Point2f(Point2f other) {
        if (other == null) {
            throw new IllegalArgumentException("Point не может быть null");
        }
        this.x = other.x;
        this.y = other.y;
    }

    public float getX() {
        return x;
    }

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

