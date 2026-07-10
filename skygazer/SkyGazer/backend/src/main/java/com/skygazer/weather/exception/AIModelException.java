package com.skygazer.weather.exception;

public class AIModelException extends RuntimeException {
    
    public AIModelException(String message) {
        super(message);
    }
    
    public AIModelException(String message, Throwable cause) {
        super(message, cause);
    }
}
