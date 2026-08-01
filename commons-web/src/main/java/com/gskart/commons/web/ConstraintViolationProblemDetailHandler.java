package com.gskart.commons.web;

import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Covers validation on path variables and request parameters, which arrives as a constraint
 * violation rather than a binding failure. The response shape matches the one used for request
 * bodies, so a client parses failures the same way whichever endpoint rejected the call.
 *
 * <p>Separate advice for the same reason as the access-denied one: bean validation is optional here.
 */
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@RestControllerAdvice
public class ConstraintViolationProblemDetailHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException exception) {
        List<String> errors = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }
}
