package com.cgvsu.model;

import javax.vecmath.Vector3f;

/**
 * Класс для хранения трансформаций модели (позиция, вращение, масштаб)
 */
public class ModelTransform {
    private Vector3f position;
    private Vector3f rotation; // В градусах
    private Vector3f scale;

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

    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    public void setPosition(Vector3f position) {
        this.position.set(position);
    }

    public void translate(Vector3f translation) {
        this.position.add(translation);
    }

    public Vector3f getRotation() {
        return new Vector3f(rotation);
    }

    public void setRotation(Vector3f rotation) {
        this.rotation.set(rotation);
    }

    public void rotate(Vector3f rotation) {
        this.rotation.add(rotation);
    }

    public Vector3f getScale() {
        return new Vector3f(scale);
    }

    public void setScale(Vector3f scale) {
        this.scale.set(scale);
    }

    public void scale(Vector3f scale) {
        this.scale.x *= scale.x;
        this.scale.y *= scale.y;
        this.scale.z *= scale.z;
    }

    public void reset() {
        position.set(0, 0, 0);
        rotation.set(0, 0, 0);
        scale.set(1, 1, 1);
    }
}

