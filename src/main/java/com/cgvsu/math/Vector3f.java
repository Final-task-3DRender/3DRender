package com.cgvsu.math;

/**
 * Класс для работы с трехмерными векторами в пространстве.
 * 
 * <p>Предоставляет основные операции линейной алгебры:
 * <ul>
 *   <li>Сложение и вычитание векторов</li>
 *   <li>Умножение и деление на скаляр</li>
 *   <li>Вычисление длины вектора</li>
 *   <li>Нормализация (приведение к единичной длине)</li>
 *   <li>Скалярное произведение (dot product)</li>
 *   <li>Векторное произведение (cross product)</li>
 * </ul>
 * 
 * <p>Все операции создают новые векторы, исходные векторы не изменяются.
 * Используется для представления точек, направлений, нормалей в 3D пространстве.
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class Vector3f {
    
    /**
     * Эпсилон для сравнения чисел с плавающей точкой.
     * Используется при проверке равенства векторов и делении на ноль.
     */
    private static final float EPSILON = 1e-7f;
    
    /**
     * X-компонента вектора.
     */
    public float x;
    
    /**
     * Y-компонента вектора.
     */
    public float y;
    
    /**
     * Z-компонента вектора.
     */
    public float z;

    /**
     * Создает нулевой вектор
     */
    public Vector3f() {
        this(0.0f, 0.0f, 0.0f);
    }

    /**
     * Создает вектор с заданными координатами
     * @param x координата x
     * @param y координата y
     * @param z координата z
     */
    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private static void requireNonNull(Vector3f vector, String paramName) {
        if (vector == null) {
            throw new IllegalArgumentException(paramName + " не может быть null");
        }
    }

    /**
     * Создает копию вектора
     * @param other исходный вектор
     */
    public Vector3f(Vector3f other) {
        requireNonNull(other, "Vector");
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
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

    /**
     * Устанавливает координату z
     */
    public void setZ(float z) {
        this.z = z;
    }


    /**
     * Сложение векторов
     * @param other другой вектор
     * @return новый вектор
     */
    public Vector3f add(Vector3f other) {
        requireNonNull(other, "Vector");
        return new Vector3f(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    /**
     * Вычитание векторов
     * @param other другой вектор
     * @return новый вектор
     */
    public Vector3f subtract(Vector3f other) {
        requireNonNull(other, "Vector");
        return new Vector3f(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    /**
     * Умножение на скаляр
     * @param scalar скалярное значение
     * @return новый вектор
     */
    public Vector3f multiply(float scalar) {
        return new Vector3f(this.x * scalar, this.y * scalar, this.z * scalar);
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
    public Vector3f divide(float scalar) {
        checkNonZero(scalar);
        return new Vector3f(this.x / scalar, this.y / scalar, this.z / scalar);
    }

    /**
     * Вычисление длины вектора
     * @return длина вектора
     */
    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Нормализация вектора
     * @return новый нормализованный вектор
     */
    public Vector3f normalize() {
        float len = length();
        if (len < EPSILON) {
            throw new ArithmeticException("Невозможно нормализовать нулевой вектор");
        }
        return new Vector3f(this.x / len, this.y / len, this.z / len);
    }

    /**
     * Скалярное произведение
     * @param other другой вектор
     * @return скалярное произведение
     */
    public float dot(Vector3f other) {
        requireNonNull(other, "Vector");
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    /**
     * Векторное произведение
     * @param other другой вектор
     * @return новый вектор
     */
    public Vector3f cross(Vector3f other) {
        requireNonNull(other, "Vector");
        return new Vector3f(
            this.y * other.z - this.z * other.y,
            this.z * other.x - this.x * other.z,
            this.x * other.y - this.y * other.x
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector3f vector3f = (Vector3f) obj;
        return Math.abs(this.x - vector3f.x) < EPSILON 
            && Math.abs(this.y - vector3f.y) < EPSILON 
            && Math.abs(this.z - vector3f.z) < EPSILON;
    }

    /**
     * Сравнение векторов (для обратной совместимости)
     */
    public boolean equals(Vector3f other) {
        return equals((Object) other);
    }

    @Override
    public int hashCode() {
        float scale = 1.0f / EPSILON;
        float maxValue = Integer.MAX_VALUE / scale;
        float safeX = Math.max(-maxValue, Math.min(maxValue, x));
        float safeY = Math.max(-maxValue, Math.min(maxValue, y));
        float safeZ = Math.max(-maxValue, Math.min(maxValue, z));
        return Integer.hashCode(Math.round(safeX * scale)) * 31 * 31 
             + Integer.hashCode(Math.round(safeY * scale)) * 31 
             + Integer.hashCode(Math.round(safeZ * scale));
    }

    @Override
    public String toString() {
        return String.format("Vector3f(%.3f, %.3f, %.3f)", x, y, z);
    }
}
