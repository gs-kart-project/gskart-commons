package com.gskart.commons.web;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Used when a service hasn't written an exception handler of its own, so simply depending on this
 * module already gives it problem-details responses.
 *
 * <p>Ordered last: a service that does have its own advice always gets first refusal on an
 * exception.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class DefaultProblemDetailExceptionHandler extends ProblemDetailExceptionHandler {
}
