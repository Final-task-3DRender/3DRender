package com.cgvsu.triangulation;

import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.objreader.ObjReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Тесты для SimpleTriangulator
 */
class SimpleTriangulatorTest {
    
    private Model cubeModel;
    private Model oneSimplePolygonModel;
    private final Triangulator simpleTriangulator = new SimpleTriangulator();
    
    @BeforeEach
    void setUp() throws IOException {
        setUpOnePolygonModel();
        setUpCube();
    }
    
    private void setUpOnePolygonModel() throws IOException {
        String content = loadResourceFile("models/oneSimplePolygon.obj");
        oneSimplePolygonModel = ObjReader.read(content);
    }
    
    private void setUpCube() throws IOException {
        String content = loadResourceFile("models/cube.obj");
        cubeModel = ObjReader.read(content);
    }
    
    private String loadResourceFile(String resourcePath) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    
    @Test
    void testTriangulationAllTriangles() {
        simpleTriangulator.triangulateModel(cubeModel);
        for (Polygon polygon : cubeModel.polygons) {
            Assertions.assertEquals(3, polygon.getVertexIndices().size(),
                "All polygons should be triangles after triangulation");
        }
        
        simpleTriangulator.triangulateModel(oneSimplePolygonModel);
        for (Polygon polygon : oneSimplePolygonModel.polygons) {
            Assertions.assertEquals(3, polygon.getVertexIndices().size(),
                "All polygons should be triangles after triangulation");
        }
    }
    
    @Test
    void testTriangulatedModelTextures() {
        Model model1 = simpleTriangulator.createTriangulatedModel(cubeModel);
        Model model2 = simpleTriangulator.createTriangulatedModel(oneSimplePolygonModel);
        Assertions.assertEquals(cubeModel.textureVertices, model1.textureVertices);
        Assertions.assertEquals(oneSimplePolygonModel.textureVertices, model2.textureVertices);
    }
    
    @Test
    void testTriangulatedModelNormals() {
        Model model1 = simpleTriangulator.createTriangulatedModel(cubeModel);
        Model model2 = simpleTriangulator.createTriangulatedModel(oneSimplePolygonModel);
        Assertions.assertEquals(cubeModel.normals, model1.normals);
        Assertions.assertEquals(oneSimplePolygonModel.normals, model2.normals);
    }
    
    @Test
    void testTriangulatedModelChange() {
        Model model = simpleTriangulator.createTriangulatedModel(cubeModel);
        Assertions.assertEquals(cubeModel.normals, model.normals);
        Assertions.assertEquals(cubeModel.textureVertices, model.textureVertices);
        
        // Изменяем модель после триангуляции
        model.normals.add(new com.cgvsu.math.Vector3f(5, 4, 3));
        model.textureVertices.add(new com.cgvsu.math.Vector2f(1, 1));
        
        // Исходная модель не должна измениться
        Assertions.assertNotEquals(cubeModel.normals.size(), model.normals.size());
        Assertions.assertNotEquals(cubeModel.textureVertices.size(), model.textureVertices.size());
    }
    
    @Test
    void testTriangulateEmptyModel() {
        Model emptyModel = new Model();
        emptyModel.vertices = new ArrayList<>();
        emptyModel.textureVertices = new ArrayList<>();
        emptyModel.normals = new ArrayList<>();
        emptyModel.polygons = new ArrayList<>();
        
        Assertions.assertDoesNotThrow(() -> simpleTriangulator.triangulateModel(emptyModel));
        Assertions.assertTrue(emptyModel.polygons.isEmpty());
    }
    
    @Test
    void testTriangulatePolygon_LessThanThreeVertices() {
        Model model = new Model();
        model.vertices.add(new com.cgvsu.math.Vector3f(0, 0, 0));
        model.vertices.add(new com.cgvsu.math.Vector3f(1, 0, 0));
        model.polygons = new ArrayList<>();
        
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(java.util.Arrays.asList(0, 1)));
        
        // Полигон с менее чем 3 вершинами должен вернуться как есть
        java.util.List<Polygon> result = simpleTriangulator.triangulatePolygon(model, polygon);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(2, result.get(0).getVertexIndices().size());
    }
    
    @Test
    void testTriangulatePolygon_ThreeVertices() {
        Model model = new Model();
        model.vertices.add(new com.cgvsu.math.Vector3f(0, 0, 0));
        model.vertices.add(new com.cgvsu.math.Vector3f(1, 0, 0));
        model.vertices.add(new com.cgvsu.math.Vector3f(0, 1, 0));
        model.polygons = new ArrayList<>();
        
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(java.util.Arrays.asList(0, 1, 2)));
        
        // Полигон с 3 вершинами должен вернуться как есть
        java.util.List<Polygon> result = simpleTriangulator.triangulatePolygon(model, polygon);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(3, result.get(0).getVertexIndices().size());
    }
    
    @Test
    void testTriangulatePolygon_FourVertices() {
        Model model = new Model();
        model.vertices.add(new com.cgvsu.math.Vector3f(0, 0, 0));
        model.vertices.add(new com.cgvsu.math.Vector3f(1, 0, 0));
        model.vertices.add(new com.cgvsu.math.Vector3f(1, 1, 0));
        model.vertices.add(new com.cgvsu.math.Vector3f(0, 1, 0));
        model.polygons = new ArrayList<>();
        
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(java.util.Arrays.asList(0, 1, 2, 3)));
        
        // Полигон с 4 вершинами должен быть разбит на 2 треугольника
        java.util.List<Polygon> result = simpleTriangulator.triangulatePolygon(model, polygon);
        Assertions.assertEquals(2, result.size());
        for (Polygon p : result) {
            Assertions.assertEquals(3, p.getVertexIndices().size());
        }
    }
    
    @Test
    void testTriangulatePolygon_FiveVertices() {
        Model model = new Model();
        for (int i = 0; i < 5; i++) {
            model.vertices.add(new com.cgvsu.math.Vector3f(i, 0, 0));
        }
        model.polygons = new ArrayList<>();
        
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(java.util.Arrays.asList(0, 1, 2, 3, 4)));
        
        // Полигон с 5 вершинами должен быть разбит на 3 треугольника
        java.util.List<Polygon> result = simpleTriangulator.triangulatePolygon(model, polygon);
        Assertions.assertEquals(3, result.size());
        for (Polygon p : result) {
            Assertions.assertEquals(3, p.getVertexIndices().size());
        }
    }
    
    @Test
    void testTriangulationPreservesVertexCount() {
        int originalVertexCount = cubeModel.vertices.size();
        simpleTriangulator.triangulateModel(cubeModel);
        Assertions.assertEquals(originalVertexCount, cubeModel.vertices.size(),
            "Triangulation should not change vertex count");
    }
}
