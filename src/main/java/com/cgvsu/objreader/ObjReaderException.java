package com.cgvsu.objreader;

/**
 * Исключение, возникающее при ошибках чтения или парсинга файлов OBJ.
 * 
 * <p>Содержит информацию о номере строки, на которой произошла ошибка,
 * и описание проблемы.
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class ObjReaderException extends RuntimeException {
    
    /**
     * Создает новое исключение с указанием номера строки и сообщения об ошибке.
     * 
     * @param errorMessage описание ошибки
     * @param lineInd номер строки в файле, на которой произошла ошибка (начиная с 1)
     */
    public ObjReaderException(String errorMessage, int lineInd) {
        super("Error parsing OBJ file on line: " + lineInd + ". " + errorMessage);
    }
}