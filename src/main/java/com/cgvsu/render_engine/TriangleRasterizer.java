package com.cgvsu.render_engine;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

/**
 * Оптимизированный класс для растеризации треугольников с интерполяцией цвета.
 * 
 * Основные оптимизации:
 * - Адаптивный выбор режима рендеринга (быстрый/точный)
 * - Оптимизированный алгоритм растеризации ребер
 * - Эффективное использование памяти
 * - Защита от зависания при больших треугольниках
 */
public class TriangleRasterizer {

    // Константы для оптимизации
    private static final double EPSILON = 1e-8;
    private static final int MAX_PIXELS_PER_TRIANGLE = 500000; // ~700x700 пикселей
    private static final int FAST_MODE_THRESHOLD = 1000; // пикселей в строке
    private static final double LARGE_TRIANGLE_THRESHOLD = 0.1; // 10% площади экрана
    
    // Пороги для проверки координат (защита от зависания)
    private static final double MAX_COORD_MULTIPLIER = 50.0; // координаты не более 50x размер экрана
    
    /**
     * Заливает треугольник с интерполяцией цвета.
     * 
     * @param gc GraphicsContext для отрисовки
     * @param x0, y0 координаты первой вершины
     * @param c0 цвет первой вершины
     * @param x1, y1 координаты второй вершины
     * @param c1 цвет второй вершины
     * @param x2, y2 координаты третьей вершины
     * @param c2 цвет третьей вершины
     */
    public static void fillTriangle(
            GraphicsContext gc,
            double x0, double y0, Color c0,
            double x1, double y1, Color c1,
            double x2, double y2, Color c2
    ) {
        PixelWriter writer = gc.getPixelWriter();
        if (writer == null) return;

        int width = (int) gc.getCanvas().getWidth();
        int height = (int) gc.getCanvas().getHeight();

        // Быстрая проверка на валидность координат
        if (!validateCoordinates(x0, y0, x1, y1, x2, y2, width, height)) {
            return;
        }

        // Вычисляем площадь треугольника (используется для барицентрических координат)
        double triangleArea = computeTriangleArea(x0, y0, x1, y1, x2, y2);
        
        // Проверка на вырожденный треугольник
        if (Math.abs(triangleArea) < EPSILON) {
            drawDegenerateTriangle(writer, x0, y0, c0, x1, y1, c1, x2, y2, c2, width, height);
            return;
        }

        // Вычисляем bounding box треугольника
        int minX = (int) Math.floor(Math.min(x0, Math.min(x1, x2)));
        int maxX = (int) Math.ceil(Math.max(x0, Math.max(x1, x2)));
        int minY = (int) Math.floor(Math.min(y0, Math.min(y1, y2)));
        int maxY = (int) Math.ceil(Math.max(y0, Math.max(y1, y2)));

        // Frustum culling: отбрасываем треугольники полностью вне экрана
        if (maxX < 0 || minX >= width || maxY < 0 || minY >= height) {
            return;
        }

        // Ограничиваем область рендеринга границами экрана
        minX = Math.max(0, minX);
        maxX = Math.min(width - 1, maxX);
        minY = Math.max(0, minY);
        maxY = Math.min(height - 1, maxY);

        if (minY > maxY) {
            return;
        }

        int rows = maxY - minY + 1;
        if (rows <= 0) {
            return;
        }

        // Оптимизация: ограничиваем область рендеринга для очень больших треугольников
        rows = limitRenderingArea(minY, maxY, minX, maxX, width, height, rows);
        if (rows <= 0) {
            return;
        }
        
        // Пересчитываем minY после ограничения
        maxY = minY + rows - 1;

        // Растеризуем треугольник
        rasterizeTriangle(writer, x0, y0, c0, x1, y1, c1, x2, y2, c2,
                         triangleArea, minY, maxY, width, height);
    }

    /**
     * Проверяет валидность координат треугольника.
     */
    private static boolean validateCoordinates(
            double x0, double y0, double x1, double y1, double x2, double y2,
            int width, int height) {
        // Проверка на NaN и Infinity
        if (Double.isNaN(x0) || Double.isNaN(y0) || Double.isNaN(x1) || 
            Double.isNaN(y1) || Double.isNaN(x2) || Double.isNaN(y2) ||
            Double.isInfinite(x0) || Double.isInfinite(y0) || Double.isInfinite(x1) ||
            Double.isInfinite(y1) || Double.isInfinite(x2) || Double.isInfinite(y2)) {
            return false;
        }

        // Проверка на разумные значения координат (защита от зависания)
        double maxCoord = Math.max(
            Math.max(Math.abs(x0), Math.abs(x1)), Math.abs(x2)
        );
        double maxCoordY = Math.max(
            Math.max(Math.abs(y0), Math.abs(y1)), Math.abs(y2)
        );
        
        return maxCoord <= width * MAX_COORD_MULTIPLIER && 
               maxCoordY <= height * MAX_COORD_MULTIPLIER;
    }

