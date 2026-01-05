package com.cgvsu.render_engine;

import com.cgvsu.render_engine.transformations.*;

/**
 * Построитель аффинных преобразований (паттерн Builder)
 */
public class AffineBuilder implements AffineBuilderInterface {
    private CompositeTransformation composite;

    public AffineBuilder() {
        this.composite = new CompositeTransformation();
    }

    public AffineBuilder scale(float sx, float sy, float sz) {
        composite.add(new ScaleTransformation(sx, sy, sz));
        return this;
    }

    @Override
    public AffineBuilder scale(Axis axis, float value) {
        switch (axis) {
            case X -> this.scaleX(value);
            case Y -> this.scaleY(value);
            case Z -> this.scaleZ(value);
            default -> throw new IllegalArgumentException("Неизвестная ось");
        }
        return this;
    }

    @Override
    public AffineBuilder scaleX(float scaleX) {
        composite.add(new ScaleTransformation(scaleX, 1, 1));
        return this;
    }

    @Override
    public AffineBuilder scaleY(float scaleY) {
        composite.add(new ScaleTransformation(1, scaleY, 1));
        return this;
    }

    @Override
    public AffineBuilder scaleZ(float scaleZ) {
        composite.add(new ScaleTransformation(1, 1, scaleZ));
        return this;
    }

    @Override
    public AffineBuilder scaleUniform(float uniformScale) {
        composite.add(new ScaleTransformation(uniformScale));
        return this;
    }

    @Override
    public AffineBuilder rotateX(float rotateX) {
        composite.add(new RotateTransformation(Axis.X, rotateX));
        return this;
    }

    @Override
    public AffineBuilder rotateXQuat(float rotateX) {
        composite.add(new RotateTransformationOnQuad(Axis.X, rotateX));
        return this;
    }

    @Override
    public AffineBuilder rotateY(float rotateY) {
        composite.add(new RotateTransformation(Axis.Y, rotateY));
        return this;
    }

    @Override
    public AffineBuilder rotateYQuat(float rotateY) {
        composite.add(new RotateTransformationOnQuad(Axis.Y, rotateY));
        return this;
    }

    @Override
    public AffineBuilder rotateZ(float rotateZ) {
        composite.add(new RotateTransformation(Axis.Z, rotateZ));
        return this;
    }

    @Override
    public AffineBuilder rotateZQuat(float rotateZ) {
        composite.add(new RotateTransformationOnQuad(Axis.Z, rotateZ));
        return this;
    }

    @Override
    public AffineBuilder rotate(Axis axis, float rotate) {
        switch (axis) {
            case X -> this.rotateX(rotate);
            case Y -> this.rotateY(rotate);
            case Z -> this.rotateZ(rotate);
            default -> throw new IllegalArgumentException("Неизвестная ось");
        }
        return this;
    }

    @Override
    public AffineBuilder rotateQuat(Axis axis, float rotate) {
        switch (axis) {
            case X -> this.rotateXQuat(rotate);
            case Y -> this.rotateYQuat(rotate);
            case Z -> this.rotateZQuat(rotate);
            default -> throw new IllegalArgumentException("Неизвестная ось");
        }
        return this;
    }

    @Override
    public AffineBuilder translateX(float translateX) {
        composite.add(new TranslationTransformation(translateX, 0, 0));
        return this;
    }

    @Override
    public AffineBuilder translateY(float translateY) {
        composite.add(new TranslationTransformation(0, translateY, 0));
        return this;
    }

    @Override
    public AffineBuilder translateZ(float translateZ) {
        composite.add(new TranslationTransformation(0, 0, translateZ));
        return this;
    }

    @Override
    public AffineBuilder translate(Axis axis, float value) {
        switch (axis) {
            case X -> this.translateX(value);
            case Y -> this.translateY(value);
            case Z -> this.translateZ(value);
            default -> throw new IllegalArgumentException("Неизвестная ось");
        }
        return this;
    }

    @Override
    public AffineBuilder translate(float tx, float ty, float tz) {
        composite.add(new TranslationTransformation(tx, ty, tz));
        return this;
    }

    @Override
    public Transformation build() {
        return composite;
    }

    @Override
    public SaveTransformation saveState() {
        return new SaveTransformation(this.composite.getMatrix());
    }

    @Override
    public AffineBuilder restoreState(SaveTransformation transformation) {
        if (transformation == null) {
            throw new IllegalArgumentException("Transformation не может быть null");
        }
        this.composite = new CompositeTransformation();
        composite.add(transformation);
        return this;
    }
}

