package com.cgvsu.objreader;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ObjReader {

	private static final String OBJ_VERTEX_TOKEN = "v";
	private static final String OBJ_TEXTURE_TOKEN = "vt";
	private static final String OBJ_NORMAL_TOKEN = "vn";
	private static final String OBJ_FACE_TOKEN = "f";
	private static final String OBJ_COMMENT_TOKEN = "#";

	public static Model read(String fileContent) {
		if (fileContent == null || fileContent.trim().isEmpty()) {
			throw new ObjReaderException("Содержимое файла пустое или равно null.", 0);
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
					case OBJ_VERTEX_TOKEN -> result.addVertex(parseVertex(wordsInLine, lineInd));
					case OBJ_TEXTURE_TOKEN -> result.addTextureVertex(parseTextureVertex(wordsInLine, lineInd));
					case OBJ_NORMAL_TOKEN -> result.addNormal(parseNormal(wordsInLine, lineInd));
					case OBJ_FACE_TOKEN -> result.addPolygon(parseFace(wordsInLine, lineInd, result));
					default -> {}
				}
			}
		}

		validateModel(result);

		return result;
	}

	private static void validateModel(Model model) {
		if (model.getVertexCount() == 0) {
			throw new ObjReaderException("В модели нет вершин.", 0);
		}
		if (model.getPolygonCount() == 0) {
			throw new ObjReaderException("В модели нет полигонов.", 0);
		}
	}

	protected static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		if (wordsInLineWithoutToken.isEmpty()) {
			throw new ObjReaderException("Слишком мало аргументов для вершины.", lineInd);
		}
		
		if (wordsInLineWithoutToken.size() < 3) {
			throw new ObjReaderException("Слишком мало аргументов для вершины.", lineInd);
		}
		
		if (wordsInLineWithoutToken.size() > 3) {
			throw new ObjReaderException("Слишком много аргументов для вершины. Ожидается 3, получено " + wordsInLineWithoutToken.size() + ".", lineInd);
		}
		
		try {
			float x = Float.parseFloat(wordsInLineWithoutToken.get(0));
			float y = Float.parseFloat(wordsInLineWithoutToken.get(1));
			float z = Float.parseFloat(wordsInLineWithoutToken.get(2));
			
			if (Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(z)) {
				throw new ObjReaderException("Вершина содержит значение NaN (не число).", lineInd);
			}
			if (Float.isInfinite(x) || Float.isInfinite(y) || Float.isInfinite(z)) {
				throw new ObjReaderException("Вершина содержит значение Infinity.", lineInd);
			}
			
			return new Vector3f(x, y, z);

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Не удалось распарсить значение float.", lineInd);
		}
	}

	protected static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		if (wordsInLineWithoutToken.isEmpty()) {
			throw new ObjReaderException("Слишком мало аргументов для текстуры вершины.", lineInd);
		}
		
		if (wordsInLineWithoutToken.size() < 2) {
			throw new ObjReaderException("Слишком мало аргументов для текстуры вершины.", lineInd);
		}
		
		if (wordsInLineWithoutToken.size() > 3) {
			throw new ObjReaderException("Слишком много аргументов для текстуры вершины. Ожидается 2 или 3, получено " + wordsInLineWithoutToken.size() + ".", lineInd);
		}
		
		try {
			float u = Float.parseFloat(wordsInLineWithoutToken.get(0));
			float v = Float.parseFloat(wordsInLineWithoutToken.get(1));
			
			if (wordsInLineWithoutToken.size() == 3) {
				float w = Float.parseFloat(wordsInLineWithoutToken.get(2));
				if (Float.isNaN(w) || Float.isInfinite(w)) {
					throw new ObjReaderException("Текстура вершины содержит значение NaN или Infinity.", lineInd);
				}
			}
			
			if (Float.isNaN(u) || Float.isNaN(v)) {
				throw new ObjReaderException("Текстура вершины содержит значение NaN (не число).", lineInd);
			}
			if (Float.isInfinite(u) || Float.isInfinite(v)) {
				throw new ObjReaderException("Текстура вершины содержит значение Infinity.", lineInd);
			}
			
			return new Vector2f(u, v);

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Не удалось распарсить значение float.", lineInd);
		}
	}

	protected static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		if (wordsInLineWithoutToken.isEmpty()) {
			throw new ObjReaderException("Слишком мало аргументов для нормали.", lineInd);
		}
		
		if (wordsInLineWithoutToken.size() < 3) {
			throw new ObjReaderException("Слишком мало аргументов для нормали.", lineInd);
		}
		
		if (wordsInLineWithoutToken.size() > 3) {
			throw new ObjReaderException("Слишком много аргументов для нормали. Ожидается 3, получено " + wordsInLineWithoutToken.size() + ".", lineInd);
		}
		
		try {
			float x = Float.parseFloat(wordsInLineWithoutToken.get(0));
			float y = Float.parseFloat(wordsInLineWithoutToken.get(1));
			float z = Float.parseFloat(wordsInLineWithoutToken.get(2));
			
			if (Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(z)) {
				throw new ObjReaderException("Нормаль содержит значение NaN (не число).", lineInd);
			}
			if (Float.isInfinite(x) || Float.isInfinite(y) || Float.isInfinite(z)) {
				throw new ObjReaderException("Нормаль содержит значение Infinity.", lineInd);
			}
			
			return new Vector3f(x, y, z);

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Не удалось распарсить значение float.", lineInd);
		}
	}

	protected static Polygon parseFace(final ArrayList<String> wordsInLineWithoutToken, int lineInd, Model model) {
		if (wordsInLineWithoutToken.isEmpty()) {
			throw new ObjReaderException("У полигона нет вершин.", lineInd);
		}
		
		ArrayList<Integer> onePolygonVertexIndices = new ArrayList<Integer>();
		ArrayList<Integer> onePolygonTextureVertexIndices = new ArrayList<Integer>();
		ArrayList<Integer> onePolygonNormalIndices = new ArrayList<Integer>();

		for (String s : wordsInLineWithoutToken) {
			parseFaceWord(s, onePolygonVertexIndices, onePolygonTextureVertexIndices, onePolygonNormalIndices, lineInd, model);
		}

		if (onePolygonVertexIndices.size() < 3) {
			throw new ObjReaderException("Полигон должен содержать минимум 3 вершины, получено " + onePolygonVertexIndices.size() + ".", lineInd);
		}

		boolean hasTextureIndices = !onePolygonTextureVertexIndices.isEmpty();
		if (hasTextureIndices && onePolygonTextureVertexIndices.size() != onePolygonVertexIndices.size()) {
			throw new ObjReaderException("Несогласованные индексы текстур: у некоторых вершин есть координаты текстуры, у других нет.", lineInd);
		}

		boolean hasNormalIndices = !onePolygonNormalIndices.isEmpty();
		if (hasNormalIndices && onePolygonNormalIndices.size() != onePolygonVertexIndices.size()) {
			throw new ObjReaderException("Несогласованные индексы нормалей: у некоторых вершин есть нормали, у других нет.", lineInd);
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

	protected static void parseFaceWord(
			String wordInLine,
			ArrayList<Integer> onePolygonVertexIndices,
			ArrayList<Integer> onePolygonTextureVertexIndices,
			ArrayList<Integer> onePolygonNormalIndices,
			int lineInd,
			Model model) {
		if (wordInLine == null || wordInLine.trim().isEmpty()) {
			throw new ObjReaderException("Пустое определение вершины полигона.", lineInd);
		}
		
		try {
			String[] wordIndices = wordInLine.split("/", -1);
			
			if (wordIndices.length > 3) {
				throw new ObjReaderException("Неверный формат полигона: слишком много слешей. Ожидаемый формат: v[/vt][/vn].", lineInd);
			}
			
			if (wordIndices.length == 0 || wordIndices[0].isEmpty()) {
				throw new ObjReaderException("Отсутствует индекс вершины в определении полигона.", lineInd);
			}
			
			int vertexIndex = parseIndex(wordIndices[0], model.getVertexCount(), "vertex", lineInd);
			onePolygonVertexIndices.add(vertexIndex);
			
			switch (wordIndices.length) {
				case 1 -> {
				}
				case 2 -> {
					if (!wordIndices[1].isEmpty()) {
						int textureIndex = parseIndex(wordIndices[1], model.getTextureVertexCount(), "texture vertex", lineInd);
						onePolygonTextureVertexIndices.add(textureIndex);
					}
				}
				case 3 -> {
					if (!wordIndices[1].isEmpty()) {
						int textureIndex = parseIndex(wordIndices[1], model.getTextureVertexCount(), "texture vertex", lineInd);
						onePolygonTextureVertexIndices.add(textureIndex);
					}
					if (!wordIndices[2].isEmpty()) {
						int normalIndex = parseIndex(wordIndices[2], model.getNormalCount(), "normal", lineInd);
						onePolygonNormalIndices.add(normalIndex);
					}
				}
				default -> {
					throw new ObjReaderException("Неверный формат полигона. Ожидаемый формат: v[/vt][/vn], получено " + wordIndices.length + " частей.", lineInd);
				}
			}

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Не удалось распарсить значение int: " + e.getMessage(), lineInd);
		}
	}

	// Парсинг индекса с поддержкой отрицательных индексов
	private static int parseIndex(String indexStr, int arraySize, String indexType, int lineInd) {
		if (indexStr == null || indexStr.trim().isEmpty()) {
			throw new ObjReaderException("Индекс " + indexType + " пустой.", lineInd);
		}
		
		if (arraySize == 0) {
			throw new ObjReaderException("Не удалось распарсить индекс " + indexType + ": массив пуст.", lineInd);
		}
		
		int index;
		try {
			index = Integer.parseInt(indexStr);
		} catch (NumberFormatException e) {
			if (indexStr.matches("^-?\\d+$")) {
				throw new ObjReaderException("Индекс " + indexType + " слишком большой или слишком маленький: " + indexStr + ".", lineInd);
			} else {
				throw new ObjReaderException("Неверный формат индекса " + indexType + ": " + indexStr + ".", lineInd);
			}
		}
		
		if (index < 0) {
			index = arraySize + index;
		} else if (index > 0) {
			index = index - 1;
		} else {
			throw new ObjReaderException("Индекс " + indexType + " не может быть равен нулю.", lineInd);
		}
		
		if (index < 0 || index >= arraySize) {
			String originalIndex = indexStr;
			throw new ObjReaderException("Индекс " + indexType + " вне границ: " + originalIndex + " (преобразован в " + index + ", размер массива: " + arraySize + ").", lineInd);
		}
		
		return index;
	}
}
