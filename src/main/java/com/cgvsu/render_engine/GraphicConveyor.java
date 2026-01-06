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

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        // Вычисляем направление камеры (forward vector) - от камеры к цели
        Vector3f forward = target.subtract(eye);
        float forwardLength = forward.length();
        if (forwardLength < 1e-6f) {
            // Если камера находится в точке цели, возвращаем единичную матрицу
            return Matrix4f.identity();
        }
        forward = forward.normalize();
        
        // Для левосторонней системы координат (JavaFX):
        // Вычисляем правый вектор (right vector) = up × forward
        Vector3f right = up.cross(forward);
        float rightLength = right.length();
        if (rightLength < 1e-6f) {
            // Если forward и up коллинеарны, используем альтернативный up
            if (Math.abs(forward.y) > 0.9f) {
                up = new Vector3f(0, 0, 1);
            } else {
                up = new Vector3f(0, 1, 0);
            }
            right = up.cross(forward);
            rightLength = right.length();
            if (rightLength < 1e-6f) {
                // Если все еще коллинеарны, используем единичную матрицу
                return Matrix4f.identity();
            }
        }
        right = right.normalize();
        
        // Вычисляем истинный up вектор = forward × right (для левосторонней системы)
        Vector3f upCorrected = forward.cross(right).normalize();

        // Создаем матрицу поворота (rotation matrix)
        // В OpenGL/JavaFX используется column-major порядок, но здесь row-major
        Matrix4f rotation = Matrix4f.identity();
        // Первая строка - right vector (X ось камеры)
        rotation.set(0, 0, right.x);
        rotation.set(0, 1, right.y);
        rotation.set(0, 2, right.z);
        // Вторая строка - up vector (Y ось камеры)
        rotation.set(1, 0, upCorrected.x);
        rotation.set(1, 1, upCorrected.y);
        rotation.set(1, 2, upCorrected.z);
        // Третья строка - отрицательный forward vector (Z ось камеры, смотрит по -Z)
        rotation.set(2, 0, -forward.x);
        rotation.set(2, 1, -forward.y);
        rotation.set(2, 2, -forward.z);

        // Создаем матрицу переноса (перемещаем камеру в начало координат)
        Matrix4f translation = Matrix4f.identity();
        translation.set(0, 3, -eye.x);
        translation.set(1, 3, -eye.y);
        translation.set(2, 3, -eye.z);

        // Комбинируем: сначала поворот, потом перенос
        // View = Rotation * Translation
        return rotation.multiply(translation);
    }

    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        Matrix4f result = Matrix4f.zero();
        
        // FOV должен быть в радианах, если передается в градусах, нужно преобразовать
        // Предполагаем, что fov уже в радианах, но если это не так, можно добавить проверку
        float fovRad = fov;
        // Если fov > 10, вероятно это градусы, преобразуем
        if (fov > 10.0f) {
            fovRad = (float) Math.toRadians(fov);
        }
        
        float tanHalfFov = (float) Math.tan(fovRad * 0.5F);
        
        if (tanHalfFov < 1e-6f || aspectRatio < 1e-6f || Math.abs(farPlane - nearPlane) < 1e-6f) {
            throw new IllegalArgumentException("Invalid perspective parameters");
        }
        
        result.set(0, 0, 1.0f / (tanHalfFov * aspectRatio));
        result.set(1, 1, 1.0f / tanHalfFov);
        result.set(2, 2, (farPlane + nearPlane) / (farPlane - nearPlane));
        result.set(2, 3, 1.0f);
        result.set(3, 2, -2.0f * nearPlane * farPlane / (farPlane - nearPlane));
        
        return result;
    }

    public static Point2f vertexToPoint(final Vector4f vertex, final int width, final int height) {
        float x = vertex.x * width / 2.0f + width / 2.0f;
        float y = -vertex.y * height / 2.0f + height / 2.0f;
        return new Point2f(x, y);
    }
}
