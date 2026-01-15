package com.cgvsu.render_engine;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Класс для работы с текстурами.
 * Загружает изображение и предоставляет доступ к пикселям по UV координатам.
 */
public class Texture {
    private final int width;
    private final int height;
    private final Color[] pixels;
    
    /**
     * Создает текстуру из массива пикселей.
     * 
     * @param width ширина текстуры
     * @param height высота текстуры
     * @param pixels массив пикселей (размер должен быть width * height)
     */
    private Texture(int width, int height, Color[] pixels) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }
    
    /**
     * Загружает текстуру из файла.
     * 
     * @param filePath путь к файлу изображения
     * @return загруженная текстура
     * @throws IOException если не удалось загрузить изображение
     */
    public static Texture loadFromFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("Texture file not found: " + filePath);
        }
        
        try (InputStream is = new FileInputStream(file)) {
            Image image = new Image(is);
            return loadFromImage(image);
        }
    }
    
    /**
     * Загружает текстуру из JavaFX Image.
     * 
     * @param image изображение
     * @return загруженная текстура
     */
    public static Texture loadFromImage(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        
        PixelReader pixelReader = image.getPixelReader();
        Color[] pixels = new Color[width * height];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = pixelReader.getColor(x, y);
            }
        }
        
        return new Texture(width, height, pixels);
    }
    
    /**
     * Получает цвет пикселя по UV координатам.
     * UV координаты должны быть в диапазоне [0, 1].
     * Используется clamp для обработки координат вне диапазона.
     * 
     * @param u координата U (горизонтальная, 0-1)
     * @param v координата V (вертикальная, 0-1)
     * @return цвет пикселя
     */
    public Color getPixel(float u, float v) {
        u = Math.max(0.0f, Math.min(1.0f, u));
        v = Math.max(0.0f, Math.min(1.0f, v));
        
        int x = (int) (u * (width - 1));
        int y = (int) ((1.0f - v) * (height - 1));
        
        x = Math.max(0, Math.min(width - 1, x));
        y = Math.max(0, Math.min(height - 1, y));
        
        return pixels[y * width + x];
    }
    
    /**
     * Получает цвет пикселя по UV координатам с билинейной интерполяцией.
     * Более качественная выборка, но медленнее.
     * 
     * @param u координата U (горизонтальная, 0-1)
     * @param v координата V (вертикальная, 0-1)
     * @return интерполированный цвет пикселя
     */
    public Color getPixelBilinear(float u, float v) {
        u = Math.max(0.0f, Math.min(1.0f, u));
        v = Math.max(0.0f, Math.min(1.0f, v));
        
        float fx = u * (width - 1);
        float fy = (1.0f - v) * (height - 1);
        
        int x0 = (int) fx;
        int y0 = (int) fy;
        int x1 = Math.min(width - 1, x0 + 1);
        int y1 = Math.min(height - 1, y0 + 1);
        
        float fracX = fx - x0;
        float fracY = fy - y0;
        
        Color c00 = pixels[y0 * width + x0];
        Color c10 = pixels[y0 * width + x1];
        Color c01 = pixels[y1 * width + x0];
        Color c11 = pixels[y1 * width + x1];
        
        Color c0 = interpolateColor(c00, c10, fracX);
        Color c1 = interpolateColor(c01, c11, fracX);
        return interpolateColor(c0, c1, fracY);
    }
    
    /**
     * Интерполирует цвет между двумя цветами.
     */
    private Color interpolateColor(Color c0, Color c1, float t) {
        double r = c0.getRed() * (1.0 - t) + c1.getRed() * t;
        double g = c0.getGreen() * (1.0 - t) + c1.getGreen() * t;
        double b = c0.getBlue() * (1.0 - t) + c1.getBlue() * t;
        double a = c0.getOpacity() * (1.0 - t) + c1.getOpacity() * t;
        return new Color(r, g, b, a);
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    /**
     * Проверяет, загружена ли текстура.
     */
    public boolean isValid() {
        return pixels != null && width > 0 && height > 0;
    }
}
