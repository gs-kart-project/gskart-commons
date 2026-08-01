package com.gskart.commons.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionalProblemDetailHandlersTest {

    @SuppressWarnings("unchecked")
    private static List<String> errorsOf(ProblemDetail problemDetail) {
        return (List<String>) problemDetail.getProperties().get("errors");
    }

    @Test
    void deniedAuthorizationBecomesForbidden() {
        ProblemDetail body = new AccessDeniedProblemDetailHandler()
                .handleAccessDenied(new AccessDeniedException("Access Denied"));

        assertThat(body.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(body.getDetail()).isEqualTo("You do not have permission to perform this action.");
    }

    @Test
    void parameterValidationUsesTheSameErrorListAsBodyValidation() {
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("findById.id");
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be greater than 0");

        ProblemDetail body = new ConstraintViolationProblemDetailHandler()
                .handleConstraintViolation(new ConstraintViolationException(Set.of(violation)));

        assertThat(body.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(body.getDetail()).isEqualTo("Validation failed");
        assertThat(errorsOf(body)).containsExactly("findById.id: must be greater than 0");
    }

    @Test
    void constraintViolationBecomesConflictWithoutLeakingTheDatabaseMessage() {
        ProblemDetail body = new DataIntegrityProblemDetailHandler().handleDataIntegrityViolation(
                new DataIntegrityViolationException("Duplicate entry 'admin' for key 'roles.name_UNIQUE'"));

        assertThat(body.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(body.getDetail())
                .isEqualTo("The request conflicts with an existing resource or a referential constraint.");
        assertThat(body.getDetail()).doesNotContain("name_UNIQUE");
    }
}
