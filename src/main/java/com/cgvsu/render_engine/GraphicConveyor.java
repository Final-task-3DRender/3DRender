package com.cgvsu.render_engine;

import com.cgvsu.math.Point2f;
import com.cgvsu.math.Vector4f;

/**
 * Утилиты для конвертации координат в графическом конвейере.
 */
public class GraphicConveyor {

    public static Point2f vertexToPoint(final Vector4f vertex, final int width, final int height) {
        if (vertex == null) {
            throw new IllegalArgumentException("Vertex cannot be null");
        }
        
        float x = vertex.x * width / 2.0f + width / 2.0f;
        float y = -vertex.y * height / 2.0f + height / 2.0f;
        return new Point2f(x, y);
    }
}
