package com.cgvsu.model;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;

/**
 * Тесты для класса ModelTransformer
 */
class ModelTransformerTest {
    
    private static final float EPSILON = 1e-5f;
    
    /**
     * Создает простую тестовую модель с одной вершиной.
     */
    private Model createSimpleModel() {
        Model model = new Model();
        model.addVertex(new Vector3f(1, 2, 3));
        return model;
    }
    
    /**
     * Создает модель с вершинами и нормалями.
     */
    private Model createModelWithNormals() {
        Model model = new Model();
        model.addVertex(new Vector3f(1, 0, 0));
        model.addVertex(new Vector3f(0, 1, 0));
        model.addVertex(new Vector3f(0, 0, 1));
        
        model.addNormal(new Vector3f(1, 0, 0));
        model.addNormal(new Vector3f(0, 1, 0));
        model.addNormal(new Vector3f(0, 0, 1));
        
        return model;
    }
    
    /**
     * Тест применения единичной матрицы (без изменений).
     */
    @Test
    void testApplyTransformIdentity() {
        Model source = createSimpleModel();
        Matrix4f identity = Matrix4f.identity();
        
        Model result = ModelTransformer.applyTransform(source, identity);
        
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getVertexCount());
        Vector3f vertex = result.getVertex(0);
        Assertions.assertEquals(1.0f, vertex.x, EPSILON);
        Assertions.assertEquals(2.0f, vertex.y, EPSILON);
        Assertions.assertEquals(3.0f, vertex.z, EPSILON);
    }
    
    /**
     * Тест применения матрицы переноса к вершинам.
     */
    @Test
    void testApplyTransformTranslation() {
        Model source = createSimpleModel();
        Vector3f translation = new Vector3f(5, 10, 15);
        Matrix4f transform = com.cgvsu.transform.AffineMatrixFactory.createTranslationMatrix(translation);
        
        Model result = ModelTransformer.applyTransform(source, transform);
        
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getVertexCount());
        // Вершина должна быть перемещена
        Vector3f vertex = result.getVertex(0);
        Assertions.assertEquals(6.0f, vertex.x, EPSILON); // 1 + 5
        Assertions.assertEquals(12.0f, vertex.y, EPSILON); // 2 + 10
        Assertions.assertEquals(18.0f, vertex.z, EPSILON); // 3 + 15
    }
    
    /**
     * Тест применения матрицы масштабирования к вершинам.
     */
    @Test
    void testApplyTransformScale() {
        Model source = createSimpleModel();
        Vector3f scale = new Vector3f(2, 3, 4);
        Matrix4f transform = com.cgvsu.transform.AffineMatrixFactory.createScaleMatrix(scale);
        
        Model result = ModelTransformer.applyTransform(source, transform);
        
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getVertexCount());
        // Вершина должна быть масштабирована
        Vector3f vertex = result.getVertex(0);
        Assertions.assertEquals(2.0f, vertex.x, EPSILON); // 1 * 2
        Assertions.assertEquals(6.0f, vertex.y, EPSILON); // 2 * 3
        Assertions.assertEquals(12.0f, vertex.z, EPSILON); // 3 * 4
    }
    
    /**
     * Тест применения матрицы вращения к вершинам.
     */
    @Test
    void testApplyTransformRotation() {
        Model source = new Model();
        source.addVertex(new Vector3f(1, 0, 0)); // Вектор вдоль оси X
        
        // Вращение на 90 градусов вокруг оси Y
        Vector3f rotation = new Vector3f(0, 90, 0);
        Matrix4f transform = com.cgvsu.transform.AffineMatrixFactory.createRotationMatrix(rotation);
        
        Model result = ModelTransformer.applyTransform(source, transform);
        
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getVertexCount());
        // После поворота на 90° вокруг Y, вектор (1,0,0) должен стать (0,0,-1) в левосторонней системе
        Vector3f transformed = result.getVertex(0);
        Assertions.assertEquals(0.0f, transformed.x, EPSILON);
        Assertions.assertEquals(0.0f, transformed.y, EPSILON);
        Assertions.assertEquals(-1.0f, transformed.z, EPSILON);
    }
    
    /**
     * Тест применения трансформации к нормалям (w=0, без переноса).
     */
    @Test
    void testApplyTransformToNormals() {
        Model source = createModelWithNormals();
        Vector3f translation = new Vector3f(10, 20, 30);
        Vector3f scale = new Vector3f(2, 2, 2);
        Matrix4f transform = com.cgvsu.transform.AffineMatrixFactory.createTranslationMatrix(translation)
                .multiply(com.cgvsu.transform.AffineMatrixFactory.createScaleMatrix(scale));
        
        Model result = ModelTransformer.applyTransform(source, transform);
        
        Assertions.assertNotNull(result);
        Assertions.assertEquals(3, result.getNormalCount());
        
        Vector3f normal0 = result.getNormal(0);
        float length0 = normal0.length();
        Assertions.assertEquals(1.0f, length0, EPSILON, "Normal should be normalized");
        
        Assertions.assertEquals(1.0f, Math.abs(normal0.x), EPSILON);
        Assertions.assertEquals(0.0f, normal0.y, EPSILON);
        Assertions.assertEquals(0.0f, normal0.z, EPSILON);
    }
    
    /**
     * Тест проверки, что исходная модель не изменяется (immutability).
     */
    @Test
    void testImmutability() {
        Model source = createSimpleModel();
        Vector3f originalVertex = source.getVertex(0);
        float originalX = originalVertex.x;
        
        Matrix4f transform = com.cgvsu.transform.AffineMatrixFactory.createTranslationMatrix(new Vector3f(100, 100, 100));
        Model result = ModelTransformer.applyTransform(source, transform);
        
        // Исходная модель не должна измениться
        Assertions.assertEquals(originalX, source.getVertex(0).x, EPSILON,
            "Source model should not be modified");
        
        // Результат должен быть другим
        Assertions.assertNotEquals(originalX, result.getVertex(0).x, EPSILON,
            "Result model should be different");
    }
    
    /**
     * Тест обработки null для source модели.
     */
    @Test
    void testNullSource() {
        Matrix4f transform = Matrix4f.identity();
        
        Assertions.assertThrows(com.cgvsu.exceptions.TransformationException.class, () -> {
            ModelTransformer.applyTransform(null, transform);
        }, "Should throw TransformationException for null source");
    }
    
    /**
     * Тест обработки null для матрицы трансформации.
     */
    @Test
    void testNullTransform() {
        Model source = createSimpleModel();
        
        Assertions.assertThrows(com.cgvsu.exceptions.TransformationException.class, () -> {
            ModelTransformer.applyTransform(source, null);
        }, "Should throw TransformationException for null transform");
    }
    
    /**
     * Тест сохранения полигонов и текстурных координат.
     */
    @Test
    void testPolygonsAndTextureVerticesPreserved() {
        Model source = new Model();
        source.addVertex(new Vector3f(1, 2, 3));
        source.addVertex(new Vector3f(4, 5, 6));
        source.addVertex(new Vector3f(7, 8, 9));
        
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(java.util.Arrays.asList(0, 1, 2))); // Минимум 3 вершины
        source.addPolygon(polygon);
        
        source.addTextureVertex(new Vector2f(0.5f, 0.5f));
        
        Matrix4f transform = com.cgvsu.transform.AffineMatrixFactory.createTranslationMatrix(new Vector3f(1, 1, 1));
        Model result = ModelTransformer.applyTransform(source, transform);
        
        // Полигоны должны быть сохранены
        Assertions.assertEquals(1, result.getPolygonCount());
        Assertions.assertEquals(source.getPolygon(0).getVertexIndices().size(), 
            result.getPolygon(0).getVertexIndices().size());
        
        // Текстурные координаты должны быть сохранены
        Assertions.assertEquals(1, result.getTextureVertexCount());
        Vector2f tex = result.getTextureVertex(0);
        Assertions.assertEquals(0.5f, tex.x, EPSILON);
        Assertions.assertEquals(0.5f, tex.y, EPSILON);
    }
    
    /**
     * Тест модели с несколькими вершинами.
     */
    @Test
    void testMultipleVertices() {
        Model source = new Model();
        source.addVertex(new Vector3f(1, 0, 0));
        source.addVertex(new Vector3f(0, 2, 0));
        source.addVertex(new Vector3f(0, 0, 3));
        
        Vector3f scale = new Vector3f(2, 2, 2);
        Matrix4f transform = com.cgvsu.transform.AffineMatrixFactory.createScaleMatrix(scale);
        Model result = ModelTransformer.applyTransform(source, transform);
        
        Assertions.assertEquals(3, result.getVertexCount());
        Assertions.assertEquals(2.0f, result.getVertex(0).x, EPSILON); // 1 * 2
        Assertions.assertEquals(4.0f, result.getVertex(1).y, EPSILON); // 2 * 2
        Assertions.assertEquals(6.0f, result.getVertex(2).z, EPSILON); // 3 * 2
    }
    
    /**
     * Тест модели без нормалей.
     */
    @Test
    void testModelWithoutNormals() {
        Model source = createSimpleModel();
        // Модель без нормалей (нормали не добавляются)
        
        Matrix4f transform = com.cgvsu.transform.AffineMatrixFactory.createTranslationMatrix(new Vector3f(1, 1, 1));
        Model result = ModelTransformer.applyTransform(source, transform);
        
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.getNormalCount());
    }
    
    /**
     * Тест комбинированной трансформации (translation + rotation + scale).
     */
    @Test
    void testCombinedTransformation() {
        Model source = createSimpleModel();
        
        Vector3f translation = new Vector3f(1, 1, 1);
        Vector3f rotation = new Vector3f(0, 0, 0); // Без вращения для простоты
        Vector3f scale = new Vector3f(2, 2, 2);
        
        Matrix4f transform = com.cgvsu.transform.ModelMatrixBuilder.build(translation, rotation, scale);
        Model result = ModelTransformer.applyTransform(source, transform);
        
        Assertions.assertNotNull(result);
        // Вершина (1,2,3) после масштабирования на 2 и переноса на (1,1,1) = (2,4,6) + (1,1,1) = (3,5,7)
        Vector3f vertex = result.getVertex(0);
        Assertions.assertEquals(3.0f, vertex.x, EPSILON);
        Assertions.assertEquals(5.0f, vertex.y, EPSILON);
        Assertions.assertEquals(7.0f, vertex.z, EPSILON);
    }
    
    /**
     * Тест обработки null вершин в модели.
     */
    @Test
    void testNullVertices() {
        Model source = new Model();
        source.addVertex(null); // Null вершина
        source.addVertex(new Vector3f(1, 2, 3));
        
        Matrix4f transform = Matrix4f.identity();
        Model result = ModelTransformer.applyTransform(source, transform);
        
        // Null вершина преобразуется в Vector3f(0, 0, 0) для безопасности
        Assertions.assertEquals(2, result.getVertexCount());
        Assertions.assertNotNull(result.getVertex(0));
        Assertions.assertEquals(0.0f, result.getVertex(0).x, 1e-6f);
        Assertions.assertEquals(0.0f, result.getVertex(0).y, 1e-6f);
        Assertions.assertEquals(0.0f, result.getVertex(0).z, 1e-6f);
        Assertions.assertNotNull(result.getVertex(1));
    }
    
    /**
     * Тест обработки null нормалей в модели.
     */
    @Test
    void testNullNormals() {
        Model source = new Model();
        source.addVertex(new Vector3f(1, 2, 3));
        source.addNormal(null); // Null нормаль
        source.addNormal(new Vector3f(0, 1, 0));
        
        Matrix4f transform = Matrix4f.identity();
        Model result = ModelTransformer.applyTransform(source, transform);
        
        // Null нормаль преобразуется в Vector3f(0, 0, 1) (нормализованная) для безопасности
        Assertions.assertEquals(2, result.getNormalCount());
        Assertions.assertNotNull(result.getNormal(0));
        Assertions.assertEquals(0.0f, result.getNormal(0).x, 1e-6f);
        Assertions.assertEquals(0.0f, result.getNormal(0).y, 1e-6f);
        Assertions.assertEquals(1.0f, result.getNormal(0).z, 1e-6f);
        Assertions.assertNotNull(result.getNormal(1));
    }
    
    /**
     * Тест нормализации нормалей после трансформации.
     */
    @Test
    void testNormalNormalization() {
        Model source = new Model();
        source.addVertex(new Vector3f(1, 0, 0));
        source.addNormal(new Vector3f(2, 0, 0)); // Ненормализованная нормаль
        
        // Масштабирование на 3
        Vector3f scale = new Vector3f(3, 3, 3);
        Matrix4f transform = com.cgvsu.transform.AffineMatrixFactory.createScaleMatrix(scale);
        Model result = ModelTransformer.applyTransform(source, transform);
        
        // Нормаль должна быть нормализована
        Vector3f transformedNormal = result.getNormal(0);
        float length = transformedNormal.length();
        Assertions.assertEquals(1.0f, length, EPSILON, "Normal should be normalized after transformation");
    }
}