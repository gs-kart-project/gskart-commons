package com.gskart.commons.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Reports a unique or foreign-key violation as a conflict - a duplicate created in a race, or a row
 * still referenced by another - rather than as a server error.
 *
 * <p>The database's own message is logged but not returned: it names tables and constraints.
 */
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@RestControllerAdvice
public class DataIntegrityProblemDetailHandler {

    private static final Logger log = LoggerFactory.getLogger(DataIntegrityProblemDetailHandler.class);

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        log.warn("Data integrity violation: {}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "The request conflicts with an existing resource or a referential constraint.");
    }
}
