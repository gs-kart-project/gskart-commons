package com.gskart.commons.web.autoconfigure;

import com.gskart.commons.web.AccessDeniedProblemDetailHandler;
import com.gskart.commons.web.ConstraintViolationProblemDetailHandler;
import com.gskart.commons.web.DataIntegrityProblemDetailHandler;
import com.gskart.commons.web.DefaultProblemDetailExceptionHandler;
import com.gskart.commons.web.ProblemDetailExceptionHandler;
import jakarta.validation.ConstraintViolationException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Registers the shared error handling.
 *
 * <p>The handlers that depend on something optional each sit in their own nested configuration, so
 * a service without bean validation, Spring Security or a transactional data layer still gets the
 * rest of them - and never gets a startup failure over an exception class it doesn't have.
 */
@AutoConfiguration
@ConditionalOnClass(ResponseEntityExceptionHandler.class)
@ConditionalOnProperty(prefix = "gskart.commons.web", name = "problem-details.enabled", havingValue = "true",
        matchIfMissing = true)
public class CommonsWebAutoConfiguration {

    /**
     * Only registered when the service has no handler of its own; the moment it writes one - by
     * extending {@link ProblemDetailExceptionHandler} - that one takes over completely.
     */
    @Bean
    @ConditionalOnMissingBean(ProblemDetailExceptionHandler.class)
    public DefaultProblemDetailExceptionHandler defaultProblemDetailExceptionHandler() {
        return new DefaultProblemDetailExceptionHandler();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(AccessDeniedException.class)
    static class AccessDeniedConfiguration {

        @Bean
        @ConditionalOnMissingBean
        AccessDeniedProblemDetailHandler accessDeniedProblemDetailHandler() {
            return new AccessDeniedProblemDetailHandler();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ConstraintViolationException.class)
    static class ConstraintViolationConfiguration {

        @Bean
        @ConditionalOnMissingBean
        ConstraintViolationProblemDetailHandler constraintViolationProblemDetailHandler() {
            return new ConstraintViolationProblemDetailHandler();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(DataIntegrityViolationException.class)
    static class DataIntegrityConfiguration {

        @Bean
        @ConditionalOnMissingBean
        DataIntegrityProblemDetailHandler dataIntegrityProblemDetailHandler() {
            return new DataIntegrityProblemDetailHandler();
        }
    }
}
