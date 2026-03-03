package com.gditTakeHome.fafsaEditProcessor.controller;

import com.gditTakeHome.fafsaEditProcessor.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles request parsing errors and returns normalized 400 responses.
 *
 * <p>Spring's default error handling for {@link HttpMessageNotReadableException} leaks
 * internal class names and package paths. This handler intercepts those exceptions and
 * returns a clean {@link ErrorResponse} with a human-readable message.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_TITLE = "Invalid request body";

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        String message = buildMessage(ex);
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .error(ERROR_TITLE)
                        .message(message)
                        .build());
    }

    private String buildMessage(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException ife) {
            String fieldName = extractLeafFieldName(ife);
            String rejectedValue = String.valueOf(ife.getValue());
            Class<?> targetType = ife.getTargetType();

            if (targetType != null && targetType.isEnum()) {
                // List the accepted enum values so the caller knows what to send
                String accepted = Arrays.stream(targetType.getEnumConstants())
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));
                return String.format("Invalid value '%s' for field '%s'. Accepted values: %s",
                        rejectedValue, fieldName, accepted);
            }

            if (targetType == LocalDate.class) {
                return String.format("Invalid value '%s' for field '%s'. Expected format: YYYY-MM-DD",
                        rejectedValue, fieldName);
            }

            // Generic type mismatch (e.g. string where integer expected)
            return String.format("Invalid value '%s' for field '%s'.", rejectedValue, fieldName);
        }

        // Syntax errors, missing body, or other unstructured parse failures
        return "Malformed JSON: could not parse request body.";
    }

    /**
     * Extracts the leaf field name from Jackson's reference path.
     * For a nested field like studentInfo -> dateOfBirth, returns "dateOfBirth".
     */
    private String extractLeafFieldName(InvalidFormatException ife) {
        List<JacksonException.Reference> path = ife.getPath();
        if (path == null || path.isEmpty()) {
            return "unknown";
        }
        String fieldName = path.get(path.size() - 1).getPropertyName();
        return fieldName != null ? fieldName : "unknown";
    }
}