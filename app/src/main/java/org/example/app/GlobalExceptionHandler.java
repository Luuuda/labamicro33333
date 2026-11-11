package org.example.app;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Цей метод ловить помилки валідації (коли @Valid не пропускає DTO)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new LinkedHashMap<>();

        // 1. Додаємо таймстемп (вимога з ТЗ)
        body.put("timestamp", LocalDateTime.now());

        // 2. Додаємо код помилки
        body.put("status", HttpStatus.BAD_REQUEST.value());

        // 3. Додаємо загальний опис
        body.put("error", "Помилка валідації вхідних даних");

        // 4. Витягуємо КОНКРЕТНІ тексти помилок з наших DTO (про пошту та ім'я)
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        body.put("details", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}