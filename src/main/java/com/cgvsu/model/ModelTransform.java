package com.cgvsu.model;

import com.cgvsu.math.Vector3f;

/**
 * Класс для хранения и управления трансформациями 3D модели.
 * 
 * <p>Содержит три компонента трансформации:
 * <ul>
 *   <li>Позиция (position) - смещение модели в мировом пространстве</li>
 *   <li>Вращение (rotation) - углы поворота вокруг осей X, Y, Z в градусах</li>
 *   <li>Масштаб (scale) - масштабирование по осям X, Y, Z</li>
 * </ul>
 * 
 * <p>Все методы возвращают копии векторов для предотвращения изменения извне.
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class ModelTransform {
    
    /**
     * Позиция модели в мировом пространстве.
     */
    private Vector3f position;
    
    /**
     * Углы вращения вокруг осей X, Y, Z в градусах.
     */
    private Vector3f rotation;
    
    /**
     * Масштаб модели по осям X, Y, Z.
     * Значение (1, 1, 1) означает отсутствие масштабирования.
     */
    private Vector3f scale;

    /**
     * Создает трансформацию с начальными значениями:
     * позиция (0, 0, 0), вращение (0, 0, 0), масштаб (1, 1, 1).
     */
    public ModelTransform() {
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);
    }

    public ModelTransform(Vector3f position, Vector3f rotation, Vector3f scale) {
        this.position = new Vector3f(position);
        this.rotation = new Vector3f(rotation);
        this.scale = new Vector3f(scale);
    }

    /**
     * Возвращает текущую позицию модели (копию).
     * 
     * @return вектор позиции
     */
    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    /**
     * Устанавливает новую позицию модели.
     * 
     * @param position новая позиция (не должна быть null)
     */
    public void setPosition(Vector3f position) {
        this.position = new Vector3f(position);
    }

    /**
     * Смещает модель на указанный вектор.
     * 
     * @param translation вектор смещения
     */
    public void translate(Vector3f translation) {
        this.position = this.position.add(translation);
    }

    /**
     * Возвращает текущее вращение модели в градусах (копию).
     * 
     * @return вектор углов вращения вокруг осей X, Y, Z
     */
    public Vector3f getRotation() {
        return new Vector3f(rotation);
    }

    /**
     * Устанавливает новое вращение модели.
     * 
     * @param rotation углы вращения в градусах вокруг осей X, Y, Z (не должен быть null)
     */
    public void setRotation(Vector3f rotation) {
        this.rotation = new Vector3f(rotation);
    }

    /**
     * Добавляет вращение к текущему.
     * 
     * @param rotation дополнительные углы вращения в градусах
     */
    public void rotate(Vector3f rotation) {
        this.rotation = this.rotation.add(rotation);
    }

    /**
     * Возвращает текущий масштаб модели (копию).
     * 
     * @return вектор масштаба по осям X, Y, Z
     */
    public Vector3f getScale() {
        return new Vector3f(scale);
    }

    /**
     * Устанавливает новый масштаб модели.
     * 
     * @param scale масштаб по осям X, Y, Z (не должен быть null)
     */
    public void setScale(Vector3f scale) {
        this.scale = new Vector3f(scale);
    }

    /**
     * Умножает текущий масштаб на указанный (комбинирует масштабирования).
     * 
     * @param scale множитель масштаба
     */
    public void scale(Vector3f scale) {
        this.scale = new Vector3f(
                this.scale.x * scale.x,
                this.scale.y * scale.y,
                this.scale.z * scale.z
        );
    }

    /**
     * Сбрасывает все трансформации к начальным значениям:
     * позиция (0, 0, 0), вращение (0, 0, 0), масштаб (1, 1, 1).
     */
    public void reset() {
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);
    }
}

