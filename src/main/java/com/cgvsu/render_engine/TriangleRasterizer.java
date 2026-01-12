package com.cgvsu.render_engine;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

/**
 * Класс для растеризации треугольников с интерполяцией цвета
 * Основан на алгоритме Брезенхема для растеризации рёбер
 * и барицентрических координатах для интерполяции цвета
 */
public class TriangleRasterizer {

    private static final boolean USE_BARYCENTRIC_COLOR = true;

    /**
     * Заливает треугольник с интерполяцией цвета
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
            double x0,
            double y0,
            Color c0,
            double x1,
            double y1,
            Color c1,
            double x2,
            double y2,
            Color c2
    ) {
        PixelWriter writer = gc.getPixelWriter();
        if (writer == null) return;

        int width = (int) gc.getCanvas().getWidth();
        int height = (int) gc.getCanvas().getHeight();

        // Проверка координат на разумные значения (защита от зависания)
        // Отбрасываем треугольники с координатами вне разумных пределов
        double maxCoord = Math.max(Math.abs(x0), Math.max(Math.abs(x1), Math.max(Math.abs(x2),
                     Math.max(Math.abs(y0), Math.max(Math.abs(y1), Math.abs(y2))))));
        if (maxCoord > width * 20 || maxCoord > height * 20 || Double.isNaN(maxCoord) || Double.isInfinite(maxCoord)) {
            return;
        }

        double triangleArea = computeTriangleArea(x0, y0, x1, y1, x2, y2);
        boolean isDegenerate = Math.abs(triangleArea) < 1e-8;

        if (isDegenerate) {
            drawLineBresenham(writer, x0, y0, c0, x1, y1, c1, x2, y2, c2, width, height);
            return;
        }

        int minX = (int) Math.floor(Math.min(x0, Math.min(x1, x2)));
        int maxX = (int) Math.ceil (Math.max(x0, Math.max(x1, x2)));
        int minY = (int) Math.floor(Math.min(y0, Math.min(y1, y2)));
        int maxY = (int) Math.ceil (Math.max(y0, Math.max(y1, y2)));

        // Отбрасываем треугольники полностью вне экрана (frustum culling)
        if (maxX < 0 || minX >= width || maxY < 0 || minY >= height) {
            return;
        }

        // Отбрасываем треугольники с недопустимо большими размерами (защита от зависания)
        // Треугольники больше чем в 10 раз размер экрана считаются некорректными
        int triangleWidth = maxX - minX;
        int triangleHeight = maxY - minY;
        if (triangleWidth > width * 10 || triangleHeight > height * 10) {
            return;
        }

        if (minY > maxY) {
            return;
        }

        minY = Math.max(0, minY);
        maxY = Math.min(height - 1, maxY);

        int rows = maxY - minY + 1;

        // Fix for NegativeArraySizeException: ensure rows is not negative or zero
        if (rows <= 0) {
            return;
        }

        double[] leftX     = new double[rows];
        double[] rightX    = new double[rows];
        Color[]  leftColor = new Color[rows];
        Color[]  rightColor= new Color[rows];

        for (int i = 0; i < rows; i++) {
            leftX[i] = Double.POSITIVE_INFINITY;
            rightX[i] = Double.NEGATIVE_INFINITY;
            leftColor[i] = null;
            rightColor[i] = null;
        }

        rasterizeEdgeBresenham(x0, y0, c0, x1, y1, c1, leftX, rightX, leftColor, rightColor, minY, maxY);
        rasterizeEdgeBresenham(x1, y1, c1, x2, y2, c2, leftX, rightX, leftColor, rightColor, minY, maxY);
        rasterizeEdgeBresenham(x2, y2, c2, x0, y0, c0, leftX, rightX, leftColor, rightColor, minY, maxY);

        for (int y = minY; y <= maxY; y++) {
            int idx = y - minY;

            if (Double.isInfinite(leftX[idx]) || Double.isInfinite(rightX[idx])) {
                continue;
            }

            int xStart = (int) Math.ceil(leftX[idx]);
            int xEnd   = (int) Math.floor(rightX[idx]);
            if (xEnd < xStart) {
                continue;
            }

            xStart = Math.max(0, xStart);
            xEnd = Math.min(width - 1, xEnd);
            if (xEnd < xStart) {
                continue;
            }

            if (!USE_BARYCENTRIC_COLOR) {
                int span = xEnd - xStart;
                for (int x = xStart; x <= xEnd; x++) {
                    double t = (span == 0) ? 0.0 : (double) (x - xStart) / span;
                    Color c = interpolateColor(leftColor[idx], rightColor[idx], t);
                    writer.setColor(x, y, c);
                }
            } else {
                for (int x = xStart; x <= xEnd; x++) {
                    double px = x;
                    double py = y;

                    double[] bc = computeBarycentric(px, py, x0, y0, x1, y1, x2, y2, triangleArea);

                    double alpha = bc[0];
                    double beta  = bc[1];
                    double gamma = bc[2];

                    double r = alpha * c0.getRed()   + beta * c1.getRed()   + gamma * c2.getRed();
                    double g = alpha * c0.getGreen() + beta * c1.getGreen() + gamma * c2.getGreen();
                    double b = alpha * c0.getBlue()  + beta * c1.getBlue()  + gamma * c2.getBlue();

                    r = clampTo01(r);
                    g = clampTo01(g);
                    b = clampTo01(b);

                    writer.setColor(x, y, new Color(r, g, b, 1.0));
                }
            }
        }
    }

    private static Color interpolateColor(Color c0, Color c1, double t) {
        double r = c0.getRed()   * (1.0 - t) + c1.getRed()   * t;
        double g = c0.getGreen() * (1.0 - t) + c1.getGreen() * t;
        double b = c0.getBlue()  * (1.0 - t) + c1.getBlue()  * t;
        double a = c0.getOpacity() * (1.0 - t) + c1.getOpacity() * t;
        return new Color(r, g, b, a);
    }

    private static double computeTriangleArea(
            double x0, double y0,
            double x1, double y1,
            double x2, double y2
    ) {
        return (y1 - y2) * (x0 - x2)
             + (x2 - x1) * (y0 - y2);
    }

    private static double[] computeBarycentric(
            double px,
            double py,
            double x0,
            double y0,
            double x1,
            double y1,
            double x2,
            double y2,
            double triangleArea
    ) {
        double alpha = ((y1 - y2) * (px - x2)
                      + (x2 - x1) * (py - y2)) / triangleArea;
        double beta  = ((y2 - y0) * (px - x2)
                      + (x0 - x2) * (py - y2)) / triangleArea;
        double gamma = 1.0 - alpha - beta;
        return new double[] { alpha, beta, gamma };
    }

    private static double clampTo01(double v) {
        if (v < 0.0) return 0.0;
        if (v > 1.0) return 1.0;
        return v;
    }

    private static void rasterizeEdgeBresenham(
            double x0d,
            double y0d,
            Color c0,
            double x1d,
            double y1d,
            Color c1,
            double[] leftX,
            double[] rightX,
            Color[] leftColor,
            Color[] rightColor,
            int minY,
            int maxY
    ) {
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
            if (y0 >= minY && y0 <= maxY) {
                int idx = y0 - minY;
                Color c = c0;
                if (x0 < leftX[idx]) {
                    leftX[idx] = x0;
                    leftColor[idx] = c;
                }
                if (x0 > rightX[idx]) {
                    rightX[idx] = x0;
                    rightColor[idx] = c;
                }
            }
            return;
        }

        int err = dx > dy ? dx / 2 : -dy / 2;
        int x = x0;
        int y = y0;

        // Защита от бесконечного цикла: ограничение максимального количества итераций
        // Ограничиваем максимальное количество итераций разумным значением (например, 100000)
        int maxIterations = Math.min(Math.max(dx, dy) + 1000, 100000);
        int iterationCount = 0;
        int prevX = x, prevY = y;
        int stuckCount = 0;

        for (int i = 0; ; i++) {
            if (y >= minY && y <= maxY) {
                int idx = y - minY;
                if (idx >= 0 && idx < leftX.length) {
                    double t = steps > 0 ? (double) i / steps : 0.0;
                    Color c = interpolateColor(c0, c1, t);

                    if (x < leftX[idx]) {
                        leftX[idx] = x;
                        leftColor[idx] = c;
                    }
                    if (x > rightX[idx]) {
                        rightX[idx] = x;
                        rightColor[idx] = c;
                    }
                }
            }

            // Проверка достижения цели
            if (x == x1 && y == y1) {
                break;
            }

            // Защита от бесконечного цикла - проверка, что мы действительно движемся
            if (x == prevX && y == prevY) {
                stuckCount++;
                if (stuckCount > 10) {
                    break; // Застряли, выходим
                }
            } else {
                stuckCount = 0;
                prevX = x;
                prevY = y;
            }

            // Защита от бесконечного цикла - максимальное количество итераций
            iterationCount++;
            if (iterationCount > maxIterations) {
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

    private static void drawLineBresenham(
            PixelWriter writer,
            double x0, double y0, Color c0,
            double x1, double y1, Color c1,
            double x2, double y2, Color c2,
            int width, int height
    ) {
        double[] pointsX = {x0, x1, x2};
        double[] pointsY = {y0, y1, y2};
        Color[] colors = {c0, c1, c2};

        int minIdx = 0, maxIdx = 0;
        double maxDist = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = i + 1; j < 3; j++) {
                double dist = Math.sqrt(
                    Math.pow(pointsX[i] - pointsX[j], 2) + 
                    Math.pow(pointsY[i] - pointsY[j], 2)
                );
                if (dist > maxDist) {
                    maxDist = dist;
                    minIdx = i;
                    maxIdx = j;
                }
            }
        }

        int x0i = (int) Math.round(pointsX[minIdx]);
        int y0i = (int) Math.round(pointsY[minIdx]);
        int x1i = (int) Math.round(pointsX[maxIdx]);
        int y1i = (int) Math.round(pointsY[maxIdx]);

        int dx = Math.abs(x1i - x0i);
        int dy = Math.abs(y1i - y0i);
        int sx = (x0i < x1i) ? 1 : -1;
        int sy = (y0i < y1i) ? 1 : -1;

        int steps = Math.max(dx, dy);
        if (steps == 0) {
            if (x0i >= 0 && x0i < width && y0i >= 0 && y0i < height) {
                writer.setColor(x0i, y0i, colors[minIdx]);
            }
            return;
        }

        int err = dx > dy ? dx / 2 : -dy / 2;
        int x = x0i;
        int y = y0i;

        for (int i = 0; ; i++) {
            if (x >= 0 && x < width && y >= 0 && y < height) {
                double t = (double) i / steps;
                Color c = interpolateColor(colors[minIdx], colors[maxIdx], t);
                writer.setColor(x, y, c);
            }

            if (x == x1i && y == y1i) {
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
