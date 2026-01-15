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

	private static void validateModel(Model model) {
		if (model.vertices.isEmpty()) {
			throw new ObjReaderException("Model has no vertices.", 0);
		}
		if (model.polygons.isEmpty()) {
			throw new ObjReaderException("Model has no polygons.", 0);
		}
	}

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
