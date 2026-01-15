package com.cgvsu.objreader;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

class ObjReaderTest {

    @Test
    public void testParseVertex01() {
        final ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.01", "1.02", "1.03"));
        final Vector3f result = ObjReader.parseVertex(wordsInLineWithoutToken, 5);
        final Vector3f expectedResult = new Vector3f(1.01f, 1.02f, 1.03f);
        Assertions.assertTrue(result.equals(expectedResult));
    }

    @Test
    public void testParseVertex02() {
        final ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("ab", "o", "ba"));
        try {
            ObjReader.parseVertex(wordsInLineWithoutToken, 10);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Не удалось распарсить значение float.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testParseVertex03() {
        final ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.0", "2.0"));
        try {
            ObjReader.parseVertex(wordsInLineWithoutToken, 10);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Слишком мало аргументов для вершины.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testParseVertex04() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.0", "2.0", "3.0", "4.0"));
        try {
            ObjReader.parseVertex(wordsInLineWithoutToken, 10);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Слишком много аргументов для вершины. Ожидается 3, получено 4.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testParseVertex05_EmptyList() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>();
        try {
            ObjReader.parseVertex(wordsInLineWithoutToken, 10);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Слишком мало аргументов для вершины.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testParseVertex06_NaN() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("NaN", "1.0", "2.0"));
        try {
            ObjReader.parseVertex(wordsInLineWithoutToken, 10);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Вершина содержит значение NaN (не число).";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testParseVertex07_Infinity() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("Infinity", "1.0", "2.0"));
        try {
            ObjReader.parseVertex(wordsInLineWithoutToken, 10);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Вершина содержит значение Infinity.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testParseVertex08_NegativeValues() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("-1.5", "-2.5", "-3.5"));
        Vector3f result = ObjReader.parseVertex(wordsInLineWithoutToken, 5);
        Vector3f expectedResult = new Vector3f(-1.5f, -2.5f, -3.5f);
        Assertions.assertTrue(result.equals(expectedResult));
    }


    @Test
    public void testParseTextureVertex01() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("0.5", "0.3"));
        Vector2f result = ObjReader.parseTextureVertex(wordsInLineWithoutToken, 5);
        Assertions.assertNotNull(result);
    }

    @Test
    public void testParseTextureVertex02_TooFew() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("0.5"));
        try {
            ObjReader.parseTextureVertex(wordsInLineWithoutToken, 10);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Слишком мало аргументов для текстуры вершины.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testParseTextureVertex03_WithW() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("0.5", "0.3", "0.0"));
        Vector2f result = ObjReader.parseTextureVertex(wordsInLineWithoutToken, 5);
        Assertions.assertNotNull(result);
    }