    /**
     * Ограничивает область рендеринга для очень больших треугольников.
     */
    private static int limitRenderingArea(
            int minY, int maxY, int minX, int maxX,
            int width, int height, int rows) {
        int estimatedPixels = rows * Math.max(maxX - minX, 1);
        
        if (estimatedPixels > MAX_PIXELS_PER_TRIANGLE) {
            // Ограничиваем количество строк
            int maxRows = MAX_PIXELS_PER_TRIANGLE / Math.max(maxX - minX, 1);
            if (maxRows < rows) {
                // Ограничиваем по центру экрана
                int centerY = (minY + maxY) / 2;
                minY = Math.max(0, centerY - maxRows / 2);
                return Math.min(maxRows, height - minY);
            }
        }
        
        return rows;
    }

    /**
     * Основной метод растеризации треугольника.
     */
    private static void rasterizeTriangle(
            PixelWriter writer,
            double x0, double y0, Color c0,
            double x1, double y1, Color c1,
            double x2, double y2, Color c2,
            double triangleArea,
            int minY, int maxY,
            int width, int height) {
        
        int rows = maxY - minY + 1;
        
        // Выделяем память для хранения границ треугольника по строкам
        double[] leftX = new double[rows];
        double[] rightX = new double[rows];
        Color[] leftColor = new Color[rows];
        Color[] rightColor = new Color[rows];

        // Инициализация массивов
        for (int i = 0; i < rows; i++) {
            leftX[i] = Double.POSITIVE_INFINITY;
            rightX[i] = Double.NEGATIVE_INFINITY;
            leftColor[i] = null;
            rightColor[i] = null;
        }

        // Растеризуем три ребра треугольника
        rasterizeEdge(x0, y0, c0, x1, y1, c1, leftX, rightX, leftColor, rightColor, minY, maxY);
        rasterizeEdge(x1, y1, c1, x2, y2, c2, leftX, rightX, leftColor, rightColor, minY, maxY);
        rasterizeEdge(x2, y2, c2, x0, y0, c0, leftX, rightX, leftColor, rightColor, minY, maxY);

        // Заполняем треугольник построчно
        fillTriangleRows(writer, leftX, rightX, leftColor, rightColor,
                        x0, y0, c0, x1, y1, c1, x2, y2, c2,
                        triangleArea, minY, maxY, width, height);
    }

    /**
     * Заполняет строки треугольника.
     */
    private static void fillTriangleRows(
            PixelWriter writer,
            double[] leftX, double[] rightX,
            Color[] leftColor, Color[] rightColor,
            double x0, double y0, Color c0,
            double x1, double y1, Color c1,
            double x2, double y2, Color c2,
            double triangleArea,
            int minY, int maxY,
            int width, int height) {
        
        for (int y = minY; y <= maxY; y++) {
            int idx = y - minY;

            // Пропускаем строки без данных
            if (Double.isInfinite(leftX[idx]) || Double.isInfinite(rightX[idx])) {
                continue;
            }

            int xStart = (int) Math.ceil(leftX[idx]);
            int xEnd = (int) Math.floor(rightX[idx]);
            
            if (xEnd < xStart) {
                continue;
            }

            // Ограничиваем границы экраном
            xStart = Math.max(0, xStart);
            xEnd = Math.min(width - 1, xEnd);
            
            if (xEnd < xStart) {
                continue;
            }

            int pixelCount = xEnd - xStart + 1;
            
            // Адаптивный выбор режима рендеринга
            boolean useFastMode = shouldUseFastMode(pixelCount, triangleArea, width, height);
            
            if (useFastMode) {
                // Быстрый режим: линейная интерполяция цвета
                fillRowFast(writer, xStart, xEnd, y, leftColor[idx], rightColor[idx], c0, c1);
            } else {
                // Точный режим: барицентрические координаты
                fillRowPrecise(writer, xStart, xEnd, y,
                              x0, y0, c0, x1, y1, c1, x2, y2, c2, triangleArea);
            }
        }
    }

    /**
     * Определяет, нужно ли использовать быстрый режим рендеринга.
     */
    private static boolean shouldUseFastMode(int pixelCount, double triangleArea, int width, int height) {
        return pixelCount > FAST_MODE_THRESHOLD || 
               triangleArea > width * height * LARGE_TRIANGLE_THRESHOLD;
    }

