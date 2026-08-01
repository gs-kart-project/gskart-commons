package com.gskart.commons.web.autoconfigure;

import com.gskart.commons.web.AccessDeniedProblemDetailHandler;
import com.gskart.commons.web.ConstraintViolationProblemDetailHandler;
import com.gskart.commons.web.DataIntegrityProblemDetailHandler;
import com.gskart.commons.web.DefaultProblemDetailExceptionHandler;
import com.gskart.commons.web.ProblemDetailExceptionHandler;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.assertj.core.api.Assertions.assertThat;

class CommonsWebAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CommonsWebAutoConfiguration.class));

    @Test
    void contributesTheProblemDetailHandlers() {
        runner.run(context -> assertThat(context)
                .hasSingleBean(DefaultProblemDetailExceptionHandler.class)
                .hasSingleBean(AccessDeniedProblemDetailHandler.class)
                .hasSingleBean(ConstraintViolationProblemDetailHandler.class)
                .hasSingleBean(DataIntegrityProblemDetailHandler.class));
    }

    @Test
    void standsAsideForAServiceThatWritesItsOwnHandler() {
        runner.withUserConfiguration(ServiceOwnHandler.class).run(context -> {
            assertThat(context).doesNotHaveBean(DefaultProblemDetailExceptionHandler.class);
            assertThat(context).hasSingleBean(ProblemDetailExceptionHandler.class);
        });
    }

    @Test
    void skipsTheAccessDeniedHandlerWithoutSpringSecurity() {
        runner.withClassLoader(new FilteredClassLoader(AccessDeniedException.class))
                .run(context -> assertThat(context)
                        .doesNotHaveBean(AccessDeniedProblemDetailHandler.class)
                        .hasSingleBean(DefaultProblemDetailExceptionHandler.class));
    }

    @Test
    void skipsTheParameterValidationHandlerWithoutBeanValidation() {
        runner.withClassLoader(new FilteredClassLoader(ConstraintViolationException.class))
                .run(context -> assertThat(context)
                        .doesNotHaveBean(ConstraintViolationProblemDetailHandler.class)
                        .hasSingleBean(DefaultProblemDetailExceptionHandler.class));
    }

    @Test
    void contributesNothingWhenSwitchedOff() {
        runner.withPropertyValues("gskart.commons.web.problem-details.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(DefaultProblemDetailExceptionHandler.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class ServiceOwnHandler {

        @Bean
        ServiceExceptionHandler serviceExceptionHandler() {
            return new ServiceExceptionHandler();
        }
    }

    @RestControllerAdvice
    static class ServiceExceptionHandler extends ProblemDetailExceptionHandler {
    }
}
