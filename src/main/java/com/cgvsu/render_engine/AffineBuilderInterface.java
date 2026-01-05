package com.cgvsu.render_engine;

import com.cgvsu.render_engine.transformations.Axis;
import com.cgvsu.render_engine.transformations.SaveTransformation;
import com.cgvsu.render_engine.transformations.Transformation;

/**
 * Интерфейс для построителя аффинных преобразований
 */
public interface AffineBuilderInterface {
    Transformation build();
    SaveTransformation saveState();
    AffineBuilder restoreState(SaveTransformation transformation);

    AffineBuilder scaleX(float scaleX);
    AffineBuilder scaleY(float scaleY);
    AffineBuilder scaleZ(float scaleZ);
    AffineBuilder scaleUniform(float uniformScale);
    AffineBuilder scale(float scaleX, float scaleY, float scaleZ);
    AffineBuilder scale(Axis axis, float value);
    AffineBuilder rotateX(float rotateX);
    AffineBuilder rotateXQuat(float rotateX);
    AffineBuilder rotateY(float rotateY);
    AffineBuilder rotateYQuat(float rotateY);
    AffineBuilder rotateZ(float rotateZ);
    AffineBuilder rotateZQuat(float rotateZ);
    AffineBuilder rotate(Axis axis, float rotate);
    AffineBuilder rotateQuat(Axis axis, float rotate);
    AffineBuilder translateX(float translateX);
    AffineBuilder translateY(float translateY);
    AffineBuilder translateZ(float translateZ);
    AffineBuilder translate(Axis axis, float value);
    AffineBuilder translate(float translateX, float translateY, float translateZ);
}

