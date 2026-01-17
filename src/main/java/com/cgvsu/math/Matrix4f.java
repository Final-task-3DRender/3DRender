package com.cgvsu.math;

import java.util.Arrays;

/**
 * Матрица 4×4 для аффинных преобразований и проекций.
 */
public class Matrix4f {
    
    /** Размер матрицы (4x4) */
    private static final int SIZE = 4;
    
    /** Эпсилон для сравнения чисел с плавающей точкой */
    private static final float EPSILON = 1e-7f;
    
    /**
     * Внутреннее хранилище матрицы в виде одномерного массива.
     * Элементы хранятся в row-major порядке: [row0col0, row0col1, ..., row3col3]
     */
    private final float[] matrix;

    /**
     * Создает единичную матрицу 4×4.
     * Единичная матрица не изменяет векторы при умножении.
     */
    public Matrix4f() {
        matrix = new float[SIZE * SIZE];
        setIdentity();
    }

    /**
     * Создает нулевую матрицу 4×4 (все элементы равны 0).
     * 
     * @return новая нулевая матрица
     */
    public static Matrix4f zero() {
        Matrix4f m = new Matrix4f();
        m.setZero();
        return m;
    }

    /**
     * Создает единичную матрицу 4×4.
     * 
     * @return новая единичная матрица
     */
    public static Matrix4f identity() {
        return new Matrix4f();
    }

    /**
     * Создает матрицу из двумерного массива.
     * 
     * <p>Массив должен быть размером 4×4. Элементы копируются в row-major порядке.
     * 
     * @param m двумерный массив 4×4 (не должен быть null)
     * @throws IllegalArgumentException если массив null или имеет неправильный размер
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
     * Создает матрицу из одномерного массива.
     * 
     * <p>Массив должен содержать 16 элементов в row-major порядке:
     * [row0col0, row0col1, row0col2, row0col3, row1col0, ...]
     * 
     * @param m одномерный массив из 16 элементов (не должен быть null)
     * @throws IllegalArgumentException если массив null или имеет неправильный размер
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
     * Создает глубокую копию матрицы.
     * 
     * <p>Все элементы копируются, изменения в новой матрице не влияют на исходную.
     * 
     * @param other исходная матрица для копирования (не должна быть null)
     * @throws IllegalArgumentException если other равен null
     */
    public Matrix4f(Matrix4f other) {
        requireNonNull(other, "Matrix");
        matrix = new float[SIZE * SIZE];
        System.arraycopy(other.matrix, 0, matrix, 0, SIZE * SIZE);
    }

    /**
     * Устанавливает матрицу в единичную.
     * 
     * <p>Единичная матрица имеет единицы на главной диагонали и нули в остальных местах.
     * При умножении на вектор не изменяет его.
     */
    public void setIdentity() {
        setZero();
        for (int i = 0; i < SIZE; i++) {
            matrix[i * SIZE + i] = 1.0f;
        }
    }

    /**
     * Устанавливает все элементы матрицы в ноль.
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
     * Получает значение элемента матрицы по индексам строки и столбца.
     * 
     * @param row номер строки (0-3)
     * @param col номер столбца (0-3)
     * @return значение элемента матрицы
     * @throws IndexOutOfBoundsException если индексы выходят за границы [0, 3]
     */
    public float get(int row, int col) {
        validateIndices(row, col);
        return matrix[row * SIZE + col];
    }

    /**
     * Устанавливает значение элемента матрицы по индексам строки и столбца.
     * 
     * @param row номер строки (0-3)
     * @param col номер столбца (0-3)
     * @param value новое значение элемента
     * @throws IndexOutOfBoundsException если индексы выходят за границы [0, 3]
     */
    public void set(int row, int col, float value) {
        validateIndices(row, col);
        matrix[row * SIZE + col] = value;
    }

    /**
     * Выполняет сложение матриц (покомпонентно).
     * 
     * <p>Создает новую матрицу, исходные матрицы не изменяются.
     * 
     * @param other матрица для сложения (не должна быть null)
     * @return новая матрица - результат сложения
     * @throws IllegalArgumentException если other равен null
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
     * Выполняет вычитание матриц (покомпонентно).
     * 
     * <p>Создает новую матрицу, исходные матрицы не изменяются.
     * 
     * @param other матрица для вычитания (не должна быть null)
     * @return новая матрица - результат вычитания
     * @throws IllegalArgumentException если other равен null
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
     * Выполняет умножение матриц.
     * 
     * <p>Умножает текущую матрицу на другую: result = this * other.
     * Создает новую матрицу, исходные матрицы не изменяются.
     * 
     * <p>Для векторов-столбцов порядок умножения важен:
     * (A * B) * v = A * (B * v) означает, что сначала применяется B, потом A.
     * 
     * @param other матрица для умножения справа (не должна быть null)
     * @return новая матрица - результат умножения
     * @throws IllegalArgumentException если other равен null
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
     * Умножает матрицу на вектор-столбец.
     * 
     * <p>Выполняет стандартное матричное умножение: result = matrix * vector.
     * Создает новый вектор, исходный вектор не изменяется.
     * 
     * <p>Формула: result[i] = sum(matrix[i][j] * vector[j]) для всех j.
     * 
     * @param vector вектор-столбец для умножения (не должен быть null)
     * @return новый вектор - результат умножения
     * @throws IllegalArgumentException если vector равен null
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
     * Умножает матрицу на трехмерный вектор, преобразуя его в однородные координаты.
     * 
     * <p>Автоматически добавляет w=1 к трехмерному вектору перед умножением.
     * Используется для преобразования точек (не направлений) в 3D пространстве.
     * 
     * @param vector трехмерный вектор (точка) для умножения (не должен быть null)
     * @return новый четырехмерный вектор - результат умножения
     * @throws IllegalArgumentException если vector равен null
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
     * Транспонирует матрицу (меняет строки и столбцы местами).
     * 
     * <p>Создает новую матрицу, исходная матрица не изменяется.
     * Транспонированная матрица: result[i][j] = this[j][i].
     * 
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
     * Вычисляет определитель матрицы 4×4.
     * 
     * <p>Использует разложение по первой строке (метод Лапласа).
     * Определитель используется для проверки обратимости матрицы.
     * 
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
     * Вычисляет обратную матрицу.
     * 
     * <p>Использует метод присоединенной матрицы (adjugate matrix).
     * Обратная матрица A^(-1) удовлетворяет: A * A^(-1) = I (единичная матрица).
     * 
     * <p>Матрица должна быть обратимой (определитель не равен нулю).
     * 
     * @return новая обратная матрица
     * @throws ArithmeticException если матрица вырожденная (определитель равен нулю)
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

