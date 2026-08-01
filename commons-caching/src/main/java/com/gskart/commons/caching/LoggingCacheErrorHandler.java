package com.gskart.commons.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.lang.Nullable;

/**
 * Keeps a cache outage from becoming an outage of the service.
 *
 * <p>Spring's default behaviour is to let the failure escape, so an unreachable cache turns every
 * read into a failed request even though the record is sitting in the database. Logging and carrying
 * on means a read falls through to its source and a write simply isn't cached - slower, but still
 * correct.
 *
 * <p>The one thing this cannot paper over is a stale entry left behind by a failed eviction, so
 * those are logged at error level rather than as a warning.
 */
public class LoggingCacheErrorHandler implements CacheErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggingCacheErrorHandler.class);

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Cache '{}' could not be read for key {}; falling back to the source.",
                cache.getName(), key, exception);
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, @Nullable Object value) {
        log.warn("Cache '{}' could not be updated for key {}; the value stays uncached.",
                cache.getName(), key, exception);
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.error("Cache '{}' could not evict key {}; it may now serve a stale value.",
                cache.getName(), key, exception);
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.error("Cache '{}' could not be cleared; it may now serve stale values.",
                cache.getName(), exception);
    }
}
