package com.ewallet.backend.exception;

import com.ewallet.backend.dto.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @SuppressWarnings("null")
    @Test
    void shouldHandleBadRequestException() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleBadRequest(new BadRequestException("Bad input"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Bad input");
    }

    @SuppressWarnings("null")
    @Test
    void shouldHandleNotFoundException() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleNotFound(new NotFoundException("Missing"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Missing");
    }

    @SuppressWarnings("null")
    @Test
    void shouldHandleMethodArgumentValidationErrors() {
        BindingResult bindingResult = new org.springframework.validation.BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "receiverPhone", "", false, null, null, "Receiver phone is required"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiResponse<Map<String, String>>> response = handler.handleValidation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Validation failed");
    }

    @SuppressWarnings("null")
    @Test
    void shouldHandleGenericException() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleAll(new RuntimeException("Boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Internal Server Error");
    }
}
