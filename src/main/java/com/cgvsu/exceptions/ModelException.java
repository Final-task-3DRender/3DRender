package com.cgvsu.exceptions;

/**
 * Исключение, возникающее при ошибках работы с 3D моделями.
 * 
 * <p>Используется для ошибок:
 * <ul>
 *   <li>Некорректная структура модели</li>
 *   <li>Ошибки трансформации модели</li>
 *   <li>Проблемы с валидацией модели</li>
 * </ul>
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class ModelException extends RuntimeException {
    
    public ModelException(String message) {
        super(message);
    }
    
    public ModelException(String message, Throwable cause) {
        super(message, cause);
    }
}
