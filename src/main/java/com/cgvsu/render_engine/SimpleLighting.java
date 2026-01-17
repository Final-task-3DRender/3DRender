package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;
import javafx.scene.paint.Color;

public class SimpleLighting {
    
    public static Color apply(Color color, Vector3f normal, Vector3f lightDir, float k) {
        float l = -normal.dot(lightDir);
        if (l < 0) l = 0;
        l = Math.max(0, Math.min(1, l));
        double r = color.getRed() * (1 - k) + color.getRed() * k * l;
        double g = color.getGreen() * (1 - k) + color.getGreen() * k * l;
        double b = color.getBlue() * (1 - k) + color.getBlue() * k * l;
        return new Color(r, g, b, color.getOpacity());
    }
    
    public static Vector3f interpolate(float[] normals, double a, double b, double c) {
        float nx = (float)(a * normals[0] + b * normals[3] + c * normals[6]);
        float ny = (float)(a * normals[1] + b * normals[4] + c * normals[7]);
        float nz = (float)(a * normals[2] + b * normals[5] + c * normals[8]);
        return new Vector3f(nx, ny, nz).normalize();
    }
}
