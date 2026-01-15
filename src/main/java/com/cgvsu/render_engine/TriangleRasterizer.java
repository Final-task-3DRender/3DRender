package com.cgvsu.render_engine;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

/**
 * Оптимизированный класс для растеризации треугольников с интерполяцией цвета, Z-buffer и текстур.
 * 
 * <p>Реализует алгоритм растеризации треугольников с поддержкой:
 * <ul>
 *   <li>Perspective-correct interpolation для Z и UV координат</li>
 *   <li>Z-buffer для правильной отрисовки глубины</li>
 *   <li>Текстуры с билинейной интерполяцией</li>
 *   <li>Интерполяцию цвета между вершинами</li>
 * </ul>
 * 
 * <p>Основные оптимизации:
 * <ul>
 *   <li>Адаптивный выбор режима рендеринга (быстрый/точный) в зависимости от размера треугольника</li>
 *   <li>Оптимизированный алгоритм растеризации ребер (алгоритм Брезенхема)</li>
 *   <li>Эффективное использование памяти</li>
 *   <li>Защита от зависания при больших треугольниках (ограничение области рендеринга)</li>
 *   <li>Frustum culling (отбрасывание треугольников вне экрана)</li>
 * </ul>
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class TriangleRasterizer {

    private static final double EPSILON = 1e-8;
    private static final int MAX_PIXELS_PER_TRIANGLE = 500000;
    private static final int FAST_MODE_THRESHOLD = 1000;
    private static final double LARGE_TRIANGLE_THRESHOLD = 0.1;
    
    private static final double MAX_COORD_MULTIPLIER = 50.0;
    
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
        fillTriangle(gc, null, null, x0, y0, 0.0f, 1.0f, 0.0f, 0.0f, c0, 
                     x1, y1, 0.0f, 1.0f, 0.0f, 0.0f, c1, 
                     x2, y2, 0.0f, 1.0f, 0.0f, 0.0f, c2);
    }
    
    /**
     * Заливает треугольник с интерполяцией цвета и поддержкой Z-buffer.
     * 
     * @param gc GraphicsContext для отрисовки
     * @param zBuffer Z-buffer для проверки глубины (может быть null)
     * @param x0, y0, z0 координаты и глубина первой вершины
     * @param c0 цвет первой вершины
     * @param x1, y1, z1 координаты и глубина второй вершины
     * @param c1 цвет второй вершины
     * @param x2, y2, z2 координаты и глубина третьей вершины
     * @param c2 цвет третьей вершины
     */
    public static void fillTriangle(
            GraphicsContext gc,
            ZBuffer zBuffer,
            double x0, double y0, float z0,
            Color c0,
            double x1, double y1, float z1,
            Color c1,
            double x2, double y2, float z2,
            Color c2
    ) {
        fillTriangle(gc, zBuffer, null, x0, y0, z0, 1.0f, 0.0f, 0.0f, c0, 
                     x1, y1, z1, 1.0f, 0.0f, 0.0f, c1, 
                     x2, y2, z2, 1.0f, 0.0f, 0.0f, c2);
    }
    
    /**
     * Заливает треугольник с интерполяцией цвета, поддержкой Z-buffer и текстур.
     * 
     * @param gc GraphicsContext для отрисовки
     * @param zBuffer Z-buffer для проверки глубины (может быть null)
     * @param texture текстура для наложения (может быть null)
     * @param x0, y0, z0 координаты и глубина (z/w) первой вершины
     * @param invW0 обратное значение w (1/w) первой вершины для perspective-correct interpolation
     * @param u0, v0 UV координаты первой вершины
     * @param c0 цвет первой вершины
     * @param x1, y1, z1 координаты и глубина (z/w) второй вершины
     * @param invW1 обратное значение w (1/w) второй вершины для perspective-correct interpolation
     * @param u1, v1 UV координаты второй вершины
     * @param c1 цвет второй вершины
     * @param x2, y2, z2 координаты и глубина (z/w) третьей вершины
     * @param invW2 обратное значение w (1/w) третьей вершины для perspective-correct interpolation
     * @param u2, v2 UV координаты третьей вершины
     * @param c2 цвет третьей вершины
     */
    public static void fillTriangle(
            GraphicsContext gc,
            ZBuffer zBuffer,
            Texture texture,
            double x0, double y0, float z0, float invW0, float u0, float v0,
            Color c0,
            double x1, double y1, float z1, float invW1, float u1, float v1,
            Color c1,
            double x2, double y2, float z2, float invW2, float u2, float v2,
            Color c2
    ) {
        PixelWriter writer = gc.getPixelWriter();
        if (writer == null) return;

        int width = (int) gc.getCanvas().getWidth();
        int height = (int) gc.getCanvas().getHeight();

        if (!validateCoordinates(x0, y0, x1, y1, x2, y2, width, height)) {
            return;
        }

        double triangleArea = computeTriangleArea(x0, y0, x1, y1, x2, y2);
        
        if (Math.abs(triangleArea) < EPSILON) {
            drawDegenerateTriangle(writer, zBuffer, texture, x0, y0, z0, invW0, u0, v0, c0, 
                                   x1, y1, z1, invW1, u1, v1, c1, 
                                   x2, y2, z2, invW2, u2, v2, c2, width, height);
            return;
        }

        int minX = (int) Math.floor(Math.min(x0, Math.min(x1, x2)));
        int maxX = (int) Math.ceil(Math.max(x0, Math.max(x1, x2)));
        int minY = (int) Math.floor(Math.min(y0, Math.min(y1, y2)));
        int maxY = (int) Math.ceil(Math.max(y0, Math.max(y1, y2)));

        if (maxX < 0 || minX >= width || maxY < 0 || minY >= height) {
            return;
        }

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

        rows = limitRenderingArea(minY, maxY, minX, maxX, width, height, rows);
        if (rows <= 0) {
            return;
        }
        
        maxY = minY + rows - 1;

        rasterizeTriangle(writer, zBuffer, texture, x0, y0, z0, invW0, u0, v0, c0, 
                          x1, y1, z1, invW1, u1, v1, c1, 
                          x2, y2, z2, invW2, u2, v2, c2,
                         triangleArea, minY, maxY, width, height);
    }

    /**
     * Проверяет валидность координат треугольника.
     */
    private static boolean validateCoordinates(
            double x0, double y0, double x1, double y1, double x2, double y2,
            int width, int height) {
        if (Double.isNaN(x0) || Double.isNaN(y0) || Double.isNaN(x1) || 
            Double.isNaN(y1) || Double.isNaN(x2) || Double.isNaN(y2) ||
            Double.isInfinite(x0) || Double.isInfinite(y0) || Double.isInfinite(x1) ||
            Double.isInfinite(y1) || Double.isInfinite(x2) || Double.isInfinite(y2)) {
            return false;
        }

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
            int maxRows = MAX_PIXELS_PER_TRIANGLE / Math.max(maxX - minX, 1);
            if (maxRows < rows) {
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
            ZBuffer zBuffer,
            Texture texture,
            double x0, double y0, float z0, float invW0, float u0, float v0, Color c0,
            double x1, double y1, float z1, float invW1, float u1, float v1, Color c1,
            double x2, double y2, float z2, float invW2, float u2, float v2, Color c2,
            double triangleArea,
            int minY, int maxY,
            int width, int height) {
        
        int rows = maxY - minY + 1;
        
        double[] leftX = new double[rows];
        double[] rightX = new double[rows];
        Color[] leftColor = new Color[rows];
        Color[] rightColor = new Color[rows];
        
        float[] leftZ = null;
        float[] rightZ = null;
        if (zBuffer != null) {
            leftZ = new float[rows];
            rightZ = new float[rows];
        }
        
        float[] leftU = null;
        float[] leftV = null;
        float[] rightU = null;
        float[] rightV = null;
        float[] leftInvW = null;
        float[] rightInvW = null;
        if (texture != null) {
            leftU = new float[rows];
            leftV = new float[rows];
            rightU = new float[rows];
            rightV = new float[rows];
            leftInvW = new float[rows];
            rightInvW = new float[rows];
        }

        for (int i = 0; i < rows; i++) {
            leftX[i] = Double.POSITIVE_INFINITY;
            rightX[i] = Double.NEGATIVE_INFINITY;
            leftColor[i] = null;
            rightColor[i] = null;
            if (zBuffer != null) {
                leftZ[i] = Float.NEGATIVE_INFINITY;
                rightZ[i] = Float.NEGATIVE_INFINITY;
            }
            if (texture != null) {
                leftU[i] = Float.NaN;
                leftV[i] = Float.NaN;
                rightU[i] = Float.NaN;
                rightV[i] = Float.NaN;
                leftInvW[i] = Float.NaN;
                rightInvW[i] = Float.NaN;
            }
        }

        rasterizeEdge(x0, y0, z0, invW0, u0, v0, c0, x1, y1, z1, invW1, u1, v1, c1, 
                      leftX, rightX, leftColor, rightColor, leftZ, rightZ, 
                      leftU, leftV, rightU, rightV, leftInvW, rightInvW, minY, maxY);
        rasterizeEdge(x1, y1, z1, invW1, u1, v1, c1, x2, y2, z2, invW2, u2, v2, c2, 
                      leftX, rightX, leftColor, rightColor, leftZ, rightZ, 
                      leftU, leftV, rightU, rightV, leftInvW, rightInvW, minY, maxY);
        rasterizeEdge(x2, y2, z2, invW2, u2, v2, c2, x0, y0, z0, invW0, u0, v0, c0, 
                      leftX, rightX, leftColor, rightColor, leftZ, rightZ, 
                      leftU, leftV, rightU, rightV, leftInvW, rightInvW, minY, maxY);

        fillTriangleRows(writer, zBuffer, texture, x0, y0, z0, invW0, u0, v0, c0, 
                         x1, y1, z1, invW1, u1, v1, c1, 
                         x2, y2, z2, invW2, u2, v2, c2,
                         leftX, rightX, leftColor, rightColor, leftZ, rightZ,
                         leftU, leftV, rightU, rightV, leftInvW, rightInvW,
                         triangleArea, minY, maxY, width, height);
    }

    /**
     * Заполняет строки треугольника.
     */
    private static void fillTriangleRows(
            PixelWriter writer,
            ZBuffer zBuffer,
            Texture texture,
            double x0, double y0, float z0, float invW0, float u0, float v0, Color c0,
            double x1, double y1, float z1, float invW1, float u1, float v1, Color c1,
            double x2, double y2, float z2, float invW2, float u2, float v2, Color c2,
            double[] leftX, double[] rightX,
            Color[] leftColor, Color[] rightColor,
            float[] leftZ, float[] rightZ,
            float[] leftU, float[] leftV, float[] rightU, float[] rightV,
            float[] leftInvW, float[] rightInvW,
            double triangleArea,
            int minY, int maxY,
            int width, int height) {
        
        for (int y = minY; y <= maxY; y++) {
            int idx = y - minY;

            if (Double.isInfinite(leftX[idx]) || Double.isInfinite(rightX[idx])) {
                continue;
            }

            int xStart = (int) Math.ceil(leftX[idx]);
            int xEnd = (int) Math.floor(rightX[idx]);
            
            if (xEnd < xStart) {
                continue;
            }

            xStart = Math.max(0, xStart);
            xEnd = Math.min(width - 1, xEnd);
            
            if (xEnd < xStart) {
                continue;
            }

            int pixelCount = xEnd - xStart + 1;
            
            boolean useFastMode = shouldUseFastMode(pixelCount, triangleArea, width, height);
            
            float leftZVal = 0.0f;
            float rightZVal = 0.0f;
            if (zBuffer != null && leftZ != null && rightZ != null) {
                if (leftZ[idx] == Float.NEGATIVE_INFINITY || Float.isNaN(leftZ[idx])) {
                    double leftXVal = leftX[idx];
                    double leftYVal = y;
                    double invArea = 1.0 / triangleArea;
                    double px_x2 = leftXVal - x2;
                    double py_y2 = leftYVal - y2;
                    double alpha = ((y1 - y2) * px_x2 + (x2 - x1) * py_y2) * invArea;
                    double beta = ((y2 - y0) * px_x2 + (x0 - x2) * py_y2) * invArea;
                    double gamma = 1.0 - alpha - beta;
                    
                    double zOverW = alpha * z0 * invW0 + beta * z1 * invW1 + gamma * z2 * invW2;
                    double oneOverW = alpha * invW0 + beta * invW1 + gamma * invW2;
                    if (Math.abs(oneOverW) > 1e-10) {
                        leftZVal = (float) (zOverW / oneOverW);
                    } else {
                        leftZVal = (float) (alpha * z0 + beta * z1 + gamma * z2);
                    }
                } else {
                    leftZVal = leftZ[idx];
                }
                
                if (rightZ[idx] == Float.NEGATIVE_INFINITY || Float.isNaN(rightZ[idx])) {
                    double rightXVal = rightX[idx];
                    double rightYVal = y;
                    double invArea = 1.0 / triangleArea;
                    double px_x2 = rightXVal - x2;
                    double py_y2 = rightYVal - y2;
                    double alpha = ((y1 - y2) * px_x2 + (x2 - x1) * py_y2) * invArea;
                    double beta = ((y2 - y0) * px_x2 + (x0 - x2) * py_y2) * invArea;
                    double gamma = 1.0 - alpha - beta;
                    
                    double zOverW = alpha * z0 * invW0 + beta * z1 * invW1 + gamma * z2 * invW2;
                    double oneOverW = alpha * invW0 + beta * invW1 + gamma * invW2;
                    if (Math.abs(oneOverW) > 1e-10) {
                        rightZVal = (float) (zOverW / oneOverW);
                    } else {
                        rightZVal = (float) (alpha * z0 + beta * z1 + gamma * z2);
                    }
                } else {
                    rightZVal = rightZ[idx];
                }
                
                if (Float.isNaN(leftZVal) || Float.isInfinite(leftZVal)) {
                    leftZVal = (z0 + z1 + z2) / 3.0f;
                }
                if (Float.isNaN(rightZVal) || Float.isInfinite(rightZVal)) {
                    rightZVal = (z0 + z1 + z2) / 3.0f;
                }
            }
            
            float leftUVal = 0.0f, leftVVal = 0.0f, rightUVal = 0.0f, rightVVal = 0.0f;
            float leftInvWVal = 1.0f, rightInvWVal = 1.0f;
            if (texture != null && leftU != null && rightU != null) {
                if (Float.isNaN(leftU[idx]) || Float.isNaN(leftInvW[idx])) {
                    double leftXVal = leftX[idx];
                    double leftYVal = y;
                    double invArea = 1.0 / triangleArea;
                    double px_x2 = leftXVal - x2;
                    double py_y2 = leftYVal - y2;
                    double alpha = ((y1 - y2) * px_x2 + (x2 - x1) * py_y2) * invArea;
                    double beta = ((y2 - y0) * px_x2 + (x0 - x2) * py_y2) * invArea;
                    double gamma = 1.0 - alpha - beta;
                    double uOverW = alpha * (u0 * invW0) + beta * (u1 * invW1) + gamma * (u2 * invW2);
                    double vOverW = alpha * (v0 * invW0) + beta * (v1 * invW1) + gamma * (v2 * invW2);
                    leftInvWVal = (float) (alpha * invW0 + beta * invW1 + gamma * invW2);
                    if (leftInvWVal > 1e-7f) {
                        leftUVal = (float) (uOverW / leftInvWVal);
                        leftVVal = (float) (vOverW / leftInvWVal);
                    } else {
                        leftUVal = u0;
                        leftVVal = v0;
                    }
                } else {
                    leftUVal = leftU[idx];
                    leftVVal = leftV[idx];
                    leftInvWVal = leftInvW[idx];
                }
                
                if (Float.isNaN(rightU[idx]) || Float.isNaN(rightInvW[idx])) {
                    double rightXVal = rightX[idx];
                    double rightYVal = y;
                    double invArea = 1.0 / triangleArea;
                    double px_x2 = rightXVal - x2;
                    double py_y2 = rightYVal - y2;
                    double alpha = ((y1 - y2) * px_x2 + (x2 - x1) * py_y2) * invArea;
                    double beta = ((y2 - y0) * px_x2 + (x0 - x2) * py_y2) * invArea;
                    double gamma = 1.0 - alpha - beta;
                    double uOverW = alpha * (u0 * invW0) + beta * (u1 * invW1) + gamma * (u2 * invW2);
                    double vOverW = alpha * (v0 * invW0) + beta * (v1 * invW1) + gamma * (v2 * invW2);
                    rightInvWVal = (float) (alpha * invW0 + beta * invW1 + gamma * invW2);
                    if (rightInvWVal > 1e-7f) {
                        rightUVal = (float) (uOverW / rightInvWVal);
                        rightVVal = (float) (vOverW / rightInvWVal);
                    } else {
                        rightUVal = u1;
                        rightVVal = v1;
                    }
                } else {
                    rightUVal = rightU[idx];
                    rightVVal = rightV[idx];
                    rightInvWVal = rightInvW[idx];
                }
            }
            
            if (useFastMode) {
                fillRowFast(writer, zBuffer, texture, xStart, xEnd, y, 
                           leftColor[idx], rightColor[idx], 
                           leftZVal, rightZVal,
                           leftUVal, leftVVal, rightUVal, rightVVal,
                           leftInvWVal, rightInvWVal,
                           c0, c1);
            } else {
                fillRowPrecise(writer, zBuffer, texture, xStart, xEnd, y,
                              x0, y0, z0, invW0, u0, v0, c0, 
                              x1, y1, z1, invW1, u1, v1, c1, 
                              x2, y2, z2, invW2, u2, v2, c2, 
                              triangleArea);
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
            ZBuffer zBuffer,
            Texture texture,
            int xStart, int xEnd, int y,
            Color leftC, Color rightC,
            float leftZ, float rightZ,
            float leftU, float leftV, float rightU, float rightV,
            float leftInvW, float rightInvW,
            Color defaultC0, Color defaultC1) {
        
        Color leftColor = (leftC != null) ? leftC : defaultC0;
        Color rightColor = (rightC != null) ? rightC : defaultC1;
        
        int span = xEnd - xStart;
        if (span == 0) {
            Color pixelColor = leftColor;
            if (texture != null) {
                pixelColor = texture.getPixel(leftU, leftV);
            }
            if (zBuffer == null || zBuffer.testAndSet(xStart, y, leftZ)) {
                writer.setColor(xStart, y, pixelColor);
            }
            return;
        }

        double leftR = leftColor.getRed();
        double leftG = leftColor.getGreen();
        double leftB = leftColor.getBlue();
        double rightR = rightColor.getRed();
        double rightG = rightColor.getGreen();
        double rightB = rightColor.getBlue();
        
        if (zBuffer == null) {
            for (int x = xStart; x <= xEnd; x++) {
                double t = (double) (x - xStart) / span;
                Color pixelColor;
                
                if (texture != null) {
                    double invW = leftInvW * (1.0 - t) + rightInvW * t;
                    if (invW > 1e-7) {
                        double uOverW = (leftU * leftInvW) * (1.0 - t) + (rightU * rightInvW) * t;
                        double vOverW = (leftV * leftInvW) * (1.0 - t) + (rightV * rightInvW) * t;
                        float u = (float) (uOverW / invW);
                        float v = (float) (vOverW / invW);
                        pixelColor = texture.getPixel(u, v);
                    } else {
                        float u = (float) (leftU * (1.0 - t) + rightU * t);
                        float v = (float) (leftV * (1.0 - t) + rightV * t);
                        pixelColor = texture.getPixel(u, v);
                    }
                } else {
                    double r = leftR * (1.0 - t) + rightR * t;
                    double g = leftG * (1.0 - t) + rightG * t;
                    double b = leftB * (1.0 - t) + rightB * t;
                    pixelColor = new Color(r, g, b, 1.0);
                }
                
                writer.setColor(x, y, pixelColor);
            }
        } else {
            for (int x = xStart; x <= xEnd; x++) {
                double t = (double) (x - xStart) / span;
                float z = (float) (leftZ * (1.0 - t) + rightZ * t);
                
                if (Float.isNaN(z) || Float.isInfinite(z)) {
                    continue;
                }
                
                if (zBuffer.testAndSet(x, y, z)) {
                    Color pixelColor;
                    
                    if (texture != null) {
                        double invW = leftInvW * (1.0 - t) + rightInvW * t;
                        if (invW > 1e-7) {
                            double uOverW = (leftU * leftInvW) * (1.0 - t) + (rightU * rightInvW) * t;
                            double vOverW = (leftV * leftInvW) * (1.0 - t) + (rightV * rightInvW) * t;
                            float u = (float) (uOverW / invW);
                            float v = (float) (vOverW / invW);
                            pixelColor = texture.getPixel(u, v);
                        } else {
                            float u = (float) (leftU * (1.0 - t) + rightU * t);
                            float v = (float) (leftV * (1.0 - t) + rightV * t);
                            pixelColor = texture.getPixel(u, v);
                        }
                    } else {
                        double r = leftR * (1.0 - t) + rightR * t;
                        double g = leftG * (1.0 - t) + rightG * t;
                        double b = leftB * (1.0 - t) + rightB * t;
                        pixelColor = new Color(r, g, b, 1.0);
                    }
                    
                    writer.setColor(x, y, pixelColor);
                }
            }
        }
    }

    /**
     * Точное заполнение строки (барицентрические координаты с perspective-correct interpolation).
     */
    private static void fillRowPrecise(
            PixelWriter writer,
            ZBuffer zBuffer,
            Texture texture,
            int xStart, int xEnd, int y,
            double x0, double y0, float z0, float invW0, float u0, float v0, Color c0,
            double x1, double y1, float z1, float invW1, float u1, float v1, Color c1,
            double x2, double y2, float z2, float invW2, float u2, float v2, Color c2,
            double triangleArea) {
        
        double c0r = c0.getRed();
        double c0g = c0.getGreen();
        double c0b = c0.getBlue();
        double c1r = c1.getRed();
        double c1g = c1.getGreen();
        double c1b = c1.getBlue();
        double c2r = c2.getRed();
        double c2g = c2.getGreen();
        double c2b = c2.getBlue();
        
        double invArea = 1.0 / triangleArea;
        double y1_y2 = y1 - y2;
        double y2_y0 = y2 - y0;
        double x2_x1 = x2 - x1;
        double x0_x2 = x0 - x2;
        double py_y2 = y - y2;
        
        if (zBuffer == null) {
            for (int x = xStart; x <= xEnd; x++) {
                double px_x2 = x - x2;
                
                double alpha = (y1_y2 * px_x2 + x2_x1 * py_y2) * invArea;
                double beta = (y2_y0 * px_x2 + x0_x2 * py_y2) * invArea;
                double gamma = 1.0 - alpha - beta;

                double r = alpha * c0r + beta * c1r + gamma * c2r;
                double g = alpha * c0g + beta * c1g + gamma * c2g;
                double b = alpha * c0b + beta * c1b + gamma * c2b;

                r = Math.max(0.0, Math.min(1.0, r));
                g = Math.max(0.0, Math.min(1.0, g));
                b = Math.max(0.0, Math.min(1.0, b));

                writer.setColor(x, y, new Color(r, g, b, 1.0));
            }
        } else {
            for (int x = xStart; x <= xEnd; x++) {
                double px_x2 = x - x2;
                
                double alpha = (y1_y2 * px_x2 + x2_x1 * py_y2) * invArea;
                double beta = (y2_y0 * px_x2 + x0_x2 * py_y2) * invArea;
                double gamma = 1.0 - alpha - beta;

                double zOverW = alpha * z0 * invW0 + beta * z1 * invW1 + gamma * z2 * invW2;
                double oneOverW = alpha * invW0 + beta * invW1 + gamma * invW2;
                float z;
                if (Math.abs(oneOverW) > 1e-10) {
                    z = (float) (zOverW / oneOverW);
                } else {
                    z = (float) (alpha * z0 + beta * z1 + gamma * z2);
                }
                
                if (Float.isNaN(z) || Float.isInfinite(z)) {
                    continue;
                }
                
                if (zBuffer.testAndSet(x, y, z)) {
                    Color pixelColor;
                    
                    if (texture != null) {
                        if (Math.abs(oneOverW) > 1e-7) {
                            double uOverW = alpha * (u0 * invW0) + beta * (u1 * invW1) + gamma * (u2 * invW2);
                            double vOverW = alpha * (v0 * invW0) + beta * (v1 * invW1) + gamma * (v2 * invW2);
                            float u = (float) (uOverW / oneOverW);
                            float v = (float) (vOverW / oneOverW);
                            pixelColor = texture.getPixel(u, v);
                        } else {
                            float u = (float) (alpha * u0 + beta * u1 + gamma * u2);
                            float v = (float) (alpha * v0 + beta * v1 + gamma * v2);
                            pixelColor = texture.getPixel(u, v);
                        }
                    } else {
                        double r = alpha * c0r + beta * c1r + gamma * c2r;
                        double g = alpha * c0g + beta * c1g + gamma * c2g;
                        double b = alpha * c0b + beta * c1b + gamma * c2b;

                        r = Math.max(0.0, Math.min(1.0, r));
                        g = Math.max(0.0, Math.min(1.0, g));
                        b = Math.max(0.0, Math.min(1.0, b));

                        pixelColor = new Color(r, g, b, 1.0);
                    }

                    writer.setColor(x, y, pixelColor);
                }
            }
        }
    }

    /**
     * Растеризует одно ребро треугольника алгоритмом Брезенхема.
     */
    private static void rasterizeEdge(
            double x0d, double y0d, float z0, float invW0, float u0, float v0, Color c0,
            double x1d, double y1d, float z1, float invW1, float u1, float v1, Color c1,
            double[] leftX, double[] rightX,
            Color[] leftColor, Color[] rightColor,
            float[] leftZ, float[] rightZ,
            float[] leftU, float[] leftV, float[] rightU, float[] rightV,
            float[] leftInvW, float[] rightInvW,
            int minY, int maxY) {
        
        int x0 = (int) Math.round(x0d);
        int y0 = (int) Math.round(y0d);
        int x1 = (int) Math.round(x1d);
        int y1 = (int) Math.round(y1d);

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        
        if (dx == 0 && dy == 0) {
            if (y0 >= minY && y0 <= maxY) {
                int idx = y0 - minY;
                if (idx >= 0 && idx < leftX.length) {
                    updateEdgeBounds(x0, z0, invW0, u0, v0, c0, leftX, rightX, leftColor, rightColor, 
                                   leftZ, rightZ, leftU, leftV, rightU, rightV, leftInvW, rightInvW, idx);
                }
            }
            return;
        }
        
        boolean useZ = (leftZ != null && rightZ != null);
        boolean useUV = (leftU != null && rightU != null);

        int sx = (x0 < x1) ? 1 : -1;
        int sy = (y0 < y1) ? 1 : -1;
        int steps = Math.max(dx, dy);
        
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
                    float z = 0.0f;
                    if (useZ) {
                        double zOverW = z0 * invW0 * (1.0 - t) + z1 * invW1 * t;
                        double oneOverW = invW0 * (1.0 - t) + invW1 * t;
                        if (Math.abs(oneOverW) > 1e-10) {
                            z = (float) (zOverW / oneOverW);
                        } else {
                            z = (float) (z0 * (1.0 - t) + z1 * t);
                        }
                    }
                    
                    if (useZ && (Float.isNaN(z) || Float.isInfinite(z))) {
                        z = (i < steps / 2) ? z0 : z1;
                    }
                    
                    float u = 0.0f, v = 0.0f, invW = 1.0f;
                    if (useUV) {
                        double uOverW = (u0 * invW0) * (1.0 - t) + (u1 * invW1) * t;
                        double vOverW = (v0 * invW0) * (1.0 - t) + (v1 * invW1) * t;
                        invW = (float) (invW0 * (1.0 - t) + invW1 * t);
                        if (invW > 1e-7f) {
                            u = (float) (uOverW / invW);
                            v = (float) (vOverW / invW);
                        } else {
                            u = (float) (u0 * (1.0 - t) + u1 * t);
                            v = (float) (v0 * (1.0 - t) + v1 * t);
                        }
                    }
                    
                    updateEdgeBounds(x, z, invW, u, v, c, leftX, rightX, leftColor, rightColor, 
                                   leftZ, rightZ, leftU, leftV, rightU, rightV, leftInvW, rightInvW, idx);
                }
            }

            if (x == x1 && y == y1) {
                break;
            }

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

            iterationCount++;
            if (iterationCount > steps + 100) {
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

    /**
     * Обновляет границы ребра для строки.
     */
    private static void updateEdgeBounds(
            int x, float z, float invW, float u, float v, Color c,
            double[] leftX, double[] rightX,
            Color[] leftColor, Color[] rightColor,
            float[] leftZ, float[] rightZ,
            float[] leftU, float[] leftV, float[] rightU, float[] rightV,
            float[] leftInvW, float[] rightInvW,
            int idx) {
        if (x < leftX[idx]) {
            leftX[idx] = x;
            leftColor[idx] = c;
            if (leftZ != null) {
                leftZ[idx] = (leftZ[idx] == Float.NEGATIVE_INFINITY) ? z : Math.max(leftZ[idx], z);
            }
            if (leftU != null && leftV != null && leftInvW != null) {
                if (Float.isNaN(leftU[idx])) {
                    leftU[idx] = u;
                    leftV[idx] = v;
                    leftInvW[idx] = invW;
                } else {
                    if (leftZ != null && z > leftZ[idx]) {
                        leftU[idx] = u;
                        leftV[idx] = v;
                        leftInvW[idx] = invW;
                    }
                }
            }
        }
        if (x > rightX[idx]) {
            rightX[idx] = x;
            rightColor[idx] = c;
            if (rightZ != null) {
                rightZ[idx] = (rightZ[idx] == Float.NEGATIVE_INFINITY) ? z : Math.max(rightZ[idx], z);
            }
            if (rightU != null && rightV != null && rightInvW != null) {
                if (Float.isNaN(rightU[idx])) {
                    rightU[idx] = u;
                    rightV[idx] = v;
                    rightInvW[idx] = invW;
                } else {
                    if (rightZ != null && z > rightZ[idx]) {
                        rightU[idx] = u;
                        rightV[idx] = v;
                        rightInvW[idx] = invW;
                    }
                }
            }
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
            ZBuffer zBuffer,
            Texture texture,
            double x0, double y0, float z0, float invW0, float u0, float v0, Color c0,
            double x1, double y1, float z1, float invW1, float u1, float v1, Color c1,
            double x2, double y2, float z2, float invW2, float u2, float v2, Color c2,
            int width, int height) {
        
        double dist01 = distanceSquared(x0, y0, x1, y1);
        double dist02 = distanceSquared(x0, y0, x2, y2);
        double dist12 = distanceSquared(x1, y1, x2, y2);
        
        double maxDist = Math.max(dist01, Math.max(dist02, dist12));
        
        if (maxDist == dist01) {
            drawLine(writer, zBuffer, texture, x0, y0, z0, invW0, u0, v0, c0, x1, y1, z1, invW1, u1, v1, c1, width, height);
        } else if (maxDist == dist02) {
            drawLine(writer, zBuffer, texture, x0, y0, z0, invW0, u0, v0, c0, x2, y2, z2, invW2, u2, v2, c2, width, height);
        } else {
            drawLine(writer, zBuffer, texture, x1, y1, z1, invW1, u1, v1, c1, x2, y2, z2, invW2, u2, v2, c2, width, height);
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
            ZBuffer zBuffer,
            Texture texture,
            double x0d, double y0d, float z0, float invW0, float u0, float v0, Color c0,
            double x1d, double y1d, float z1, float invW1, float u1, float v1, Color c1,
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
                Color pixelColor = c0;
                if (texture != null) {
                    pixelColor = texture.getPixel(u0, v0);
                }
                if (zBuffer == null) {
                    writer.setColor(x0, y0, pixelColor);
                } else {
                    if (!Float.isNaN(z0) && !Float.isInfinite(z0)) {
                        if (zBuffer.testAndSet(x0, y0, z0)) {
                            writer.setColor(x0, y0, pixelColor);
                        }
                    }
                }
            }
            return;
        }

        int err = dx > dy ? dx / 2 : -dy / 2;
        int x = x0;
        int y = y0;

        for (int i = 0; i <= steps; i++) {
            if (x >= 0 && x < width && y >= 0 && y < height) {
                double t = (double) i / steps;
                Color pixelColor;
                
                if (texture != null) {
                    double invW = invW0 * (1.0 - t) + invW1 * t;
                    if (invW > 1e-7) {
                        double uOverW = (u0 * invW0) * (1.0 - t) + (u1 * invW1) * t;
                        double vOverW = (v0 * invW0) * (1.0 - t) + (v1 * invW1) * t;
                        float u = (float) (uOverW / invW);
                        float v = (float) (vOverW / invW);
                        pixelColor = texture.getPixel(u, v);
                    } else {
                        float u = (float) (u0 * (1.0 - t) + u1 * t);
                        float v = (float) (v0 * (1.0 - t) + v1 * t);
                        pixelColor = texture.getPixel(u, v);
                    }
                } else {
                    pixelColor = interpolateColor(c0, c1, t);
                }
                
                if (zBuffer == null) {
                    writer.setColor(x, y, pixelColor);
                } else {
                    double zOverW = z0 * invW0 * (1.0 - t) + z1 * invW1 * t;
                    double oneOverW = invW0 * (1.0 - t) + invW1 * t;
                    float z;
                    if (Math.abs(oneOverW) > 1e-10) {
                        z = (float) (zOverW / oneOverW);
                    } else {
                        z = (float) (z0 * (1.0 - t) + z1 * t);
                    }
                    
                    if (!Float.isNaN(z) && !Float.isInfinite(z)) {
                        if (zBuffer.testAndSet(x, y, z)) {
                            writer.setColor(x, y, pixelColor);
                        }
                    }
                }
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
