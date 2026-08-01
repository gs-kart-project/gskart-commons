package com.gskart.commons.web;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemDetailExceptionHandlerTest {

    private final ProblemDetailExceptionHandler handler = new DefaultProblemDetailExceptionHandler();

    @SuppressWarnings("unused")
    private void endpoint(String name) {
    }

    @SuppressWarnings("unchecked")
    private static List<String> errorsOf(ProblemDetail problemDetail) {
        return (List<String>) problemDetail.getProperties().get("errors");
    }

    private MethodArgumentNotValidException validationFailure() throws NoSuchMethodException {
        Method method = getClass().getDeclaredMethod("endpoint", String.class);
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "name", "must not be blank"));
        bindingResult.addError(new FieldError("request", "price", "must be positive"));
        return new MethodArgumentNotValidException(new MethodParameter(method, 0), bindingResult);
    }

    @Test
    void reportsEachRejectedFieldSeparately() throws Exception {
        ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(validationFailure(),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, new ServletWebRequest(new MockHttpServletRequest()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ProblemDetail body = (ProblemDetail) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getDetail()).isEqualTo("Validation failed");
        assertThat(errorsOf(body)).containsExactly("name: must not be blank", "price: must be positive");
    }

    @Test
    void answersAnUnknownRouteWithoutEchoingThePath() {
        NoResourceFoundException exception =
                new NoResourceFoundException(HttpMethod.GET, "/internal/secret", "/internal/secret");

        ResponseEntity<Object> response = handler.handleNoResourceFoundException(exception, new HttpHeaders(),
                HttpStatus.NOT_FOUND, new ServletWebRequest(new MockHttpServletRequest()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ProblemDetail body = (ProblemDetail) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getDetail()).isEqualTo("Requested resource was not found.");
        assertThat(body.getDetail()).doesNotContain("/internal/secret");
    }

    @Test
    void keepsTheDetailGenericForAnythingUnexpected() {
        ProblemDetail body = handler.handleUnexpectedException(
                new IllegalStateException("connection string user:password@host"));

        assertThat(body.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(body.getDetail()).isEqualTo("Unexpected error occurred. Unable to process this request.");
        assertThat(body.getDetail()).doesNotContain("password");
    }
}
