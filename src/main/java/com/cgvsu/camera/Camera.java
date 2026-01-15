package com.cgvsu.camera;

import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Matrix4f;

/**
 * Базовый класс камеры для 3D сцены.
 * 
 * <p>Представляет виртуальную камеру с:
 * <ul>
 *   <li>Позицией в мировом пространстве</li>
 *   <li>Точкой, на которую смотрит камера (target)</li>
 *   <li>Параметрами перспективной проекции (FOV, aspect ratio, near/far planes)</li>
 * </ul>
 * 
 * <p>Предоставляет матрицы преобразования:
 * <ul>
 *   <li>View Matrix - преобразование из мирового пространства в пространство камеры</li>
 *   <li>Projection Matrix - перспективная проекция на экран</li>
 * </ul>
 * 
 * <p>Все методы возвращают копии векторов для предотвращения изменения извне.
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class Camera {

    /**
     * Создает новую камеру.
     * 
     * @param position позиция камеры в мировом пространстве
     * @param target точка, на которую смотрит камера
     * @param fov поле зрения в радианах
     * @param aspectRatio соотношение сторон (ширина / высота)
     * @param nearPlane расстояние до ближней плоскости отсечения
     * @param farPlane расстояние до дальней плоскости отсечения
     */
    public Camera(
            final Vector3f position,
            final Vector3f target,
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        this.position = new Vector3f(position);
        this.target = new Vector3f(target);
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
    }

    /**
     * Устанавливает позицию камеры.
     */
    public void setPosition(final Vector3f position) {
        this.position = new Vector3f(position);
    }

    /**
     * Устанавливает точку, на которую смотрит камера.
     */
    public void setTarget(final Vector3f target) {
        this.target = new Vector3f(target);
    }

    /**
     * Устанавливает соотношение сторон.
     */
    public void setAspectRatio(final float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    /**
     * Получает позицию камеры (копию).
     */
    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    /**
     * Получает точку, на которую смотрит камера (копию).
     */
    public Vector3f getTarget() {
        return new Vector3f(target);
    }

    /**
     * Перемещает позицию камеры на указанный вектор.
     */
    public void movePosition(final Vector3f translation) {
        this.position = this.position.add(translation);
    }

    /**
     * Перемещает точку, на которую смотрит камера, на указанный вектор.
     */
    public void moveTarget(final Vector3f translation) {
        this.target = this.target.add(translation);
    }

    /**
     * Получает матрицу вида (view matrix).
     * Преобразует координаты из мирового пространства в пространство камеры.
     */
    public Matrix4f getViewMatrix() {
        return CameraView.lookAt(position, target);
    }

    /**
     * Получает матрицу проекции (projection matrix).
     * Преобразует координаты из пространства камеры в пространство экрана.
     */
    public Matrix4f getProjectionMatrix() {
        return CameraProjection.perspective(fov, aspectRatio, nearPlane, farPlane);
    }

    private Vector3f position;
    private Vector3f target;
    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;
}

