package com.cgvsu.exceptions;

/**
 * Исключение, возникающее при ошибках трансформации моделей.
 * 
 * <p>Используется для ошибок:
 * <ul>
 *   <li>Некорректные параметры трансформации</li>
 *   <li>Ошибки при применении матриц трансформации</li>
 *   <li>Проблемы с вычислением трансформаций</li>
 * </ul>
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class TransformationException extends RuntimeException {
    
    public TransformationException(String message) {
        super(message);
    }
    
    public TransformationException(String message, Throwable cause) {
        super(message, cause);
    }
}
