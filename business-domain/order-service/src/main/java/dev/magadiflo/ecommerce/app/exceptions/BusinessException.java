package dev.magadiflo.ecommerce.app.exceptions;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
