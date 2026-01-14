package com.cgvsu.render_engine;

import java.util.Arrays;

/**
 * Z-buffer (буфер глубины) для правильной отрисовки 3D объектов.
 * Хранит глубину (Z-координату) для каждого пикселя экрана.
 */
public class ZBuffer {
    private final float[] buffer;
    private final int width;
    private final int height;
    
    // Специальное значение для неинициализированных пикселей (быстрее чем Float.MAX_VALUE)
    private static final float UNINITIALIZED = 1e10f;
    
    /**
     * Создает новый Z-buffer заданного размера.
     * 
     * @param width ширина экрана
     * @param height высота экрана
     */
    public ZBuffer(int width, int height) {
        this.width = width;
        this.height = height;
        this.buffer = new float[width * height];
        clear();
    }
    
    /**
     * Очищает Z-buffer, устанавливая все значения в максимальную глубину (дальше всего от камеры).
     */
    public void clear() {
        // Используем Arrays.fill для быстрой очистки
        // UNINITIALIZED - специальное значение, чтобы первый пиксель всегда проходил проверку
        Arrays.fill(buffer, UNINITIALIZED);
    }
    
    /**
     * Проверяет, нужно ли рисовать пиксель на заданной позиции.
     * Согласно алгоритму Z-buffer:
     * if (z_buffer(x, y) <= z) continue; // не рисуем
     * put_pixel(x, y);
     * z_buffer(x, y) = z;
     * 
     * То есть, рисуем только если z < z_buffer(x, y) (пиксель ближе к камере).
     * 
     * @param x координата X пикселя
     * @param y координата Y пикселя
     * @param z глубина (Z-координата) пикселя в NDC пространстве [-1, 1]
     * @return true если пиксель нужно нарисовать (он ближе), false иначе
     */
    public boolean testAndSet(int x, int y, float z) {
        // Проверка границ
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        
        // Проверка валидности z
        if (Float.isNaN(z) || Float.isInfinite(z)) {
            return false;
        }
        
        int index = y * width + x;
        float currentZ = buffer[index];
        
        // Если это первый пиксель в этой позиции, всегда рисуем его
        if (currentZ == UNINITIALIZED) {
            buffer[index] = z;
            return true;
        }
        
        // Согласно алгоритму Z-buffer: рисуем только если z < z_buffer(x, y)
        // В NDC пространстве после перспективной проекции:
        // - z = -1 соответствует near plane (ближе к камере)
        // - z = 1 соответствует far plane (дальше от камеры)
        // Поэтому меньшие значения z означают меньшую глубину (ближе к камере)
        if (z < currentZ) {
            buffer[index] = z;
            return true;
        }
        
        // Если z >= currentZ, пиксель дальше от камеры, не рисуем
        return false;
    }
    
    /**
     * Получает текущую глубину пикселя.
     * 
     * @param x координата X
     * @param y координата Y
     * @return глубина пикселя или UNINITIALIZED если координаты вне границ или пиксель не был нарисован
     */
    public float get(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return UNINITIALIZED;
        }
        return buffer[y * width + x];
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
}
