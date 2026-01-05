package com.cgvsu.render_engine;

import com.cgvsu.math.*;

public class GraphicConveyor {

    public static Matrix4f rotateScaleTranslate() {
        return Matrix4f.identity();
    }

    public static Matrix4f createTranslationMatrix(Vector3f translation) {
        Matrix4f result = new Matrix4f();
        result.setIdentity();
        result.m03 = translation.x;
        result.m13 = translation.y;
        result.m23 = translation.z;
        return result;
    }

    public static Matrix4f createRotationMatrix(Vector3f rotationDegrees) {
        // Конвертируем градусы в радианы
        float rx = (float) Math.toRadians(rotationDegrees.x);
        float ry = (float) Math.toRadians(rotationDegrees.y);
        float rz = (float) Math.toRadians(rotationDegrees.z);

        // Матрицы вращения вокруг каждой оси
        Matrix4f rotX = new Matrix4f();
        rotX.setIdentity();
        rotX.m11 = (float) Math.cos(rx);
        rotX.m12 = -(float) Math.sin(rx);
        rotX.m21 = (float) Math.sin(rx);
        rotX.m22 = (float) Math.cos(rx);

        Matrix4f rotY = new Matrix4f();
        rotY.setIdentity();
        rotY.m00 = (float) Math.cos(ry);
        rotY.m02 = (float) Math.sin(ry);
        rotY.m20 = -(float) Math.sin(ry);
        rotY.m22 = (float) Math.cos(ry);

        Matrix4f rotZ = new Matrix4f();
        rotZ.setIdentity();
        rotZ.m00 = (float) Math.cos(rz);
        rotZ.m01 = -(float) Math.sin(rz);
        rotZ.m10 = (float) Math.sin(rz);
        rotZ.m11 = (float) Math.cos(rz);

        // Комбинируем: сначала Z, потом Y, потом X
        Matrix4f result = new Matrix4f(rotZ);
        result.mul(rotY);
        result.mul(rotX);
        return result;
    }

    public static Matrix4f createScaleMatrix(Vector3f scale) {
        Matrix4f result = new Matrix4f();
        result.setIdentity();
        result.m00 = scale.x;
        result.m11 = scale.y;
        result.m22 = scale.z;
        return result;
    }

    public static Matrix4f createModelMatrix(Vector3f position, Vector3f rotation, Vector3f scale) {
        // Порядок: Scale -> Rotation -> Translation
        Matrix4f scaleMatrix = createScaleMatrix(scale);
        Matrix4f rotationMatrix = createRotationMatrix(rotation);
        Matrix4f translationMatrix = createTranslationMatrix(position);

        Matrix4f result = new Matrix4f(translationMatrix);
        result.mul(rotationMatrix);
        result.mul(scaleMatrix);
        return result;
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        // Вычисляем базис камеры
        Vector3f z = target.subtract(eye).normalize();
        Vector3f x = up.cross(z).normalize();
        Vector3f y = z.cross(x).normalize();

        // Матрица переноса Tv (перемещение начала координат в позицию камеры)
        Matrix4f translation = Matrix4f.identity();
        translation.set(0, 3, -eye.x);
        translation.set(1, 3, -eye.y);
        translation.set(2, 3, -eye.z);

        // Матрица проекции Pv (проекция на оси камеры)
        // Для векторов-столбцов базисные векторы записываются в столбцы
        Matrix4f projection = Matrix4f.identity();
        projection.set(0, 0, x.x);
        projection.set(1, 0, x.y);
        projection.set(2, 0, x.z);
        projection.set(0, 1, y.x);
        projection.set(1, 1, y.y);
        projection.set(2, 1, y.z);
        projection.set(0, 2, z.x);
        projection.set(1, 2, z.y);
        projection.set(2, 2, z.z);

        // V = Pv * Tv
        return projection.multiply(translation);
    }

    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        Matrix4f result = Matrix4f.zero();
        float tanFov = (float) Math.tan(fov * 0.5F);
        
        // По формуле из задания для векторов-столбцов
        result.set(0, 0, 1.0f / (tanFov * aspectRatio));
        result.set(1, 1, 1.0f / tanFov);
        result.set(2, 2, (farPlane + nearPlane) / (farPlane - nearPlane));
        result.set(2, 3, 1.0f);
        result.set(3, 2, 2.0f * nearPlane * farPlane / (nearPlane - farPlane));
        
        return result;
    }

    /**
     * Преобразует однородные координаты в экранные координаты
     * @param vertex однородные координаты (после нормализации, в диапазоне [-1, 1])
     * @param width ширина экрана
     * @param height высота экрана
     * @return точка на экране
     */
    public static Point2f vertexToPoint(final Vector4f vertex, final int width, final int height) {
        // vertex уже нормализован (x/w, y/w, z/w)
        // Преобразуем из диапазона [-1, 1] в экранные координаты
        float x = vertex.x * width / 2.0f + width / 2.0f;
        float y = -vertex.y * height / 2.0f + height / 2.0f; // инвертируем Y
        return new Point2f(x, y);
    }
}
