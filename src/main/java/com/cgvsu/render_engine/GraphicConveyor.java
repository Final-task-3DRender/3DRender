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
        Vector3f z = target.subtract(eye).normalize();
        Vector3f x = up.cross(z).normalize();
        Vector3f y = z.cross(x).normalize();

        Matrix4f translation = Matrix4f.identity();
        translation.set(0, 3, -eye.x);
        translation.set(1, 3, -eye.y);
        translation.set(2, 3, -eye.z);

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

        return projection.multiply(translation);
    }

    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        Matrix4f result = Matrix4f.zero();
        float tanFov = (float) Math.tan(fov * 0.5F);
        
        result.set(0, 0, 1.0f / (tanFov * aspectRatio));
        result.set(1, 1, 1.0f / tanFov);
        result.set(2, 2, (farPlane + nearPlane) / (farPlane - nearPlane));
        result.set(2, 3, 1.0f);
        result.set(3, 2, 2.0f * nearPlane * farPlane / (nearPlane - farPlane));
        
        return result;
    }

    public static Point2f vertexToPoint(final Vector4f vertex, final int width, final int height) {
        float x = vertex.x * width / 2.0f + width / 2.0f;
        float y = -vertex.y * height / 2.0f + height / 2.0f;
        return new Point2f(x, y);
    }
}
