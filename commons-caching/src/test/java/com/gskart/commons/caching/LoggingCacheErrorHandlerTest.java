package com.gskart.commons.caching;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import static org.assertj.core.api.Assertions.assertThatCode;

class LoggingCacheErrorHandlerTest {

    private final LoggingCacheErrorHandler handler = new LoggingCacheErrorHandler();
    private final Cache cache = new ConcurrentMapCache("products");
    private final RuntimeException failure = new IllegalStateException("redis is unreachable");

    @Test
    void aFailedReadLetsTheCallFallThroughToTheSource() {
        assertThatCode(() -> handler.handleCacheGetError(failure, cache, 42L)).doesNotThrowAnyException();
    }

    @Test
    void aFailedWriteLeavesTheValueUncachedRatherThanFailingTheRequest() {
        assertThatCode(() -> handler.handleCachePutError(failure, cache, 42L, "a product"))
                .doesNotThrowAnyException();
    }

    @Test
    void aFailedEvictionIsReportedButDoesNotBreakTheWrite() {
        assertThatCode(() -> handler.handleCacheEvictError(failure, cache, 42L)).doesNotThrowAnyException();
    }

    @Test
    void aFailedClearIsReportedButDoesNotBreakTheCall() {
        assertThatCode(() -> handler.handleCacheClearError(failure, cache)).doesNotThrowAnyException();
    }
}