    /**
     * Быстрое заполнение строки (линейная интерполяция).
     */
    private static void fillRowFast(
            PixelWriter writer,
            int xStart, int xEnd, int y,
            Color leftC, Color rightC, Color defaultC0, Color defaultC1) {
        
        Color leftColor = (leftC != null) ? leftC : defaultC0;
        Color rightColor = (rightC != null) ? rightC : defaultC1;
        
        int span = xEnd - xStart;
        if (span == 0) {
            writer.setColor(xStart, y, leftColor);
            return;
        }

        // Предвычисляем компоненты цветов для оптимизации
        double leftR = leftColor.getRed();
        double leftG = leftColor.getGreen();
        double leftB = leftColor.getBlue();
        double rightR = rightColor.getRed();
        double rightG = rightColor.getGreen();
        double rightB = rightColor.getBlue();
        
        // Линейная интерполяция цвета
        for (int x = xStart; x <= xEnd; x++) {
            double t = (double) (x - xStart) / span;
            double r = leftR * (1.0 - t) + rightR * t;
            double g = leftG * (1.0 - t) + rightG * t;
            double b = leftB * (1.0 - t) + rightB * t;
            
            writer.setColor(x, y, new Color(r, g, b, 1.0));
        }
    }

    /**
     * Точное заполнение строки (барицентрические координаты).
     */
    private static void fillRowPrecise(
            PixelWriter writer,
            int xStart, int xEnd, int y,
            double x0, double y0, Color c0,
            double x1, double y1, Color c1,
            double x2, double y2, Color c2,
            double triangleArea) {
        
        // Предвычисляем компоненты цветов для оптимизации
        double c0r = c0.getRed();
        double c0g = c0.getGreen();
        double c0b = c0.getBlue();
        double c1r = c1.getRed();
        double c1g = c1.getGreen();
        double c1b = c1.getBlue();
        double c2r = c2.getRed();
        double c2g = c2.getGreen();
        double c2b = c2.getBlue();
        
        // Константы для барицентрических координат (оптимизация)
        double invArea = 1.0 / triangleArea;
        double y1_y2 = y1 - y2;
        double y2_y0 = y2 - y0;
        double x2_x1 = x2 - x1;
        double x0_x2 = x0 - x2;
        double py_y2 = y - y2;
        
        for (int x = xStart; x <= xEnd; x++) {
            double px_x2 = x - x2;
            
            // Вычисляем барицентрические координаты
            double alpha = (y1_y2 * px_x2 + x2_x1 * py_y2) * invArea;
            double beta = (y2_y0 * px_x2 + x0_x2 * py_y2) * invArea;
            double gamma = 1.0 - alpha - beta;

            // Интерполируем цвет
            double r = alpha * c0r + beta * c1r + gamma * c2r;
            double g = alpha * c0g + beta * c1g + gamma * c2g;
            double b = alpha * c0b + beta * c1b + gamma * c2b;

            // Ограничиваем значения [0, 1]
            r = Math.max(0.0, Math.min(1.0, r));
            g = Math.max(0.0, Math.min(1.0, g));
            b = Math.max(0.0, Math.min(1.0, b));

            writer.setColor(x, y, new Color(r, g, b, 1.0));
        }
    }

    /**
     * Растеризует одно ребро треугольника алгоритмом Брезенхема.
     */
    private static void rasterizeEdge(
            double x0d, double y0d, Color c0,
            double x1d, double y1d, Color c1,
            double[] leftX, double[] rightX,
            Color[] leftColor, Color[] rightColor,
            int minY, int maxY) {
        
        int x0 = (int) Math.round(x0d);
        int y0 = (int) Math.round(y0d);
        int x1 = (int) Math.round(x1d);
        int y1 = (int) Math.round(y1d);

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        
        // Обработка вырожденного случая (точка)
        if (dx == 0 && dy == 0) {
            if (y0 >= minY && y0 <= maxY) {
                int idx = y0 - minY;
                if (idx >= 0 && idx < leftX.length) {
                    updateEdgeBounds(x0, c0, leftX, rightX, leftColor, rightColor, idx);
                }
            }
            return;
        }

        int sx = (x0 < x1) ? 1 : -1;
        int sy = (y0 < y1) ? 1 : -1;
        int steps = Math.max(dx, dy);
        
        // Защита от очень длинных ребер
        if (steps > 100000) {
            return;
        }

        int err = dx > dy ? dx / 2 : -dy / 2;
        int x = x0;
        int y = y0;
        int iterationCount = 0;
        int prevX = x, prevY = y;
        int stuckCount = 0;

        for (int i = 0; i <= steps; i++) {
            if (y >= minY && y <= maxY) {
                int idx = y - minY;
                if (idx >= 0 && idx < leftX.length) {
                    double t = (steps > 0) ? (double) i / steps : 0.0;
                    Color c = interpolateColor(c0, c1, t);
                    updateEdgeBounds(x, c, leftX, rightX, leftColor, rightColor, idx);
                }
            }

            // Проверка достижения цели
            if (x == x1 && y == y1) {
                break;
            }

            // Защита от застревания
            if (x == prevX && y == prevY) {
                stuckCount++;
                if (stuckCount > 10) {
                    break;
                }
            } else {
                stuckCount = 0;
                prevX = x;
                prevY = y;
            }

            // Защита от бесконечного цикла
            iterationCount++;
            if (iterationCount > steps + 100) {
                break;
            }

            // Алгоритм Брезенхема
            int e2 = err;
            if (e2 > -dx) {
                err -= dy;
                x += sx;
            }
            if (e2 < dy) {
                err += dx;
                y += sy;
            }
        }
    }

