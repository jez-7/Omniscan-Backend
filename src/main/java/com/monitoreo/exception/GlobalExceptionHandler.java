package com.monitoreo.exception;

import com.monitoreo.model.dto.ErrorMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Error interno no controlado: ", ex);

        ErrorMessage error = ErrorMessage.builder()
                .message("Ocurrió un error interno. Por favor, intente de nuevo más tarde.")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timeStamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorMessage> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String detail = String.format("El valor '%s' no es válido para el parámetro '%s'", ex.getValue(), ex.getName());

        ErrorMessage error = ErrorMessage.builder()
                .message(detail)
                .status(HttpStatus.BAD_REQUEST.value())
                .timeStamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorMessage> NoHandlerFoundException(NoHandlerFoundException ex, HttpServletRequest request) {
        String detail = String.format("No se encontró un endpoint para la ruta '%s'", ex.getRequestURL());
        ErrorMessage error = ErrorMessage.builder()
                .message(detail)
                .status(HttpStatus.NOT_FOUND.value())
                .timeStamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.notFound().build();

    }

}
