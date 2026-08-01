package com.gskart.commons.caching.autoconfigure;

import com.gskart.commons.caching.LoggingCacheErrorHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * The caching conventions the services share: bounded lifetimes, readable JSON values, keys that
 * can't collide between services, and a cache failure that degrades instead of failing the request.
 *
 * <p>There is deliberately no cache interface of our own. Spring's {@code CacheManager} already is
 * the seam between the application and whatever stores the entries, so swapping Redis for something
 * else means swapping that one bean; and moving from a local Redis to a managed one is a change of
 * host and credentials, not of code. What is genuinely shared is the policy, and that is what lives
 * here. Which methods get cached, and under which keys, stays with the service that knows its own
 * read and write patterns.
 *
 * <p>Everything is off until a service asks for it, and turning caching on itself stays with the
 * service: {@code @EnableCaching} changes what its methods do, which is not something a dependency
 * should decide.
 */
@AutoConfiguration
@ConditionalOnClass(CachingConfigurer.class)
@ConditionalOnProperty(prefix = "gskart.commons.caching", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(CommonsCachingProperties.class)
public class CommonsCachingAutoConfiguration {

    /**
     * Applied by Spring Boot as the default for every cache, so a service gets the conventions
     * without repeating them per cache.
     */
    // The JSON value serializer needs Jackson, which every service that serves an API already has.
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(value = RedisCacheConfiguration.class, name = "tools.jackson.databind.ObjectMapper")
    static class RedisConfiguration {

        @Bean
        @ConditionalOnMissingBean
        RedisCacheConfiguration redisCacheConfiguration(CommonsCachingProperties properties,
                Environment environment) {
            RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(properties.timeToLive())
                    // A cached null would answer for something that has since been created.
                    .disableCachingNullValues()
                    .serializeValuesWith(RedisSerializationContext.SerializationPair
                            .fromSerializer(RedisSerializer.json()));
            if (properties.keyPrefixed()) {
                String application = environment.getProperty("spring.application.name", "gskart");
                configuration = configuration.computePrefixWith(
                        CacheKeyPrefix.prefixed(application + "::"));
            }
            return configuration;
        }
    }

    @Bean
    @ConditionalOnMissingBean(CachingConfigurer.class)
    public CachingConfigurer failOpenCachingConfigurer() {
        return new CachingConfigurer() {
            @Override
            public CacheErrorHandler errorHandler() {
                return new LoggingCacheErrorHandler();
            }
        };
    }
}
