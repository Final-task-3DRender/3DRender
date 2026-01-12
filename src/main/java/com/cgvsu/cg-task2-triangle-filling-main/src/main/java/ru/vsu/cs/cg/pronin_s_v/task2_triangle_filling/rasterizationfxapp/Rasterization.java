package ru.vsu.cs.cg.pronin_s_v.task2_triangle_filling.rasterizationfxapp;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class Rasterization {

    private static final boolean USE_BARYCENTRIC_COLOR = true;

    public static void drawRectangle(
            final GraphicsContext graphicsContext,
            final int x,
            final int y,
            final int width,
            final int height,
            final Color color
    ) {
        final PixelWriter pixelWriter = graphicsContext.getPixelWriter();

        for (int row = y; row < y + height; ++row) {
            for (int col = x; col < x + width; ++col) {
                pixelWriter.setColor(col, row, color);
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

        for (int i = 0; ; i++) {
            if (y >= minY && y <= maxY) {
                int idx = y - minY;
                double t = (double) i / steps;
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

        System.out.printf("Треугольник:\n  P1=(%.2f, %.2f) цвет: (%.2f, %.2f, %.2f)\n", 
                x0, y0, c0.getRed(), c0.getGreen(), c0.getBlue());
        System.out.printf("  P2=(%.2f, %.2f) цвет: (%.2f, %.2f, %.2f)\n", 
                x1, y1, c1.getRed(), c1.getGreen(), c1.getBlue());
        System.out.printf("  P3=(%.2f, %.2f) цвет: (%.2f, %.2f, %.2f)\n", 
                x2, y2, c2.getRed(), c2.getGreen(), c2.getBlue());

        double triangleArea = computeTriangleArea(x0, y0, x1, y1, x2, y2);
        boolean isDegenerate = Math.abs(triangleArea) < 1e-8;
        
        if (isDegenerate) {
            System.out.println("Комментарий: Точки лежат на одной прямой (вырожденный треугольник)");
        } else {
            System.out.println("Комментарий: Точки не лежат на одной прямой (валидный треугольник)");
        }
        System.out.println();

        if (isDegenerate) {
            drawLineBresenham(writer, x0, y0, c0, x1, y1, c1, x2, y2, c2, width, height);
            return;
        }

        int minY = (int) Math.floor(Math.min(y0, Math.min(y1, y2)));
        int maxY = (int) Math.ceil (Math.max(y0, Math.max(y1, y2)));

        if (minY > maxY) {
            return;
        }

        minY = Math.max(0, minY);
        maxY = Math.min(height - 1, maxY);

        int rows = maxY - minY + 1;

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
