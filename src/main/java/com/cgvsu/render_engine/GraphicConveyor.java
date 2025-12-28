package com.cgvsu.render_engine;
import javax.vecmath.*;

public class GraphicConveyor {

    public static Matrix4f rotateScaleTranslate() {
        float[] matrix = new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1};
        return new Matrix4f(matrix);
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
        Vector3f resultX = new Vector3f();
        Vector3f resultY = new Vector3f();
        Vector3f resultZ = new Vector3f();

        resultZ.sub(target, eye);
        resultX.cross(up, resultZ);
        resultY.cross(resultZ, resultX);

        resultX.normalize();
        resultY.normalize();
        resultZ.normalize();

        float[] matrix = new float[]{
                resultX.x, resultY.x, resultZ.x, 0,
                resultX.y, resultY.y, resultZ.y, 0,
                resultX.z, resultY.z, resultZ.z, 0,
                -resultX.dot(eye), -resultY.dot(eye), -resultZ.dot(eye), 1};
        return new Matrix4f(matrix);
    }

    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        Matrix4f result = new Matrix4f();
        float tangentMinusOnDegree = (float) (1.0F / (Math.tan(fov * 0.5F)));
        result.m00 = tangentMinusOnDegree / aspectRatio;
        result.m11 = tangentMinusOnDegree;
        result.m22 = (farPlane + nearPlane) / (farPlane - nearPlane);
        result.m23 = 1.0F;
        result.m32 = 2 * (nearPlane * farPlane) / (nearPlane - farPlane);
        return result;
    }

    public static Vector3f multiplyMatrix4ByVector3(final Matrix4f matrix, final Vector3f vertex) {
        final float x = (vertex.x * matrix.m00) + (vertex.y * matrix.m10) + (vertex.z * matrix.m20) + matrix.m30;
        final float y = (vertex.x * matrix.m01) + (vertex.y * matrix.m11) + (vertex.z * matrix.m21) + matrix.m31;
        final float z = (vertex.x * matrix.m02) + (vertex.y * matrix.m12) + (vertex.z * matrix.m22) + matrix.m32;
        final float w = (vertex.x * matrix.m03) + (vertex.y * matrix.m13) + (vertex.z * matrix.m23) + matrix.m33;
        return new Vector3f(x / w, y / w, z / w);
    }

    public static Point2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        return new Point2f(vertex.x * width + width / 2.0F, -vertex.y * height + height / 2.0F);
    }
}
