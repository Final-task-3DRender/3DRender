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
    
    private static final float UNINITIALIZED = -1e10f;
    
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
        Arrays.fill(buffer, UNINITIALIZED);
    }
    
    /**
     * Проверяет, нужно ли рисовать пиксель на заданной позиции.
     * 
     * Алгоритм Z-buffer:
     * 1. Если пиксель в этой позиции еще не был нарисован -> рисуем
     * 2. Если новый пиксель ближе к камере, чем уже нарисованный -> рисуем и обновляем Z-buffer
     * 3. Иначе -> не рисуем
     * 
     * ВАЖНО: В нашей системе координат после перспективной проекции:
     * - Ближние объекты имеют БОЛЬШИЕ значения Z (менее отрицательные или положительные)
     * - Дальние объекты имеют МЕНЬШИЕ значения Z (более отрицательные)
     * Поэтому используем сравнение z > currentZ для определения ближайшего пикселя.
     * 
     * @param x координата X пикселя
     * @param y координата Y пикселя
     * @param z глубина (Z-координата) пикселя после перспективной проекции
     * @return true если пиксель нужно нарисовать (он ближе), false иначе
     */
    public boolean testAndSet(int x, int y, float z) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        
        if (Float.isNaN(z) || Float.isInfinite(z)) {
            return false;
        }
        
        int index = y * width + x;
        float currentZ = buffer[index];
        
        if (currentZ == UNINITIALIZED) {
            buffer[index] = z;
            return true;
        }
        
        if (z > currentZ) {
            buffer[index] = z;
            return true;
        }
        
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
    
    /**
     * Возвращает ширину Z-buffer (ширину экрана).
     * 
     * @return ширина в пикселях
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Возвращает высоту Z-buffer (высоту экрана).
     * 
     * @return высота в пикселях
     */
    public int getHeight() {
        return height;
    }
}