    /**
     * Обновляет границы ребра для строки.
     */
    private static void updateEdgeBounds(
            int x, Color c,
            double[] leftX, double[] rightX,
            Color[] leftColor, Color[] rightColor,
            int idx) {
        if (x < leftX[idx]) {
            leftX[idx] = x;
            leftColor[idx] = c;
        }
        if (x > rightX[idx]) {
            rightX[idx] = x;
            rightColor[idx] = c;
        }
    }

    /**
     * Интерполирует цвет между двумя цветами.
     */
    private static Color interpolateColor(Color c0, Color c1, double t) {
        double r = c0.getRed() * (1.0 - t) + c1.getRed() * t;
        double g = c0.getGreen() * (1.0 - t) + c1.getGreen() * t;
        double b = c0.getBlue() * (1.0 - t) + c1.getBlue() * t;
        double a = c0.getOpacity() * (1.0 - t) + c1.getOpacity() * t;
        return new Color(r, g, b, a);
    }

    /**
     * Вычисляет площадь треугольника (удвоенную, со знаком).
     */
    private static double computeTriangleArea(
            double x0, double y0,
            double x1, double y1,
            double x2, double y2) {
        return (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2);
    }

    /**
     * Рисует вырожденный треугольник (линия или точка).
     */
    private static void drawDegenerateTriangle(
            PixelWriter writer,
            double x0, double y0, Color c0,
            double x1, double y1, Color c1,
            double x2, double y2, Color c2,
            int width, int height) {
        
        // Находим две самые удаленные точки
        double dist01 = distanceSquared(x0, y0, x1, y1);
        double dist02 = distanceSquared(x0, y0, x2, y2);
        double dist12 = distanceSquared(x1, y1, x2, y2);
        
        double maxDist = Math.max(dist01, Math.max(dist02, dist12));
        
        if (maxDist == dist01) {
            drawLine(writer, x0, y0, c0, x1, y1, c1, width, height);
        } else if (maxDist == dist02) {
            drawLine(writer, x0, y0, c0, x2, y2, c2, width, height);
        } else {
            drawLine(writer, x1, y1, c1, x2, y2, c2, width, height);
        }
    }

    /**
     * Вычисляет квадрат расстояния между двумя точками.
     */
    private static double distanceSquared(double x0, double y0, double x1, double y1) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        return dx * dx + dy * dy;
    }

    /**
     * Рисует линию алгоритмом Брезенхема.
     */
    private static void drawLine(
            PixelWriter writer,
            double x0d, double y0d, Color c0,
            double x1d, double y1d, Color c1,
            int width, int height) {
        
        int x0 = (int) Math.round(x0d);
        int y0 = (int) Math.round(y0d);
        int x1 = (int) Math.round(x1d);
        int y1 = (int) Math.round(y1d);

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = (x0 < x1) ? 1 : -1;
        int sy = (y0 < y1) ? 1 : -1;

        int steps = Math.max(dx, dy);
        if (steps == 0) {
            if (x0 >= 0 && x0 < width && y0 >= 0 && y0 < height) {
                writer.setColor(x0, y0, c0);
            }
            return;
        }

        int err = dx > dy ? dx / 2 : -dy / 2;
        int x = x0;
        int y = y0;

        for (int i = 0; i <= steps; i++) {
            if (x >= 0 && x < width && y >= 0 && y < height) {
                double t = (double) i / steps;
                Color c = interpolateColor(c0, c1, t);
                writer.setColor(x, y, c);
            }

            if (x == x1 && y == y1) {
                break;
            }

            int e2 = err;
            if (e2 > -dx) {
                err -= dy;
                x += sx;
            }
            if (e2 < dy) {
                err += dx;
                y += sy;
            }
        }
    }
}
