package com.gskart.commons.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Turns a denied authorization check into a 403 instead of letting it reach a catch-all and come
 * back as a 500.
 *
 * <p>It lives in its own advice rather than on the base class because Spring reads the exception
 * types off the annotation as soon as the bean is inspected: a service without Spring Security on
 * the classpath would fail to start if this handler were part of the base everyone extends.
 */
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@RestControllerAdvice
public class AccessDeniedProblemDetailHandler {

    private static final Logger log = LoggerFactory.getLogger(AccessDeniedProblemDetailHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException exception) {
        log.warn("Access denied: {}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action.");
    }
}
