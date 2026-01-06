package com.cgvsu.render_engine;

import com.cgvsu.math.*;

public class GraphicConveyor {

    public static Matrix4f rotateScaleTranslate() {
        return Matrix4f.identity();
    }

    public static Matrix4f createTranslationMatrix(Vector3f translation) {
        Matrix4f result = Matrix4f.identity();
        result.set(0, 3, translation.x);
        result.set(1, 3, translation.y);
        result.set(2, 3, translation.z);
        return result;
    }

    public static Matrix4f createRotationMatrix(Vector3f rotationDegrees) {
        float rx = (float) Math.toRadians(rotationDegrees.x);
        float ry = (float) Math.toRadians(rotationDegrees.y);
        float rz = (float) Math.toRadians(rotationDegrees.z);

        Matrix4f rotX = Matrix4f.identity();
        rotX.set(1, 1, (float) Math.cos(rx));
        rotX.set(1, 2, -(float) Math.sin(rx));
        rotX.set(2, 1, (float) Math.sin(rx));
        rotX.set(2, 2, (float) Math.cos(rx));

        Matrix4f rotY = Matrix4f.identity();
        rotY.set(0, 0, (float) Math.cos(ry));
        rotY.set(0, 2, (float) Math.sin(ry));
        rotY.set(2, 0, -(float) Math.sin(ry));
        rotY.set(2, 2, (float) Math.cos(ry));

        Matrix4f rotZ = Matrix4f.identity();
        rotZ.set(0, 0, (float) Math.cos(rz));
        rotZ.set(0, 1, -(float) Math.sin(rz));
        rotZ.set(1, 0, (float) Math.sin(rz));
        rotZ.set(1, 1, (float) Math.cos(rz));

        return rotX.multiply(rotY).multiply(rotZ);
    }

    public static Matrix4f createScaleMatrix(Vector3f scale) {
        Matrix4f result = Matrix4f.identity();
        result.set(0, 0, scale.x);
        result.set(1, 1, scale.y);
        result.set(2, 2, scale.z);
        return result;
    }

    public static Matrix4f createModelMatrix(Vector3f position, Vector3f rotation, Vector3f scale) {
        Matrix4f scaleMatrix = createScaleMatrix(scale);
        Matrix4f rotationMatrix = createRotationMatrix(rotation);
        Matrix4f translationMatrix = createTranslationMatrix(position);

        return translationMatrix.multiply(rotationMatrix).multiply(scaleMatrix);
    }

    // Функции lookAt и perspective перенесены в пакет camera:
    // - CameraView.lookAt() - для создания матрицы вида
    // - CameraProjection.perspective() - для создания матрицы проекции

    public static Point2f vertexToPoint(final Vector4f vertex, final int width, final int height) {
        float x = vertex.x * width / 2.0f + width / 2.0f;
        float y = -vertex.y * height / 2.0f + height / 2.0f;
        return new Point2f(x, y);
    }
}
