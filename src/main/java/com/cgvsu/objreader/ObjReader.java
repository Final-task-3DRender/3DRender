package com.cgvsu.objreader;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Класс для чтения 3D моделей из файлов формата OBJ.
 * 
 * <p>Поддерживает чтение:
 * <ul>
 *   <li>Вершин (v) - трехмерные координаты точек</li>
 *   <li>Текстурных координат (vt) - UV координаты для текстур</li>
 *   <li>Нормалей (vn) - векторы нормалей вершин</li>
 *   <li>Полигонов (f) - грани модели с поддержкой текстурных координат и нормалей</li>
 * </ul>
 * 
 * <p>Поддерживает относительную индексацию (отрицательные индексы).
 * Пропускает комментарии (строки, начинающиеся с #).
 * 
 * <p>При ошибках парсинга выбрасывает {@link ObjReaderException} с указанием номера строки.
 * 
 * @author CGVSU Team
 * @version 1.0
 * @see ObjReaderException
 */
public class ObjReader {

	/** Токен для вершин в OBJ файле */
	private static final String OBJ_VERTEX_TOKEN = "v";
	/** Токен для текстурных координат в OBJ файле */
	private static final String OBJ_TEXTURE_TOKEN = "vt";
	/** Токен для нормалей в OBJ файле */
	private static final String OBJ_NORMAL_TOKEN = "vn";
	/** Токен для полигонов (граней) в OBJ файле */
	private static final String OBJ_FACE_TOKEN = "f";
	/** Токен для комментариев в OBJ файле */
	private static final String OBJ_COMMENT_TOKEN = "#";

	/**
	 * Читает модель из содержимого OBJ файла.
	 * 
	 * <p>Парсит строки файла, извлекая вершины, текстурные координаты, нормали и полигоны.
	 * Валидирует модель после чтения (проверяет наличие вершин и полигонов).
	 * 
	 * @param fileContent содержимое OBJ файла в виде строки
	 * @return загруженная модель
	 * @throws ObjReaderException если содержимое файла пустое, null или содержит ошибки парсинга
	 */
	public static Model read(String fileContent) {
		if (fileContent == null || fileContent.trim().isEmpty()) {
			throw new ObjReaderException("File content is empty or null.", 0);
		}

		Model result = new Model();

		int lineInd = 0;
		try (Scanner scanner = new Scanner(fileContent)) {
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine().trim();
				lineInd++;

				if (line.isEmpty() || line.startsWith(OBJ_COMMENT_TOKEN)) {
					continue;
				}

				ArrayList<String> wordsInLine = new ArrayList<String>(Arrays.asList(line.split("\\s+")));
				wordsInLine.removeIf(String::isEmpty);
				
				if (wordsInLine.isEmpty()) {
					continue;
				}

				final String token = wordsInLine.get(0);
				wordsInLine.remove(0);

				switch (token) {
					case OBJ_VERTEX_TOKEN -> result.vertices.add(parseVertex(wordsInLine, lineInd));
					case OBJ_TEXTURE_TOKEN -> result.textureVertices.add(parseTextureVertex(wordsInLine, lineInd));
					case OBJ_NORMAL_TOKEN -> result.normals.add(parseNormal(wordsInLine, lineInd));
					case OBJ_FACE_TOKEN -> result.polygons.add(parseFace(wordsInLine, lineInd, result));
					default -> {}
				}
			}
		}

		validateModel(result);

		return result;
	}

	/**
	 * Валидирует загруженную модель.
	 * 
	 * <p>Проверяет, что модель содержит хотя бы одну вершину и один полигон.
	 * 
	 * @param model модель для валидации
	 * @throws ObjReaderException если модель невалидна (нет вершин или полигонов)
	 */
	private static void validateModel(Model model) {
		if (model.vertices.isEmpty()) {
			throw new ObjReaderException("Model has no vertices.", 0);
		}
		if (model.polygons.isEmpty()) {
			throw new ObjReaderException("Model has no polygons.", 0);
		}
	}

	/**
	 * Парсит строку с вершиной из OBJ файла.
	 * 
	 * <p>Ожидает 3 координаты (x, y, z) в формате float.
	 * Проверяет на NaN и Infinity значения.
	 * 
	 * @param wordsInLineWithoutToken список слов в строке без токена (только координаты)
	 * @param lineInd номер строки в файле (для сообщений об ошибках)
	 * @return трехмерный вектор вершины
	 * @throws ObjReaderException если количество аргументов неверное, значения не могут быть распарсены,
	 *                            или содержат NaN/Infinity
	 */
	protected static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		if (wordsInLineWithoutToken.isEmpty()) {
			throw new ObjReaderException("Too few arguments for vertex.", lineInd);
		}
		
		if (wordsInLineWithoutToken.size() < 3) {
			throw new ObjReaderException("Too few arguments for vertex.", lineInd);
		}
		
		if (wordsInLineWithoutToken.size() > 3) {
			throw new ObjReaderException("Too many arguments for vertex. Expected 3, got " + wordsInLineWithoutToken.size() + ".", lineInd);
		}
		
		try {
			float x = Float.parseFloat(wordsInLineWithoutToken.get(0));
			float y = Float.parseFloat(wordsInLineWithoutToken.get(1));
			float z = Float.parseFloat(wordsInLineWithoutToken.get(2));
			
			if (Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(z)) {
				throw new ObjReaderException("Vertex contains NaN (not a number) value.", lineInd);
			}
			if (Float.isInfinite(x) || Float.isInfinite(y) || Float.isInfinite(z)) {
				throw new ObjReaderException("Vertex contains Infinity value.", lineInd);
			}
			
			return new Vector3f(x, y, z);

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);
		}
	}

	/**
	 * Парсит строку с текстурной координатой из OBJ файла.
	 * 
	 * <p>Ожидает 2 координаты (u, v) в формате float.
	 * OBJ формат поддерживает также третью координату w, но она игнорируется.
	 * Проверяет на NaN и Infinity значения.
	 * 
	 * @param wordsInLineWithoutToken список слов в строке без токена (только координаты)
	 * @param lineInd номер строки в файле (для сообщений об ошибках)
	 * @return двумерный вектор текстурной координаты
	 * @throws ObjReaderException если количество аргументов неверное, значения не могут быть распарсены,
	 *                            или содержат NaN/Infinity
	 */
	protected static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		if (wordsInLineWithoutToken.isEmpty()) {
			throw new ObjReaderException("Too few arguments for texture vertex.", lineInd);
		}
		
		if (wordsInLineWithoutToken.size() < 2) {
			throw new ObjReaderException("Too few arguments for texture vertex.", lineInd);
		}
		
		if (wordsInLineWithoutToken.size() > 3) {
			throw new ObjReaderException("Too many arguments for texture vertex. Expected 2 or 3, got " + wordsInLineWithoutToken.size() + ".", lineInd);
		}
		
		try {
			float u = Float.parseFloat(wordsInLineWithoutToken.get(0));
			float v = Float.parseFloat(wordsInLineWithoutToken.get(1));
			
			if (Float.isNaN(u) || Float.isNaN(v)) {
				throw new ObjReaderException("Texture vertex contains NaN (not a number) value.", lineInd);
			}
			if (Float.isInfinite(u) || Float.isInfinite(v)) {
				throw new ObjReaderException("Texture vertex contains Infinity value.", lineInd);
			}
			
			return new Vector2f(u, v);

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);
		}
	}

	/**
	 * Парсит строку с нормалью из OBJ файла.
	 * 
	 * <p>Ожидает 3 компонента (x, y, z) в формате float.
	 * Проверяет на NaN и Infinity значения.
	 * 
	 * @param wordsInLineWithoutToken список слов в строке без токена (только компоненты)
	 * @param lineInd номер строки в файле (для сообщений об ошибках)
	 * @return трехмерный вектор нормали
	 * @throws ObjReaderException если количество аргументов неверное, значения не могут быть распарсены,
	 *                            или содержат NaN/Infinity
	 */
	protected static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		if (wordsInLineWithoutToken.isEmpty()) {
			throw new ObjReaderException("Too few arguments for normal.", lineInd);
		}
		
		if (wordsInLineWithoutToken.size() < 3) {
			throw new ObjReaderException("Too few arguments for normal.", lineInd);
		}
		
		if (wordsInLineWithoutToken.size() > 3) {
			throw new ObjReaderException("Too many arguments for normal. Expected 3, got " + wordsInLineWithoutToken.size() + ".", lineInd);
		}
		
		try {
			float x = Float.parseFloat(wordsInLineWithoutToken.get(0));
			float y = Float.parseFloat(wordsInLineWithoutToken.get(1));
			float z = Float.parseFloat(wordsInLineWithoutToken.get(2));
			
			if (Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(z)) {
				throw new ObjReaderException("Normal contains NaN (not a number) value.", lineInd);
			}
			if (Float.isInfinite(x) || Float.isInfinite(y) || Float.isInfinite(z)) {
				throw new ObjReaderException("Normal contains Infinity value.", lineInd);
			}
			
			return new Vector3f(x, y, z);

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);
		}
	}

	/**
	 * Парсит строку с полигоном (гранью) из OBJ файла.
	 * 
	 * <p>Формат полигона: f v1[/vt1][/vn1] v2[/vt2][/vn2] v3[/vt3][/vn3] ...
	 * где v - индекс вершины, vt - индекс текстурной координаты, vn - индекс нормали.
	 * 
	 * <p>Полигон должен содержать минимум 3 вершины.
	 * Проверяет согласованность индексов (если указаны текстурные координаты или нормали,
	 * они должны быть указаны для всех вершин).
	 * 
	 * @param wordsInLineWithoutToken список слов в строке без токена (только определения вершин)
	 * @param lineInd номер строки в файле (для сообщений об ошибках)
	 * @param model модель, содержащая уже загруженные вершины, текстурные координаты и нормали
	 * @return полигон с установленными индексами
	 * @throws ObjReaderException если формат неверный, недостаточно вершин, индексы не согласованы
	 *                            или выходят за границы массивов
	 */
	protected static Polygon parseFace(final ArrayList<String> wordsInLineWithoutToken, int lineInd, Model model) {
		if (wordsInLineWithoutToken.isEmpty()) {
			throw new ObjReaderException("Polygon has no vertices.", lineInd);
		}
		
		ArrayList<Integer> onePolygonVertexIndices = new ArrayList<Integer>();
		ArrayList<Integer> onePolygonTextureVertexIndices = new ArrayList<Integer>();
		ArrayList<Integer> onePolygonNormalIndices = new ArrayList<Integer>();

		for (String s : wordsInLineWithoutToken) {
			parseFaceWord(s, onePolygonVertexIndices, onePolygonTextureVertexIndices, onePolygonNormalIndices, lineInd, model);
		}

		if (onePolygonVertexIndices.size() < 3) {
			throw new ObjReaderException("Polygon must contain at least 3 vertices, got " + onePolygonVertexIndices.size() + ".", lineInd);
		}

		boolean hasTextureIndices = !onePolygonTextureVertexIndices.isEmpty();
		if (hasTextureIndices && onePolygonTextureVertexIndices.size() != onePolygonVertexIndices.size()) {
			throw new ObjReaderException("Inconsistent texture indices: some vertices have texture coordinates, others don't.", lineInd);
		}

		boolean hasNormalIndices = !onePolygonNormalIndices.isEmpty();
		if (hasNormalIndices && onePolygonNormalIndices.size() != onePolygonVertexIndices.size()) {
			throw new ObjReaderException("Inconsistent normal indices: some vertices have normals, others don't.", lineInd);
		}

		Polygon result = new Polygon();
		result.setVertexIndices(onePolygonVertexIndices);
		if (hasTextureIndices) {
			result.setTextureVertexIndices(onePolygonTextureVertexIndices);
		}
		if (hasNormalIndices) {
			result.setNormalIndices(onePolygonNormalIndices);
		}
		return result;
	}

	/**
	 * Парсит одно определение вершины в полигоне.
	 * 
	 * <p>Формат: v[/vt][/vn], где:
	 * <ul>
	 *   <li>v - индекс вершины (обязателен)</li>
	 *   <li>vt - индекс текстурной координаты (опционален)</li>
	 *   <li>vn - индекс нормали (опционален)</li>
	 * </ul>
	 * 
	 * <p>Поддерживает относительную индексацию (отрицательные индексы).
	 * 
	 * @param wordInLine определение вершины в формате v[/vt][/vn]
	 * @param onePolygonVertexIndices список для добавления индекса вершины
	 * @param onePolygonTextureVertexIndices список для добавления индекса текстурной координаты
	 * @param onePolygonNormalIndices список для добавления индекса нормали
	 * @param lineInd номер строки в файле (для сообщений об ошибках)
	 * @param model модель для проверки границ индексов
	 * @throws ObjReaderException если формат неверный, индекс не может быть распарсен,
	 *                            индекс равен нулю, или индекс выходит за границы массива
	 */
	protected static void parseFaceWord(
			String wordInLine,
			ArrayList<Integer> onePolygonVertexIndices,
			ArrayList<Integer> onePolygonTextureVertexIndices,
			ArrayList<Integer> onePolygonNormalIndices,
			int lineInd,
			Model model) {
		if (wordInLine == null || wordInLine.trim().isEmpty()) {
			throw new ObjReaderException("Empty polygon vertex definition.", lineInd);
		}
		
		try {
			String[] wordIndices = wordInLine.split("/", -1);
			
			if (wordIndices.length > 3) {
				throw new ObjReaderException("Invalid polygon format: too many slashes. Expected format: v[/vt][/vn].", lineInd);
			}
			
			if (wordIndices.length == 0 || wordIndices[0].isEmpty()) {
				throw new ObjReaderException("Missing vertex index in polygon definition.", lineInd);
			}
			
			int vertexIndex = parseIndex(wordIndices[0], model.vertices.size(), "vertex", lineInd);
			onePolygonVertexIndices.add(vertexIndex);
			
			switch (wordIndices.length) {
				case 1 -> {
				}
				case 2 -> {
					if (!wordIndices[1].isEmpty()) {
						int textureIndex = parseIndex(wordIndices[1], model.textureVertices.size(), "texture vertex", lineInd);
						onePolygonTextureVertexIndices.add(textureIndex);
					}
				}
				case 3 -> {
					if (!wordIndices[1].isEmpty()) {
						int textureIndex = parseIndex(wordIndices[1], model.textureVertices.size(), "texture vertex", lineInd);
						onePolygonTextureVertexIndices.add(textureIndex);
					}
					if (!wordIndices[2].isEmpty()) {
						int normalIndex = parseIndex(wordIndices[2], model.normals.size(), "normal", lineInd);
						onePolygonNormalIndices.add(normalIndex);
					}
				}
				default -> {
					throw new ObjReaderException("Invalid polygon format. Expected format: v[/vt][/vn], got " + wordIndices.length + " parts.", lineInd);
				}
			}

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse int value: " + e.getMessage(), lineInd);
		}
	}

	/**
	 * Парсит индекс из строки с поддержкой относительной индексации.
	 * 
	 * <p>OBJ формат использует индексацию, начинающуюся с 1 (не с 0).
	 * Отрицательные индексы означают относительную индексацию от конца массива.
	 * 
	 * <p>Примеры:
	 * <ul>
	 *   <li>"1" -> индекс 0 (первый элемент)</li>
	 *   <li>"-1" -> последний элемент массива</li>
	 *   <li>"-2" -> предпоследний элемент массива</li>
	 * </ul>
	 * 
	 * @param indexStr строка с индексом
	 * @param arraySize размер массива, к которому относится индекс
	 * @param indexType тип индекса ("vertex", "texture vertex", "normal") для сообщений об ошибках
	 * @param lineInd номер строки в файле (для сообщений об ошибках)
	 * @return преобразованный индекс (0-based)
	 * @throws ObjReaderException если индекс пустой, равен нулю, не может быть распарсен,
	 *                            или выходит за границы массива
	 */
	private static int parseIndex(String indexStr, int arraySize, String indexType, int lineInd) {
		if (indexStr == null || indexStr.trim().isEmpty()) {
			throw new ObjReaderException("Index " + indexType + " is empty.", lineInd);
		}
		
		if (arraySize == 0) {
			throw new ObjReaderException("Index " + indexType + " references non-existent data. No " + indexType + " data defined in file.", lineInd);
		}
		
		int index;
		try {
			index = Integer.parseInt(indexStr);
		} catch (NumberFormatException e) {
			if (indexStr.matches("^-?\\d+$")) {
				throw new ObjReaderException("Index " + indexType + " is too large or too small: " + indexStr + ".", lineInd);
			} else {
				throw new ObjReaderException("Invalid index " + indexType + " format: " + indexStr + ".", lineInd);
			}
		}
		
		if (index < 0) {
			index = arraySize + index;
		} else if (index > 0) {
			index = index - 1;
		} else {
			throw new ObjReaderException("Index " + indexType + " cannot be zero.", lineInd);
		}
		
		if (index < 0 || index >= arraySize) {
			String originalIndex = indexStr;
			throw new ObjReaderException("Index " + indexType + " out of bounds: " + originalIndex + " (converted to " + index + ", array size: " + arraySize + ").", lineInd);
		}
		
		return index;
	}
}
