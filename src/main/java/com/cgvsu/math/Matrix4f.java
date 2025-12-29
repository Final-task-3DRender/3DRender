package com.cgvsu.math;

import java.util.Arrays;

/**
 * Класс для работы с матрицами 4×4
 */
public class Matrix4f {
    private static final int SIZE = 4;
    private static final float EPSILON = 1e-7f;
    
    private final float[] matrix;

    /**
     * Создает единичную матрицу
     */
    public Matrix4f() {
        matrix = new float[SIZE * SIZE];
        setIdentity();
    }

    /**
     * Создает нулевую матрицу
     * @return нулевая матрица
     */
    public static Matrix4f zero() {
        Matrix4f m = new Matrix4f();
        m.setZero();
        return m;
    }

    /**
     * Создает единичную матрицу
     * @return единичная матрица
     */
    public static Matrix4f identity() {
        return new Matrix4f();
    }

    /**
     * Создает матрицу из двумерного массива
     * @param m двумерный массив 4×4
     */
    public Matrix4f(float[][] m) {
        if (m == null || m.length != SIZE) {
            throw new IllegalArgumentException("Матрица должна быть 4x4");
        }
        for (int i = 0; i < SIZE; i++) {
            if (m[i] == null || m[i].length != SIZE) {
                throw new IllegalArgumentException("Матрица должна быть 4x4");
            }
        }
        matrix = new float[SIZE * SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                matrix[i * SIZE + j] = m[i][j];
            }
        }
    }

    /**
     * Создает матрицу из одномерного массива
     * @param m одномерный массив из 16 элементов
     */
    public Matrix4f(float[] m) {
        if (m == null || m.length != SIZE * SIZE) {
            throw new IllegalArgumentException("Массив должен содержать 16 элементов");
        }
        matrix = new float[SIZE * SIZE];
        System.arraycopy(m, 0, matrix, 0, SIZE * SIZE);
    }

    private static void requireNonNull(Matrix4f matrix, String paramName) {
        if (matrix == null) {
            throw new IllegalArgumentException(paramName + " не может быть null");
        }
    }

    private static void requireNonNull(Vector4f vector, String paramName) {
        if (vector == null) {
            throw new IllegalArgumentException(paramName + " не может быть null");
        }
    }

    /**
     * Создает копию матрицы
     * @param other исходная матрица
     */
    public Matrix4f(Matrix4f other) {
        requireNonNull(other, "Matrix");
        matrix = new float[SIZE * SIZE];
        System.arraycopy(other.matrix, 0, matrix, 0, SIZE * SIZE);
    }

    /**
     * Устанавливает единичную матрицу
     */
    public void setIdentity() {
        setZero();
        for (int i = 0; i < SIZE; i++) {
            matrix[i * SIZE + i] = 1.0f;
        }
    }

    /**
     * Устанавливает нулевую матрицу
     */
    public void setZero() {
        Arrays.fill(matrix, 0.0f);
    }

    private void validateIndices(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            throw new IndexOutOfBoundsException(
                String.format("Индекс вне границ: строка=%d, столбец=%d (размер матрицы: %dx%d)", 
                    row, col, SIZE, SIZE));
        }
    }

    /**
     * Получить значение элемента матрицы
     * @param row номер строки (0-3)
     * @param col номер столбца (0-3)
     * @return значение элемента
     */
    public float get(int row, int col) {
        validateIndices(row, col);
        return matrix[row * SIZE + col];
    }

    /**
     * Установить значение элемента матрицы
     * @param row номер строки (0-3)
     * @param col номер столбца (0-3)
     * @param value новое значение
     */
    public void set(int row, int col, float value) {
        validateIndices(row, col);
        matrix[row * SIZE + col] = value;
    }

    /**
     * Сложение матриц
     * @param other другая матрица
     * @return новая матрица
     */
    public Matrix4f add(Matrix4f other) {
        requireNonNull(other, "Matrix");
        Matrix4f result = Matrix4f.zero();
        for (int i = 0; i < SIZE * SIZE; i++) {
            result.matrix[i] = this.matrix[i] + other.matrix[i];
        }
        return result;
    }

    /**
     * Вычитание матриц
     * @param other другая матрица
     * @return новая матрица
     */
    public Matrix4f subtract(Matrix4f other) {
        requireNonNull(other, "Matrix");
        Matrix4f result = Matrix4f.zero();
        for (int i = 0; i < SIZE * SIZE; i++) {
            result.matrix[i] = this.matrix[i] - other.matrix[i];
        }
        return result;
    }

    /**
     * Умножение матриц
     * @param other другая матрица
     * @return новая матрица
     */
    public Matrix4f multiply(Matrix4f other) {
        requireNonNull(other, "Matrix");
        Matrix4f result = Matrix4f.zero();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                float sum = 0.0f;
                for (int k = 0; k < SIZE; k++) {
                    sum += this.matrix[i * SIZE + k] * other.matrix[k * SIZE + j];
                }
                result.matrix[i * SIZE + j] = sum;
            }
        }
        return result;
    }

    /**
     * Умножение матрицы на вектор (вектор-столбец)
     * @param vector вектор
     * @return новый вектор
     */
    public Vector4f multiply(Vector4f vector) {
        requireNonNull(vector, "Vector");
        float[] result = new float[SIZE];
        for (int i = 0; i < SIZE; i++) {
            float sum = 0.0f;
            for (int j = 0; j < SIZE; j++) {
                sum += matrix[i * SIZE + j] * getVectorComponent(vector, j);
            }
            result[i] = sum;
        }
        return new Vector4f(result[0], result[1], result[2], result[3]);
    }

    /**
     * Умножение матрицы на трехмерный вектор (добавляется w=1)
     * @param vector трехмерный вектор
     * @return новый четырехмерный вектор
     */
    public Vector4f multiply(Vector3f vector) {
        if (vector == null) {
            throw new IllegalArgumentException("Vector не может быть null");
        }
        Vector4f v4 = new Vector4f(vector, 1.0f);
        return multiply(v4);
    }

    private float getVectorComponent(Vector4f vector, int index) {
        return switch (index) {
            case 0 -> vector.getX();
            case 1 -> vector.getY();
            case 2 -> vector.getZ();
            case 3 -> vector.getW();
            default -> throw new IllegalArgumentException("Неверный индекс компоненты вектора: " + index);
        };
    }

    /**
     * Транспонирование матрицы
     * @return новая транспонированная матрица
     */
    public Matrix4f transpose() {
        Matrix4f result = Matrix4f.zero();
        for (int i = 0; i < SIZE; i++) {
            result.matrix[i * SIZE + i] = this.matrix[i * SIZE + i];
        }
        for (int i = 0; i < SIZE; i++) {
            for (int j = i + 1; j < SIZE; j++) {
                float temp = this.matrix[i * SIZE + j];
                result.matrix[i * SIZE + j] = this.matrix[j * SIZE + i];
                result.matrix[j * SIZE + i] = temp;
            }
        }
        return result;
    }

    /**
     * Вычисление определителя
     * @return определитель матрицы
     */
    public float determinant() {
        float det = 0.0f;
        for (int j = 0; j < SIZE; j++) {
            float sign = (j % 2 == 0) ? 1.0f : -1.0f;
            det += sign * matrix[0 * SIZE + j] * minorDeterminant(0, j);
        }
        return det;
    }

    private float minorDeterminant(int row, int col) {
        float[] minor = new float[9];
        int mi = 0;
        for (int i = 0; i < SIZE; i++) {
            if (i == row) continue;
            for (int j = 0; j < SIZE; j++) {
                if (j == col) continue;
                minor[mi] = matrix[i * SIZE + j];
                mi++;
            }
        }
        float a = minor[0], b = minor[1], c = minor[2];
        float d = minor[3], e = minor[4], f = minor[5];
        float g = minor[6], h = minor[7], i = minor[8];
        return a * (e * i - f * h) - b * (d * i - f * g) + c * (d * h - e * g);
    }

    private static void checkNonZeroDeterminant(float det) {
        if (Math.abs(det) < EPSILON) {
            throw new ArithmeticException("Матрица вырожденная (определитель равен нулю), невозможно вычислить обратную матрицу");
        }
    }

    /**
     * Вычисление обратной матрицы
     * @return обратная матрица
     */
    public Matrix4f inverse() {
        float det = determinant();
        checkNonZeroDeterminant(det);
        
        Matrix4f adjugate = Matrix4f.zero();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                float sign = ((i + j) % 2 == 0) ? 1.0f : -1.0f;
                adjugate.matrix[j * SIZE + i] = sign * minorDeterminant(i, j) / det;
            }
        }
        return adjugate;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Matrix4f matrix4f = (Matrix4f) obj;
        for (int i = 0; i < SIZE * SIZE; i++) {
            if (Math.abs(this.matrix[i] - matrix4f.matrix[i]) >= EPSILON) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        float scale = 1.0f / EPSILON;
        float maxValue = Integer.MAX_VALUE / scale;
        int result = 1;
        for (int i = 0; i < SIZE * SIZE; i++) {
            float safeValue = Math.max(-maxValue, Math.min(maxValue, matrix[i]));
            result = 31 * result + Integer.hashCode(Math.round(safeValue * scale));
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Matrix4f:\n");
        for (int i = 0; i < SIZE; i++) {
            sb.append("[");
            for (int j = 0; j < SIZE; j++) {
                if (j > 0) sb.append(", ");
                sb.append(String.format("%.3f", matrix[i * SIZE + j]));
            }
            sb.append("]\n");
        }
        return sb.toString();
    }
}

