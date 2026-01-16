package com.cgvsu.exceptions;

/**
 * Базовое исключение для ошибок рендеринга.
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class RenderException extends RuntimeException {
    
    public RenderException(String message) {
        super(message);
    }
    
    public RenderException(String message, Throwable cause) {
        super(message, cause);
    }
}