    @Test
    public void testParseTextureVertex03_TooMany() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("0.5", "0.3", "0.1", "0.2"));
        try {
            ObjReader.parseTextureVertex(wordsInLineWithoutToken, 10);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Слишком много аргументов для текстуры вершины. Ожидается 2 или 3, получено 4.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testParseTextureVertex04_InvalidFormat() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("abc", "def"));
        try {
            ObjReader.parseTextureVertex(wordsInLineWithoutToken, 10);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Не удалось распарсить значение float.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testParseTextureVertex05_NaN() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("NaN", "0.5"));
        try {
            ObjReader.parseTextureVertex(wordsInLineWithoutToken, 10);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Текстура вершины содержит значение NaN (не число).";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }


    @Test
    public void testParseNormal01() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("0.0", "0.0", "1.0"));
        Vector3f result = ObjReader.parseNormal(wordsInLineWithoutToken, 5);
        Vector3f expectedResult = new Vector3f(0.0f, 0.0f, 1.0f);
        Assertions.assertTrue(result.equals(expectedResult));
    }

    @Test
    public void testParseNormal02_TooFew() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("0.0", "0.0"));
        try {
            ObjReader.parseNormal(wordsInLineWithoutToken, 10);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Слишком мало аргументов для нормали.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testParseNormal03_TooMany() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("0.0", "0.0", "1.0", "2.0"));
        try {
            ObjReader.parseNormal(wordsInLineWithoutToken, 10);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Слишком много аргументов для нормали. Ожидается 3, получено 4.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testParseNormal04_Infinity() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("Infinity", "0.0", "1.0"));
        try {
            ObjReader.parseNormal(wordsInLineWithoutToken, 10);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Нормаль содержит значение Infinity.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }


    @Test
    public void testParseFace01_SimpleFace() {
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        model.vertices.add(new Vector3f(1, 1, 0));
        
        ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList("1", "2", "3"));
        Polygon result = ObjReader.parseFace(wordsInLine, 5, model);
        
        Assertions.assertEquals(3, result.getVertexIndices().size());
        Assertions.assertEquals(0, result.getVertexIndices().get(0).intValue());
        Assertions.assertEquals(1, result.getVertexIndices().get(1).intValue());
        Assertions.assertEquals(2, result.getVertexIndices().get(2).intValue());
    }

    @Test
    public void testParseFace02_WithTexture() {
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        model.vertices.add(new Vector3f(1, 1, 0));
        model.textureVertices.add(new Vector2f(0, 0));
        model.textureVertices.add(new Vector2f(1, 0));
        model.textureVertices.add(new Vector2f(1, 1));
        
        ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList("1/1", "2/2", "3/3"));
        Polygon result = ObjReader.parseFace(wordsInLine, 5, model);
        
        Assertions.assertEquals(3, result.getVertexIndices().size());
        Assertions.assertEquals(3, result.getTextureVertexIndices().size());
    }

    @Test
    public void testParseFace03_WithNormal() {
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        model.vertices.add(new Vector3f(1, 1, 0));
        model.normals.add(new Vector3f(0, 0, 1));
        model.normals.add(new Vector3f(0, 0, 1));
        model.normals.add(new Vector3f(0, 0, 1));
        
        ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList("1//1", "2//2", "3//3"));
        Polygon result = ObjReader.parseFace(wordsInLine, 5, model);
        
        Assertions.assertEquals(3, result.getVertexIndices().size());
        Assertions.assertEquals(3, result.getNormalIndices().size());
    }

    @Test
    public void testParseFace04_WithTextureAndNormal() {
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        model.vertices.add(new Vector3f(1, 1, 0));
        model.textureVertices.add(new Vector2f(0, 0));
        model.textureVertices.add(new Vector2f(1, 0));
        model.textureVertices.add(new Vector2f(1, 1));
        model.normals.add(new Vector3f(0, 0, 1));
        model.normals.add(new Vector3f(0, 0, 1));
        model.normals.add(new Vector3f(0, 0, 1));
        
        ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList("1/1/1", "2/2/2", "3/3/3"));
        Polygon result = ObjReader.parseFace(wordsInLine, 5, model);
        
        Assertions.assertEquals(3, result.getVertexIndices().size());
        Assertions.assertEquals(3, result.getTextureVertexIndices().size());
        Assertions.assertEquals(3, result.getNormalIndices().size());
    }

    @Test
    public void testParseFace05_TooFewVertices() {
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        
        ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList("1", "2"));
        try {
            ObjReader.parseFace(wordsInLine, 10, model);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Полигон должен содержать минимум 3 вершины, получено 2.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testParseFace06_IndexOutOfBounds() {
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        model.vertices.add(new Vector3f(1, 1, 0));
        
        ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList("1", "2", "100"));
        try {
            ObjReader.parseFace(wordsInLine, 10, model);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            Assertions.assertTrue(exception.getMessage().contains("вне границ"));
        }
    }

    @Test
    public void testParseFace07_InconsistentTexture() {
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        model.vertices.add(new Vector3f(1, 1, 0));
        model.textureVertices.add(new Vector2f(0, 0));
        model.textureVertices.add(new Vector2f(1, 0));
        
        ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList("1/1", "2/2", "3"));
        try {
            ObjReader.parseFace(wordsInLine, 10, model);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Несогласованные индексы текстур: у некоторых вершин есть координаты текстуры, у других нет.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testParseFace08_InconsistentNormal() {
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        model.vertices.add(new Vector3f(1, 1, 0));
        model.normals.add(new Vector3f(0, 0, 1));
        model.normals.add(new Vector3f(0, 0, 1));
        
        ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList("1//1", "2//2", "3"));
        try {
            ObjReader.parseFace(wordsInLine, 10, model);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Несогласованные индексы нормалей: у некоторых вершин есть нормали, у других нет.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testParseFace09_EmptyFace() {
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        
        ArrayList<String> wordsInLine = new ArrayList<>();
        try {
            ObjReader.parseFace(wordsInLine, 10, model);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. У полигона нет вершин.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testParseFace10_InvalidFormat() {
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        model.vertices.add(new Vector3f(1, 1, 0));
        
        ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList("1/1/1/1", "2/2/2", "3/3/3"));
        try {
            ObjReader.parseFace(wordsInLine, 10, model);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            Assertions.assertTrue(exception.getMessage().contains("Неверный формат полигона"));
        }
    }


    @Test
    public void testNegativeIndex01_LastVertex() {
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        model.vertices.add(new Vector3f(1, 1, 0));
        
        ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList("1", "2", "-1"));
        Polygon result = ObjReader.parseFace(wordsInLine, 5, model);
        
        Assertions.assertEquals(3, result.getVertexIndices().size());
        Assertions.assertEquals(2, result.getVertexIndices().get(2).intValue());
    }

    @Test
    public void testNegativeIndex02_PreviousVertex() {
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        model.vertices.add(new Vector3f(1, 1, 0));
        model.vertices.add(new Vector3f(0, 1, 0));
        
        ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList("1", "2", "-2"));
        Polygon result = ObjReader.parseFace(wordsInLine, 5, model);
        
        Assertions.assertEquals(3, result.getVertexIndices().size());
        Assertions.assertEquals(2, result.getVertexIndices().get(2).intValue());
    }

    @Test
    public void testNegativeIndex03_OutOfBounds() {
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        
        ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList("1", "2", "-10"));
        try {
            ObjReader.parseFace(wordsInLine, 10, model);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            Assertions.assertTrue(exception.getMessage().contains("вне границ"));
        }
    }

    @Test
    public void testNegativeIndex04_ZeroIndex() {
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        model.vertices.add(new Vector3f(1, 1, 0));
        
        ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList("1", "2", "0"));
        try {
            ObjReader.parseFace(wordsInLine, 10, model);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Индекс vertex не может быть равен нулю.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }


    @Test
    public void testRead01_ValidFile() {
        String fileContent = """
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 1.0 1.0 0.0
            f 1 2 3
            """;
        
        Model model = ObjReader.read(fileContent);
        
        Assertions.assertEquals(3, model.vertices.size());
        Assertions.assertEquals(1, model.polygons.size());
        Assertions.assertEquals(3, model.polygons.get(0).getVertexIndices().size());
    }

    @Test
    public void testRead02_WithComments() {
        String fileContent = """
            # This is a comment
            v 0.0 0.0 0.0
            # Another comment
            v 1.0 0.0 0.0
            v 1.0 1.0 0.0
            f 1 2 3
            """;
        
        Model model = ObjReader.read(fileContent);
        
        Assertions.assertEquals(3, model.vertices.size());
        Assertions.assertEquals(1, model.polygons.size());
    }

    @Test
    public void testRead03_WithEmptyLines() {
        String fileContent = """
            
            v 0.0 0.0 0.0
            
            v 1.0 0.0 0.0
            v 1.0 1.0 0.0
            
            f 1 2 3
            
            """;
        
        Model model = ObjReader.read(fileContent);
        
        Assertions.assertEquals(3, model.vertices.size());
        Assertions.assertEquals(1, model.polygons.size());
    }

    @Test
    public void testRead04_NoVertices() {
        String fileContent = """
            f 1 2 3
            """;
        
        try {
            ObjReader.read(fileContent);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String msg = exception.getMessage();
            Assertions.assertTrue(msg.contains("вне границ") || 
                                msg.contains("массив пуст") ||
                                msg.contains("Не удалось распарсить") ||
                                msg.contains("Индекс") ||
                                msg.contains("пустой"));
        }
    }

    @Test
    public void testRead05_NoPolygons() {
        String fileContent = """
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 1.0 1.0 0.0
            """;
        
        try {
            ObjReader.read(fileContent);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 0. В модели нет полигонов.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testRead06_EmptyFile() {
        String fileContent = "";
        
        try {
            ObjReader.read(fileContent);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 0. Содержимое файла пустое или равно null.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testRead07_NullFile() {
        try {
            ObjReader.read(null);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 0. Содержимое файла пустое или равно null.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testRead08_ComplexModel() {
        String fileContent = """
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 1.0 1.0 0.0
            v 0.0 1.0 0.0
            vt 0.0 0.0
            vt 1.0 0.0
            vt 1.0 1.0
            vt 0.0 1.0
            vn 0.0 0.0 1.0
            f 1/1/1 2/2/1 3/3/1
            f 1/1/1 3/3/1 4/4/1
            """;
        
        Model model = ObjReader.read(fileContent);
        
        Assertions.assertEquals(4, model.vertices.size());
        Assertions.assertEquals(4, model.textureVertices.size());
        Assertions.assertEquals(1, model.normals.size());
        Assertions.assertEquals(2, model.polygons.size());
    }

    @Test
    public void testRead09_WithMultipleSpaces() {
        String fileContent = """
            v    0.0    0.0    0.0
            v   1.0   0.0   0.0
            v  1.0  1.0  0.0
            f  1  2  3
            """;
        
        Model model = ObjReader.read(fileContent);
        
        Assertions.assertEquals(3, model.vertices.size());
        Assertions.assertEquals(1, model.polygons.size());
    }

    @Test
    public void testRead10_WithTabs() {
        String fileContent = "v\t0.0\t0.0\t0.0\nv\t1.0\t0.0\t0.0\nv\t1.0\t1.0\t0.0\nf\t1\t2\t3";
        
        Model model = ObjReader.read(fileContent);
        
        Assertions.assertEquals(3, model.vertices.size());
        Assertions.assertEquals(1, model.polygons.size());
    }


    @Test
    public void testEdgeCase01_ScientificNotation() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.5e-10", "2.5e10", "3.5E-5"));
        Vector3f result = ObjReader.parseVertex(wordsInLineWithoutToken, 5);
        Vector3f expected = new Vector3f(1.5e-10f, 2.5e10f, 3.5E-5f);
        Assertions.assertTrue(result.equals(expected));
    }

    @Test
    public void testEdgeCase02_VeryLargePolygon() {
        Model model = new Model();
        for (int i = 0; i < 10; i++) {
            model.vertices.add(new Vector3f(i, 0, 0));
        }
        
        ArrayList<String> wordsInLine = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            wordsInLine.add(String.valueOf(i));
        }
        
        Polygon result = ObjReader.parseFace(wordsInLine, 5, model);
        Assertions.assertEquals(10, result.getVertexIndices().size());
    }

    @Test
    public void testEdgeCase03_TextureWithoutVertex() {
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        model.vertices.add(new Vector3f(1, 1, 0));
        model.textureVertices.add(new Vector2f(0, 0));
        
        ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList("1/1", "2", "3"));
        try {
            ObjReader.parseFace(wordsInLine, 10, model);
            Assertions.fail();
        } catch (ObjReaderException exception) {
            Assertions.assertTrue(exception.getMessage().contains("Несогласованные индексы текстур"));
        }
    }
}
